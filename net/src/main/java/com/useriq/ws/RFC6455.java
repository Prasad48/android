package com.useriq.ws;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * RFC6455 protocol Sender & Receiver
 * <p>
 * <pre>
 * 	  0                   1                   2                   3
 * 	  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * 	 +-+-+-+-+-------+-+-------------+-------------------------------+
 * 	 |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
 * 	 |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
 * 	 |N|V|V|V|       |S|             |   (if payload len==126/127)   |
 * 	 | |1|2|3|       |K|             |                               |
 * 	 +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
 * 	 |     Extended payload length continued, if payload len == 127  |
 * 	 + - - - - - - - - - - - - - - - +-------------------------------+
 * 	 |                               |Masking-key, if MASK set to 1  |
 * 	 +-------------------------------+-------------------------------+
 * 	 | Masking-key (continued)       |          Payload Data         |
 * 	 +-------------------------------- - - - - - - - - - - - - - - - +
 * 	 :                     Payload Data continued ...                :
 * 	 + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
 * 	 |                     Payload Data continued ...                |
 * 	 +---------------------------------------------------------------+
 * </pre>
 */
final class RFC6455 {
    private static final int BYTE = 255;
    private static final int FIN = 128;
    private static final int MASK = 128;
    private static final int RSV1 = 64;
    private static final int RSV2 = 32;
    private static final int RSV3 = 16;
    private static final int OPCODE = 15;
    private static final int LENGTH = 127;
    private static final Random random = new Random();
    private static final int COMPRESS_BUFFER = 512;

    private static final Pool<Inflater> inflaterPool = new Pool<>(new Pool.Factory<Inflater>() {
        @Override
        protected Inflater generate() {
            return new Inflater(true);
        }
    });

    private static final Pool<Deflater> deflaterPool = new Pool<>(new Pool.Factory<Deflater>() {
        @Override
        protected Deflater generate() {
            return new Deflater(Deflater.BEST_COMPRESSION, true);
        }
    });

    private static long byteArrayToLong(byte[] b) {
        int length = b.length;
        long value = 0;
        for (int i = 0; i < length; i++) {
            int shift = (length - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }

    private static byte[] readBytes(DataInputStream inp, int length) throws IOException {
        byte[] buffer = new byte[length];
        inp.readFully(buffer);
        return buffer;
    }

    private static byte[] decompress(byte[] source) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(source.length);

        Inflater decompressor = inflaterPool.borrow();

        decompressor.reset();

        InflaterOutputStream ios = new InflaterOutputStream(buffer, decompressor, COMPRESS_BUFFER);
        OutputStream os = new BufferedOutputStream(ios);
        os.write(source);
        os.flush();
        ios.finish();

        inflaterPool.giveBack(decompressor);
        return buffer.toByteArray();
    }

    private static String encode(byte[] buffer) {
        try {
            return new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // Shouldnt happen.
            return "";
        }
    }

    static byte[] frameToBytes(WS.FrameType type, byte[] bytes, boolean zip) throws IOException {
        int len = bytes.length;
        int opByte = 0b10000000 | type.id;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if (zip) {
            try {
                bytes = compress(bytes);
                opByte |= RSV1;
            } catch (IOException e) { // skip compression
                e.printStackTrace();
            }
        }

        bos.write(opByte);

        if (len <= 125) {
            bos.write(MASK | len);
        } else if (len <= 65535) {
            bos.write(MASK | 126);
            bos.write((len >> 8) & BYTE);
            bos.write(len & BYTE);
        } else {
            bos.write((byte) (MASK | 127));
            // We support only 2^31-1 (ie) max 2.14GB in payload
            // so first 4 bytes are 0
            bos.write(0);
            bos.write(0);
            bos.write(0);
            bos.write(0);
            bos.write((len >> 24) & BYTE);
            bos.write((len >> 16) & BYTE);
            bos.write((len >> 8) & BYTE);
            bos.write(len & BYTE);
        }

        // Client frames should always be masked
        byte[] maskKey = randomBytes(4);
        bos.write(maskKey);
        applyMask(bytes, maskKey);
        bos.write(bytes);
        bos.flush();
        return bos.toByteArray();
    }

    private static byte[] compress(byte[] source) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(source.length);

        Deflater compressor = deflaterPool.borrow();

        compressor.reset();

        DeflaterOutputStream dos = new DeflaterOutputStream(buffer, compressor, COMPRESS_BUFFER);
        OutputStream os = new BufferedOutputStream(dos);
        os.write(source);
        os.flush();
        dos.finish();

        deflaterPool.giveBack(compressor);
        return buffer.toByteArray();
    }

    private static byte[] randomBytes(int length) {
        byte[] buff = new byte[length];
        random.nextBytes(buff);
        return buff;
    }

    private static void applyMask(byte[] payload, byte[] mask) {
        if (mask.length == 0) return;

        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (payload[i] ^ mask[i % 4]);
        }
    }

    interface ReceiveListener {
        void onReceive(WS.Frame frame) throws IOException;
    }

    static class Receiver {
        private final DataInputStream inp;
        private final ReceiveListener recv;
        private Fragment frag;

        Receiver(InputStream inp, ReceiveListener recv) {
            this.recv = recv;
            this.inp = new DataInputStream(inp);
        }

        void run() throws IOException, WS.Error {
            while (inp.available() != -1) {
                byte byte1 = inp.readByte();
                byte byte2 = inp.readByte();
                boolean isMasked = (byte2 & MASK) == MASK;

                // TODO: Should we abort ?
                // if(isMasked) // https://tools.ietf.org/html/rfc6455#section-5.1
                //    throw new Connection.Error("Server Must not mask frames");

                int length = parseLength(byte2);
                byte[] mask = isMasked ? readBytes(inp, 4) : new byte[0];
                byte[] payload = readBytes(inp, length);

                this.buildAndEmit(byte1, mask, payload);
            }
        }

        private int parseLength(byte byte2) throws IOException {
            int length = byte2 & LENGTH;

            if (length >= 0 && length <= 125)
                return length;

            int lenSize = (length == 126) ? 2 : 8;
            byte[] extLength = new byte[lenSize];
            inp.read(extLength);

            return (int) byteArrayToLong(extLength);
        }

        private void buildAndEmit(byte opByte, byte[] mask, byte[] payload) throws WS.Error, IOException {
            boolean fin = (opByte & FIN) == FIN;
            boolean zipped = (opByte & RSV1) == RSV1;
            WS.FrameType type = WS.FrameType.of(opByte & OPCODE);

            boolean rsv2 = (opByte & RSV2) == RSV2;
            boolean rsv3 = (opByte & RSV3) == RSV3;

            if (rsv2) throw new WS.Error("RSV2 should be zero");
            if (rsv3) throw new WS.Error("RSV3 should be zero");
            if (type == null) throw new WS.Error("Unknown Opcode");
            if (WS.FrameType.isControl(type) && !fin)
                throw new WS.Error("Control Frame fragmented");

            applyMask(payload, mask);

            if (WS.FrameType.isControl(type)) {
                if (type == WS.FrameType.CLOSE) {
                    int code = 1000;
                    String reason = "";
                    if (payload.length >= 2) {
                        code = ByteBuffer.wrap(new byte[]{payload[0], payload[1]}).getShort();
                        reason = RFC6455.encode(Arrays.copyOfRange(payload, 2, payload.length));
                    }
                    recv.onReceive(new WS.Frame(type, payload, reason, code, false));
                } else
                    recv.onReceive(new WS.Frame(type, payload, encode(payload), 0, false));
                return;
            }

            if (type == WS.FrameType.CONTINUATION) {
                if (frag == null)
                    throw new WS.Error("Orphaned CONTINUATION frame");
                frag.buff.write(payload);
            } else {
                frag = new Fragment(type, zipped); // TEXT/BINARY
                frag.buff.write(payload);
            }

            if (fin) {
                payload = frag.buff.toByteArray();
                if (frag.zipped) payload = decompress(payload);
                String txt = frag.type == WS.FrameType.TEXT ? encode(payload) : null;
                recv.onReceive(new WS.Frame(frag.type, payload, txt, 0, false));
                frag = null;
            }
        }
    }

    static class Fragment {
        final WS.FrameType type;
        final boolean zipped;
        final ByteArrayOutputStream buff = new ByteArrayOutputStream();

        Fragment(WS.FrameType type, boolean zipped) {
            this.type = type;
            this.zipped = zipped;
        }
    }

    static final class Pool<T> {
        final Factory factory;
        final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<T>();

        Pool(Factory factory) {
            this.factory = factory;
        }

        T borrow() {
            T instance = (T) queue.poll();
            return instance == null
                    ? (T) factory.generate()
                    : instance;
        }

        void giveBack(T object) {
            queue.offer(object);
        }

        static abstract class Factory<T> {
            protected abstract T generate();
        }
    }

}
