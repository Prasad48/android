package com.useriq.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Lean WebSocket implementation with PMCE. Server should support
 * 1. PMCE with (permessage-deflate, client_no_context_takeover, server_no_context_takeover)
 * 2. TLS1.0, TLS1.1, TLS1.2 for secure WebSockets
 * <p>
 * URI uri = new URI("http://localhost:3000");
 * Map<String, String> headers = new HashMap<>();
 * WS ws = new WS(uri, headers);
 * WS.Listener listener = new WS.Listener() {
 * void onConnect(WS ws, Connection connection) {}
 * void onFrame(Frame frame) {}
 * void onFailed(Exception error, Frame, toSend) {}
 * }
 * <p>
 * try {
 * ws.connect(listener);
 * } catch(IOException e) {
 * // Usually caused by network or data or unexpected closure
 * // worth retrying!
 * } catch(WS.Error e) {
 * // Connection aborted due to any of
 * //   1. Server failed to meet this clients requirements
 * //   2. Server sent a frame with unexpected format
 * // Hence not supported. Simply abort. Dont retry.
 * }
 * <p>
 * Both ws.connect & ws.send* are designed as synchronous blocking calls.
 * Each of them should be called in its own thread.
 * <p>
 * ws.connect will act as receiver calling onFrame
 * ws.send will use calling thread to wite data
 */
public final class WS {
    private static final int MIN_COMPRESS_SIZE = 256;

    private static final Pattern SECURE = Pattern.compile("^(wss|https)", Pattern.CASE_INSENSITIVE);

    private final Object lock = new Object();

    private Socket socket;
    /**
     * selfClosing: is close initiated from client side
     */
    private boolean selfClosing = false;
    private Connection connection;
    private OutputStream outStream;
    private Status status = Status.CLOSED;
    private Listener listener;
    private RFC6455.ReceiveListener receiver = new RFC6455.ReceiveListener() {
        @Override
        public void onReceive(Frame frame) throws IOException {
            switch (frame.type) {
                case CLOSE:
                    if (!selfClosing) {
                        send(frame);
                        status = Status.CLOSING;
                    } else closeSilently(WS.this.socket);
                    break;
                case PING:
                    if (status == Status.CONNECTED)
                        send(new Frame(FrameType.PONG, frame.bytes, frame.text, 0, false));
                    break;
            }
            listener.onFrame(frame);
        }
    };

    public WS(Listener listener) {
        this.listener = listener;
    }

    public void connect(URI uri, Map<String, String> headers) throws IOException, Error {
        synchronized (lock) {
            status = Status.CONNECTING;
            selfClosing = false;

            try {
                SocketFactory factory = SECURE.matcher(uri.getScheme()).matches()
                        ? getSSLSocketFactory()
                        : SocketFactory.getDefault();

                connection = new Connection(uri, headers);

                socket = connection.perform(factory);
                outStream = socket.getOutputStream();
                InputStream inpStream = socket.getInputStream();
                status = Status.CONNECTED;

                listener.onConnect(this, connection);

                RFC6455.Receiver receiver = new RFC6455.Receiver(inpStream, this.receiver);

                // Will block the current thread until connection is
                // fully terminated. Keeps reading the input stream &
                // dispatches incoming frames
                receiver.run();
            } catch (IOException e) {
                if (status != Status.CLOSING) throw e;
            } catch (Exception e) {
                throw e;
            } finally {
                closeSilently(socket);
                status = Status.CLOSED;
            }
        }
    }

    private SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null);
            return context.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace(); // shouldnt happen
        }
        return null;
    }

    private void closeSilently(final Socket socket) {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    public Status getStatus() {
        return status;
    }

    public void ping(String text) throws IOException {
        send(Frame.build(FrameType.PING, text, false));
    }

    private synchronized void send(Frame frame) throws IOException {
        if (status != Status.CONNECTED)
            throw new IOException("Not Connected");

        byte[] bytes;
        boolean zip = connection.canZip && frame.canZip;

        try {
            bytes = RFC6455.frameToBytes(frame.type, frame.bytes, zip);
            outStream.write(bytes);
            outStream.flush();
        } catch (IOException e) {
            closeSilently(socket);
            throw e;
        }
    }

    public void pong(String text) throws IOException {
        send(Frame.build(FrameType.PONG, text, false));
    }

    public void send(Object data) throws IOException {
        if (data instanceof byte[])
            send(new Frame(FrameType.BINARY, (byte[]) data, null, 0, false));
        else {
            String text = (String) data;
            boolean canZip = text.length() > MIN_COMPRESS_SIZE;
            send(Frame.build(FrameType.TEXT, text, canZip));
        }
    }

    public void close(int code, String reason) throws IOException {
        selfClosing = true;

        byte[] bytes = reason.getBytes(Charset.forName("UTF-8"));
        ByteBuffer buff = ByteBuffer.allocate(bytes.length + 2);
        buff.putShort((short) code);
        buff.put(bytes);
        Frame closeFrame = new Frame(FrameType.CLOSE, buff.array(), reason, code, false);

        send(closeFrame);

        status = Status.CLOSING;
    }

    public enum FrameType {
        CONTINUATION(0),
        TEXT(0x1),
        BINARY(0x2),
        CLOSE(0x8),
        PING(0x9),
        PONG(0xA);

        private static final Map<Integer, FrameType> TYPE_MAP = new HashMap<>();
        public final int id;

        FrameType(int id) {
            this.id = id;
        }

        public static FrameType of(int x) {
            if (TYPE_MAP.isEmpty()) populate();
            return TYPE_MAP.get(x);
        }

        private static void populate() {
            for (FrameType t : FrameType.values()) {
                TYPE_MAP.put(t.id, t);
            }
        }

        public static boolean isControl(FrameType typ) {
            return typ.id >= 8;
        }
    }

    public enum Status {CONNECTING, CONNECTED, CLOSING, CLOSED}

    /**
     * Listener to emit lifecycle events.
     * NOTE: Exceptions should not be thrown in these callbacks
     */
    public interface Listener {
        void onConnect(WS ws, Connection connection);

        void onFrame(Frame frame);
    }

    public static final class Frame {
        public final byte[] bytes;
        public final String text;
        public final int code;
        public final FrameType type;
        public final boolean canZip;

        Frame(FrameType type, byte[] bytes, String text, int code, boolean canZip) {
            this.bytes = bytes;
            this.text = text;
            this.code = code;
            this.type = type;
            this.canZip = canZip;
        }

        static Frame build(FrameType type, String msg, boolean canZip) {
            return new Frame(type, msg.getBytes(Charset.forName("UTF-8")), msg, 0, canZip);
        }
    }

    public static class Error extends Exception {
        public Error(String detailMessage) {
            super(detailMessage);
        }
    }
}

