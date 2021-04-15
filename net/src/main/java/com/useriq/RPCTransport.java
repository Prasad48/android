package com.useriq;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * @author sudhakar
 * @created 10-May-2018
 */

public interface RPCTransport {
    void send(List packet) throws IOException;

    void setMsgHandler(MessageHandler msgHandler);

    URI getHost() throws Exception;

    interface MessageHandler {
        void handleMessage(List packet) throws IOException;
    }
}
