package com.useriq.sdk.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SharedPrefUtil {
    private static final String DEFAULT_SHARED_PREF = "UserIQSharedPrefa";

    public static void saveMap(Application application, String key, Map<String, Object> valueMap) {
        SharedPreferences sharedPref = application.getSharedPreferences(DEFAULT_SHARED_PREF, Context.MODE_PRIVATE);
        if (sharedPref != null) {
            JSONObject jsonObject = new JSONObject(valueMap);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(key, jsonString);
            editor.apply();
        }
    }

    public static Map<String, Object> loadMap(Application application, String key) {
        SharedPreferences sharedPref = application.getSharedPreferences(key, Context.MODE_PRIVATE);
        if (!sharedPref.contains(key)) {
            return null;
        }
        Map<String, Object> outputMap=null;
        try {
            String jsonString = sharedPref.getString(key, "{}");
            JSONObject jsonObject = new JSONObject(jsonString);
            outputMap = getMap(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputMap;
    }

    private static Map<String, Object> getMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> outputMap = new HashMap<>();
        Iterator<String> keysItr = jsonObject.keys();
        while (keysItr.hasNext()) {
            String k = keysItr.next();
            Object value = jsonObject.get(k);
            if (value instanceof JSONObject) {
                outputMap.put(k, getMap((JSONObject) value));
            } else {
                outputMap.put(k, value);
            }
        }
        return outputMap;
    }
}
