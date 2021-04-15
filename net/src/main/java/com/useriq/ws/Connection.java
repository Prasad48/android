package com.useriq.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.SocketFactory;

public final class Connection {
    private static final SecureRandom sRandom = new SecureRandom();
    private static final String PMCE = (
            "permessage-deflate; client_no_context_takeover; server_no_context_takeover"
    );
    private static final List<String> PMCE_EXTNS = Arrays.asList(PMCE.split("; "));
    public final Map<String, String> reqHdr;
    public final Map<String, String> resHdr;
    private final String host;
    private final int port;
    private final String path;
    private final String secret;
    public StatusLine status;
    public boolean canZip;

    public Connection(URI uri, Map<String, String> extraHeaders) {
        this.host = uri.getHost();
        boolean isSecure = (uri.getScheme().matches("(http|ws)s"));

        int port = uri.getPort();
        String portSuffix = "";

        if (port == -1) {
            this.port = (isSecure ? 443 : 80);
        } else {
            this.port = port;
            portSuffix = ":" + port;
        }

        String origin = (isSecure ? "https" : "http") + "://" + host + portSuffix;

        String path = uri.getPath();

        if (path == null || path.isEmpty())
            path = "/";

        if (uri.getQuery() != null)
            path += "?" + uri.getQuery();

        this.path = path;
        this.secret = generateWebSocketKey();

        reqHdr = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        reqHdr.putAll(extraHeaders);
        reqHdr.put("Upgrade", "websocket");
        reqHdr.put("Connection", "Upgrade");
        reqHdr.put("Host", host + portSuffix);
        reqHdr.put("Origin", origin);
        reqHdr.put("Sec-WebSocket-Key", secret);
        reqHdr.put("Sec-WebSocket-Version", "13");
        reqHdr.put("Sec-WebSocket-Extensions", PMCE);

        resHdr = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    private static String generateWebSocketKey() {
        byte[] data = new byte[16];
        sRandom.nextBytes(data); // "randomly selected"
        return Base64.encode(data); // "base64-encoded"
    }

    Socket perform(SocketFactory factory) throws WS.Error, IOException {
        Socket socket = factory.createSocket(host, port);
        writeRequestHeaders(socket);

        InputStream iStream = socket.getInputStream();
        this.status = new StatusLine(readLine(iStream));
        parseResponseHeaders(iStream, resHdr);

        // Read HTTP response status line.
        if (status.code != 101)
            throw new WS.Error(status.code + ": " + status.reason);

        validateResponseHeaders();
        checkPerMsgExtn();

        return socket;
    }

    private void writeRequestHeaders(Socket socket) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.print("GET " + path + " HTTP/1.1\r\n");
        out.print(getHeaderTxt(reqHdr));
        out.print("\r\n");
        out.flush();
    }

    // Can't use BufferedReader because it buffers past the HTTP data.
    private static String readLine(InputStream reader) throws IOException {
        int readChar = reader.read();
        if (readChar == -1) {
            return null;
        }
        StringBuilder string = new StringBuilder("");
        while (readChar != '\n') {
            if (readChar != '\r') {
                string.append((char) readChar);
            }

            readChar = reader.read();
            if (readChar == -1) {
                return null;
            }
        }
        return string.toString();
    }

    private static void parseResponseHeaders(InputStream iStream, Map<String, String> header) throws IOException {
        String line;
        StringBuilder builder = null;

        while (true) {
            line = readLine(iStream);

            // If the end of the header section was reached.
            if (line == null || line.length() == 0) {
                if (builder != null)
                    parseHttpHeader(header, builder.toString());
                break; // The end of the header section.
            }

            // The first char of the line.
            char ch = line.charAt(0);

            // If the first char is SP or HT.
            if (ch == ' ' || ch == '\t') {
                if (builder == null)
                    continue; // Weird. No preceding "field-name:field-value" line. Ignore this line.

                // Replacing the leading 1*(SP|HT) to a single SP.
                line = line.replaceAll("^[ \t]+", " ");

                // Concatenate
                builder.append(line);

                continue;
            }

            if (builder != null)
                parseHttpHeader(header, builder.toString());

            builder = new StringBuilder(line);
        }
    }

    private void validateResponseHeaders() throws WS.Error {
        String accepts = resHdr.get("Sec-WebSocket-Accept");
        if (accepts == null || accepts.isEmpty())
            throw new WS.Error("No Sec-WebSocket-Accept header.");
        else if (!accepts.equals(createSecretValidation(secret)))
            throw new WS.Error("Bad Sec-WebSocket-Accept header value.");

        String upgrade = resHdr.get("Upgrade");
        if (upgrade == null || upgrade.isEmpty())
            throw new WS.Error("No Upgrade header.");
        else if (!upgrade.equalsIgnoreCase("websocket"))
            throw new WS.Error("'Upgrade' header should equal 'websocket'");

        String connection = resHdr.get("Connection");
        if (connection == null || connection.isEmpty())
            throw new WS.Error("No Connection header.");
        else if (!connection.equalsIgnoreCase("upgrade"))
            throw new WS.Error("'Connection' header should equal 'Upgrade'");
    }

    private void checkPerMsgExtn() {
        String perMsgExtension = resHdr.get("Sec-WebSocket-Extensions");
        if (perMsgExtension == null || perMsgExtension.isEmpty()) return;

        String[] pmce = perMsgExtension.split(";");

        for (String val : pmce) {
            val = val.trim();
            List<String> params = Arrays.asList(val.split("[\\s;\\s]+"));
            if (params.containsAll(PMCE_EXTNS)) {
                this.canZip = true;
                return;
            }
        }

        // TODO: Do through validation
    }

    private static String getHeaderTxt(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String values = entry.getValue();
            sb.append(key).append(": ").append(values).append("\r\n");
        }
        return sb.toString();
    }

    private static void parseHttpHeader(Map<String, String> map, String header) {
        // Split 'header' to name & value.
        String[] pair = header.split(":", 2);

        if (pair.length < 2) // Weird. Ignore this header.
            return;

        map.put(pair[0].trim(), pair[1].trim());
    }

    private static String createSecretValidation(String secret) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update((secret + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes());
            String str = Base64.encode(md.digest());
            return str;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ""; // This never happens.
        }
    }

    public static final class StatusLine {
        public final String httpVer;
        public final int code;
        public final String reason;

        StatusLine(String line) {
            // HTTP-Version Status-Code Reason-Phrase
            String[] elements = line.split(" +", 3);

            if (elements.length < 2)
                throw new IllegalArgumentException();

            httpVer = elements[0];
            code = Integer.parseInt(elements[1]);
            reason = (elements.length == 3) ? elements[2] : null;
        }
    }
}
