package com.useriq;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

import javax.net.SocketFactory;

/**
 * @author sudhakar
 * @created 10-May-2018
 * <p>
 * TODO: WIP
 */
public class TCPTransport implements RPCTransport {
    private final URI uri;
    private MessageHandler msgHandler;
    private Socket socket;
    private boolean connected;

    private final Retrier wsRetrier = new Retrier(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            connected = false;
            socket = SocketFactory.getDefault().createSocket(uri.getHost(), uri.getPort());
            return null;
        }
    }, IOException.class);
    private MPack.Decoder decoder;
    private MPack.Encoder encoder;

    public TCPTransport(URI uri) {
        this.uri = uri;
    }

    public void start() throws IOException {
        wsRetrier.start();

        decoder = new MPack.Decoder(socket.getInputStream());
        encoder = new MPack.Encoder(socket.getOutputStream());
        this.onConnect(socket);

        //noinspection InfiniteLoopStatement
        while (true) {
            List packet = (List) decoder.decode();
            msgHandler.handleMessage(packet);
        }
    }

    public void cancel() {

    }

    public void onConnect(Socket socket) {

        // if onConnect is required, create anonymous class and override
    }

    @Override
    public void send(List packet) throws IOException {
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
}


