package com.useriq;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import com.useriq.rdp.Ln;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * LocalTransport uses UNIX socket to establish connection
 *
 * Use `add reverse localabstract:uiq_rdp tcp:9090` to make
 * connection available in the host at 9090 port.
 *
 * @author sudhakar
 * @created 13-May-2018
 *
 */
public class LocalTransport implements RPCTransport {
    private final LocalSocketAddress address;
    private final String sockName;
    private final Map<String, String> hdrs;
    private final LocalSocket socket;
    private MessageHandler msgHandler;
    private boolean connected;

    /**
     * Retrier is not really required in LocalTransport as
     * we use it only inside android-host. Technically the
     * server should always be running. Even if the socket is
     * closed, host process can kill rdp & restart it!
     *
     * TODO: Test & simplify LocalTransport
     */
    private final Retrier wsRetrier = new Retrier(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            connected = false;
            socket.connect(address);
            return null;
        }
    }, IOException.class);
    private MPack.Decoder decoder;
    private MPack.Encoder encoder;

    public LocalTransport(String sockName, Map<String, String> hdrs) {
        this.address = new LocalSocketAddress(sockName);
        this.sockName = sockName;
        this.hdrs = hdrs;
        this.socket = new LocalSocket();
    }

    public void start() throws IOException {
        wsRetrier.start();

        decoder = new MPack.Decoder(socket.getInputStream());
        encoder = new MPack.Encoder(socket.getOutputStream());
        connected = true;
        this.writeHeader();
        this.onConnect(socket);

        //noinspection InfiniteLoopStatement
        while (true){
            List packet = (List) decoder.decode();
            Ln.i("deviceMessage:" + packet.get(0) + packet.get(1) + packet.get(2));
            msgHandler.handleMessage(packet);
        }
    }

    public void cancel() {

    }

    public void onConnect(LocalSocket socket) {
        // if onConnect is required, create anonymous class and override
    }

    @Override
    public void send(List packet) throws IOException {
        Log.d("LocalTransport", packet.get(0) + ", " + packet.get(1) + ", " + packet.get(2));
        encoder.encode(packet);
    }

    @Override
    public void setMsgHandler(MessageHandler msgHandler) {
        this.msgHandler = msgHandler;
    }

    @Override
    public URI getHost() throws Exception {
        return null;
    }

    public boolean isConnected() {
        return connected;
    }

    private void writeHeader() throws IOException {
        byte[] header = MPack.encode(this.hdrs);
        OutputStream os = socket.getOutputStream();
        DataOutputStream stream = new DataOutputStream(os);
        stream.writeShort(header.length);
        stream.write(header);
        stream.flush();
    }
}