package com.useriq.sdk;

import com.useriq.Logger;
import com.useriq.SimpleRPC;
import com.useriq.WSTransport;
import com.useriq.ws.Connection;
import com.useriq.ws.WS;

import java.io.IOException;
import java.util.Map;

/**
 * IO is responsible for starting WSTransport & handling SimpleRPC
 *
 * @author sudhakar
 * @created 26-Sep-2018
 */
public class IO {

    IO(SimpleRPC.IService sdkService, ThreadExecutor jobExecutor, ConnectListener listener) {
        this.jobExecutor = jobExecutor;
        this.wsTransport = new AppWSTransport(listener);
        this.simpleRpc = new SimpleRPC(wsTransport, sdkService);
        this.ioExecutor = new ThreadExecutor("IOConnect");
    }

    void start() {
        if (isConnected()) {
            logger.d("IO is already connected");
            return;
        }

        logger.d("IO connecting..");

        ioExecutor.execute(new Runnable() {
            public void run() {
                Map<String, String> headers = UserIQSDKInternal.getSDKConfig().getHeaders();
                wsTransport.setHeaders(headers);
                wsTransport.start();
            }
        });
    }

    void stop() {
        try {
            wsTransport.cancel();
        } catch (IOException e) { /* ignore */ }
    }

    void notify(final String method, final Object... args) {
        if (!isConnected()) {
            logger.e("WS not connected, can't notify: " + method, null);
            return;
        }

        jobExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    simpleRpc.notify(method, args);
                } catch (Exception e) {
                    logger.e(String.format("%s call failed on notify: %s ", method, e.getMessage() != null ? e.getMessage() : ""), e);
                }
            }
        });
    }

    boolean isConnected() {
        return wsTransport.isConnected();
    }


    interface ConnectListener {
        void onIOConnect(WS ws, Connection connection);
    }

    private final SimpleRPC simpleRpc;
    private static final Logger logger = Logger.init(IO.class.getSimpleName());
    private final WSTransport wsTransport;

    private final ThreadExecutor ioExecutor;
    private ThreadExecutor jobExecutor;
}
