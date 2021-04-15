package com.useriq;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sudhakar
 * @created 13-May-2018
 */

class RPCErrors {
    private static final String CODE = "code";
    private static final String NAME = "name";
    private static final String DESC = "desc";

    private RPCErrors() {
    }

    static Map<String, Object> noMethod(String method) {
        Map<String, Object> obj = new HashMap<>();

        obj.put(CODE, 1000);
        obj.put(NAME, "NO_METHOD");
        obj.put(DESC, "Unknown method '" + method + "' called on SimpleRPC");

        return obj;
    }

    static Map<String, Object> internalErr() {
        Map<String, Object> obj = new HashMap<>();

        obj.put(CODE, 1001);
        obj.put(NAME, "INTERNAL_ERROR");
        obj.put(DESC, "Something broke. Unexpected error happened");

        return obj;
    }

    static Map<String, Object> timeoutErr(String method) {
        Map<String, Object> obj = new HashMap<>();

        obj.put(CODE, 1002);
        obj.put(NAME, "TIMEOUT_ERROR");
        obj.put(DESC, "Timed out invoking method '" + method + "' on SimpleRPC");

        return obj;
    }
}
