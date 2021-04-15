package com.useriq.sdk.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by appunfold on 04/10/17.
 */

public class JSONUtil {
    public static Map<String, Object> toMap(JSONObject obj) throws JSONException {

        Map<String, Object> out = new HashMap<>();
        if (obj == null) return out;

        Iterator<?> keys = obj.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = obj.getString(key);
            out.put(key, value);
        }

        return out;
    }
}
