package com.useriq.sdk;

import com.useriq.sdk.util.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Http.Request req = new Http.Request("GET", "")
 * .query("", "")
 * .header("", "")
 * .body(new MultiPart().add("asset", new File()))
 * .onProgress(new ProgressCb() {
 * public void progress(Http req, int percent) {}
 * });
 * <p>
 * req.execute(new HttpCb() {
 * public void response(Http req, Response res, Exception e) {}
 * })
 * </p>
 * Created by smylsamy on 03/09/16.
 */

public class Http {
    public static final String UTF_8 = "UTF-8";
    static final int bufferSize = 1024 * 4;

    public interface HttpCb {
        void response(Request req, Response res, Exception e);
    }

    public interface StreamCb extends HttpCb {
        void onBytes(byte[] bytes);
    }

    public interface ProgressCb {
        void progress(Request http, int totalRead, int totalAvailable, int percent);
    }

    public static class Request {
        private final String method;
        private final String uri;
        private final Map<String, String> query = new HashMap<>();
        private final Map<String, String> header = new HashMap<>();

        private HttpCb httpCb;
        private ProgressCb progressCb;
        private byte[] body;

        public Request(String method, String uri) {
            this.method = method;
            this.uri = uri;
        }

        public Request query(String key, String value) {
            query.put(key, value);
            return this;
        }

        public Request body(JSONObject json) {
            body(json.toString());
            header("Content-Type", "application/json");
            return this;
        }

        public Request body(String textBody) {
            if (textBody == null) {
                body = null;
                return this;
            }
            header("Content-Type", "text/plain");
            try {
                body = textBody.getBytes(UTF_8);
            } catch (UnsupportedEncodingException e) { /* Should never happen */ }
            return this;
        }

        public Request header(String key, String value) {
            header.put(key, value);
            return this;
        }

        public Request body(MultiPart multiPart) {
            body(multiPart.data());
            header("Content-Type", String.format("multipart/form-data; boundary=%s", multiPart.boundry));
            return this;
        }

        public Request body(byte[] rawBody) {
            if (rawBody == null) {
                body = null;
                return this;
            }
            body = rawBody;
            return this;
        }

        public void onProgress(ProgressCb progressCb) {
            this.progressCb = progressCb;
        }

        public void execute(ThreadExecutor jobExecutor, HttpCb cb) {
            this.httpCb = cb;
            execute(jobExecutor);
        }

        public void execute(ThreadExecutor jobExecutor) {
            jobExecutor.execute(new RequestTask(this));
        }

        public Response execute() throws Exception {
            ResponseCallable responseCallable = new ResponseCallable(new RequestTask(this));
            this.httpCb = responseCallable;
            return responseCallable.call();
        }

        public void stream(ThreadExecutor jobExecutor, StreamCb cb) {
            this.httpCb = cb;
            execute(jobExecutor);
        }

        private String getQueryString() {
            if (query.isEmpty()) return "";

            final StringBuilder result = new StringBuilder("?");

            for (Map.Entry<String, String> entry : query.entrySet()) {
                try {
                    result.append(URLEncoder.encode(entry.getKey(), UTF_8));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), UTF_8));
                } catch (Exception e) { /* This should never happen */ }
            }
            return result.toString();
        }

        private void fireProgress(final int totalRead, final int totalAvailable) {
            if (progressCb == null) return;
            final int percent = (int) (((float) totalRead / (float) totalAvailable) * 100f);
            progressCb.progress(Request.this, totalRead, totalAvailable, percent);
        }

        void sendResponse(final Response resp, final Exception e) {
            if (httpCb != null)
                httpCb.response(Request.this, resp, e);
            else if (e != null)
                e.printStackTrace();
        }
    }

    static class ResponseCallable implements HttpCb, Callable<Response> {
        private Response response;
        private Exception e;
        final CountDownLatch latch = new CountDownLatch(1);
        private final RequestTask requestTask;

        ResponseCallable(RequestTask requestTask) {
            this.requestTask = requestTask;
        }

        @Override
        public void response(Request req, Response res, Exception e) {
            this.response = res;
            this.e = e;
            latch.countDown();
        }

        @Override
        public Response call() throws Exception {
            requestTask.run();
            latch.await();
            if (e != null) throw e;
            return response;
        }
    }

    static class RequestTask implements Runnable {
        private final Request req;

        public RequestTask(Request req) {
            this.req = req;
        }

        @Override
        public void run() {
            try {
                perform();
            } catch (IOException e) {
                req.sendResponse(null, e);
                e.printStackTrace();
            }
        }

        private void perform() throws IOException {
            URL url = new URL(req.uri + req.getQueryString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(req.method);
            conn.setConnectTimeout(120000);
            conn.setDoInput(true);

            for (Map.Entry<String, String> hdr : req.header.entrySet()) {
                conn.setRequestProperty(hdr.getKey(), hdr.getValue());
            }

            if (req.body != null) {
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(req.body);
            }
            conn.connect();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int status = conn.getResponseCode();
            String message = conn.getResponseMessage();
            TreeMap<String, List<String>> respHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            Map<String, List<String>> headerFields = new HashMap<>(conn.getHeaderFields());
            headerFields.remove(null); // null values are not allowed in TreeMap
            respHeaders.putAll(headerFields);
            InputStream inpStream = (status >= 200 && status < 400)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            int totalAvailable = respHeaders.containsKey("Content-Length")
                    ? Integer.parseInt(respHeaders.get("Content-Length").get(0))
                    : -1;

            if (totalAvailable != -1)
                req.fireProgress(0, totalAvailable);

            int read, totalRead = 0;
            byte[] buf = new byte[bufferSize];
            StreamCb streamCb = null;

            if (req.httpCb instanceof StreamCb)
                streamCb = (StreamCb) req.httpCb;

            while ((read = inpStream.read(buf)) != -1) {
                // Streaming(Partial download) doesn't download image properly.
                //if(streamCb != null) streamCb.onBytes(buf);
                bos.write(buf, 0, read);
                totalRead += read;
                if (totalAvailable != -1)
                    req.fireProgress(totalRead, totalAvailable);
            }
            if (totalAvailable != -1)
                req.fireProgress(totalAvailable, totalAvailable);

            Response resp = new Response(bos.toByteArray(), status, message, respHeaders);
            req.sendResponse(resp, null);
            conn.disconnect();
        }
    }

    public static class Response {
        public final byte[] data;
        public final int status;
        public final String message;
        public final Map<String, List<String>> respHeaders;

        public Response(byte[] data, int status, String message, Map<String, List<String>> respHeaders) {
            this.data = data;
            this.status = status;
            this.message = message;
            this.respHeaders = respHeaders;
        }

        public JSONObject asJSONObject() throws JSONException {
            String str = asString();
            return str.isEmpty() ? new JSONObject() : new JSONObject(str);
        }

        public String asString() {
            try {
                if (data == null || data.length == 0) return "";
                return new String(data, UTF_8);
            } catch (UnsupportedEncodingException e) { /* Should never happen */ }
            return "";
        }
    }

    public static class MultiPart {
        final String boundry;
        private final String encoding;
        private final byte[] LINE_FEED = "\r\n".getBytes();
        private ByteArrayOutputStream bos;
        private boolean singlePart = true;

        public MultiPart() {
            this.boundry = "------" + System.currentTimeMillis() + "------";
            this.bos = new ByteArrayOutputStream();
            this.encoding = UTF_8;
        }

        MultiPart add(String fieldName, File file) throws IOException {
            String contentType = URLConnection.guessContentTypeFromName(file.getName());
            add(fieldName, file.getName(), new FileInputStream(file), contentType);
            return this;
        }

        MultiPart add(String fieldName, String fileName, InputStream ins, String contentType) throws IOException {
            if (bos == null) throw new IllegalStateException("Multipart already consumed");

            if (singlePart) singlePart = false;
            else bos.write(LINE_FEED);

            bos.write(("--" + boundry).getBytes());
            bos.write(LINE_FEED);
            bos.write(("Content-Disposition: form-data; name=\"" + fieldName
                    + "\"; filename=\"" + fileName + "\"").getBytes());
            bos.write(LINE_FEED);
            bos.write(("Content-Type: " + contentType).getBytes());
            bos.write(LINE_FEED);
            bos.write("Content-Transfer-Encoding: binary".getBytes());
            bos.write(LINE_FEED);
            bos.write(LINE_FEED);
            writeBytes(ins, bos);

            return this;
        }

        void writeBytes(InputStream ins, ByteArrayOutputStream bos) throws IOException {
            try {
                byte[] buffer = new byte[bufferSize];
                int read;
                while ((read = ins.read(buffer)) != -1) {
                    bos.write(buffer, 0, read);
                }
            } finally {
                NetworkUtil.closeQuietly(ins);
            }
        }

        MultiPart add(String fieldName, String fileName, InputStream ins) throws IOException {
            String contentType = URLConnection.guessContentTypeFromStream(ins);
            add(fieldName, fileName, ins, contentType);
            return this;
        }

        byte[] data() {
            final byte[] data = bos.toByteArray();
            NetworkUtil.closeQuietly(bos);
            bos = null;
            return data;
        }
    }
}
