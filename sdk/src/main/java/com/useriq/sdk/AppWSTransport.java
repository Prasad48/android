package com.useriq.sdk;

import com.useriq.Logger;
import com.useriq.WSTransport;
import com.useriq.ws.Connection;
import com.useriq.ws.WS;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

class AppWSTransport extends WSTransport {
    private final IO.ConnectListener listener;
    private static final Logger logger = Logger.init(AppWSTransport.class.getSimpleName());

    AppWSTransport(IO.ConnectListener listener) {
        this.listener = listener;
    }

    @Override
    public void onConnect(WS ws, Connection connection) {
        if (listener != null) listener.onIOConnect(ws, connection);
    }

    @Override
    public URI getHost() throws Exception {
        SDKConfig cfg = UserIQSDKInternal.getSDKConfig();
        if (cfg.url != null) return URI.create(cfg.url);

        Http.Request request = new Http.Request("GET", BuildConfig.SDK_SERVER_URL);
        Map<String, String> headers = cfg.getHeaders();
        for (Map.Entry<String, String> hdr : headers.entrySet()) {
            request.header(hdr.getKey(), hdr.getValue());
        }

        try {
            Http.Response response = request.execute();
            String responseJson = new String(response.data);
            JSONObject jsonObject = new JSONObject(responseJson);
            String host = jsonObject.getString("host");
            logger.d("WS_HOST: " + host);
            return new URI(host);
        } catch (Exception e) {
            logger.e("getHost()", e);

            // rethrow as IO so that retry logic will be used
            if (e instanceof URISyntaxException) {
                throw new IOException(e);
            }

            throw e;
        }
    }
}
