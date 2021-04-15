package com.useriq;

import java.io.IOException;
import java.util.List;

/**
 * @author sudhakar
 * @created 12-May-2018
 */

class Piped {
    private static RPCTransport.MessageHandler serverMsgHandler;
    private static RPCTransport.MessageHandler clientMsgHandler;

    static final RPCTransport serverTransport = new RPCTransport() {
        public void send(List packet) throws IOException {
            // Our MPack impl converts upto byte, int & long -TO-> Long
            // So lets fake'm
            byte[] bytes = MPack.encode(packet);
            List msg = (List) MPack.decode(bytes);
            clientMsgHandler.handleMessage(msg);
        }

        public void setMsgHandler(MessageHandler msgHandler) {
            serverMsgHandler = msgHandler;
        }
    };

    static final RPCTransport clientTransport = new RPCTransport() {
        public void send(List packet) throws IOException {
            // Our MPack impl converts upto byte, int & long -TO-> Long
            // So lets fake'm
            byte[] bytes = MPack.encode(packet);
            List msg = (List) MPack.decode(bytes);
            serverMsgHandler.handleMessage(msg);
        }

        public void setMsgHandler(MessageHandler msgHandler) {
            clientMsgHandler = msgHandler;
        }
    };
}

