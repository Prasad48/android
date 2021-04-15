package com.useriq;


import com.useriq.ws.Connection;
import com.useriq.ws.WS;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author sudhakar
 * @created 10-May-2018
 */

public abstract class WSTransport implements RPCTransport {
    private final WS ws;
    private MessageHandler msgHandler;
    private Map<String, String> headers = new HashMap<>();

    private boolean connected;

    private final WS.Listener wsListener = new WS.Listener() {
        @Override
        public void onConnect(WS ws, Connection connection) {
            connected = true;
            wsRetrier.reset();
            WSTransport.this.onConnect(ws, connection);
        }

        @Override
        public void onFrame(WS.Frame frame) {
            if (frame.type == WS.FrameType.TEXT) {
                System.err.println("Text messages are unsupported: " + frame.text);
                return;
            }

            if (frame.type != WS.FrameType.BINARY) return;

            try {
                List packet = (List) MPack.decode(frame.bytes);
                msgHandler.handleMessage(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private final Retrier wsRetrier = new Retrier(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            connected = false;
            URI host = getHost();
            ws.connect(host, headers);
            connected = false;
            return null;
        }
    }, IOException.class);

    public WSTransport() {
        this.ws = new WS(wsListener);
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    @Override
    public void send(List packet) throws IOException {
        byte[] bytes = MPack.encode(packet);
        this.ws.send(bytes);
    }

    @Override
    public void setMsgHandler(MessageHandler msgHandler) {
        this.msgHandler = msgHandler;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * start starts WSRetrier which retries ws
     * connection on IO Error
     */
    public void start() {
        wsRetrier.start();
    }

    public void cancel() throws IOException {
        connected = false;
        ws.close(1000, "Shutting down");
        wsRetrier.cancel();
    }

    public void onConnect(WS ws, Connection connection) {
        // if onConnect is required, create anonymous class and override
    }

}
