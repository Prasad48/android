/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.useriq.sdk.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.util.DisplayMetrics;

import com.useriq.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Class containing some static utility methods.
 */
public class Utils {
    private static final Logger logger = Logger.init(Utils.class.getSimpleName());
    private static Boolean isTablet;
    private static int TEST_ID_KEY = -1;

    static {
        try {
            getRNTestIdKey();
        } catch (Exception e) {
            // Silence error; ignore
        }
    }

    private Utils() {
    }

    public static boolean isTablet(Activity activity) {
        if (isTablet != null)
            return isTablet;
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float yInches = metrics.heightPixels / metrics.ydpi;
        float xInches = metrics.widthPixels / metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
        if (diagonalInches >= 6.5) {
            // 6.5inch device or bigger
            isTablet = true;
        } else isTablet = false;
        return isTablet;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
    }

    public static void openPlayStoreLink(String appPackageName, Context context) {
        if (appPackageName == null) {
            logger.e("appPackageName is null", null);
            return;
        }
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void openUrl(String url, Context context) {
        if (url == null) {
            logger.e("url is null", null);
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        try {
            context.startActivity(i);
        } catch (Exception e) {
            logger.e("No activity found to handle url: " + url, e);
        }
    }

    public static boolean isReactNativeApp() {
        return TEST_ID_KEY != -1;
    }

    public static int getRNTestIdKey() {
        try {
            if(TEST_ID_KEY == -1) {
                TEST_ID_KEY = Class.forName("com.facebook.react.R$id").getField("react_test_id").getInt(null);
                return TEST_ID_KEY;
            } else {
                return TEST_ID_KEY;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    public static String readFileToString(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        char[] buffer = new char[1];
        while (reader.read(buffer) != -1) {
            stringBuilder.append(new String(buffer));
        }
        reader.close();


        return stringBuilder.toString();
    }

    public static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }

    /**
     * Creates directories inside app dir
     *
     * @param app
     * @return String
     */
    public static String mkdirs(Application app, String path) {
        try {
            File filesDir = app.getApplicationContext().getFilesDir();
            if (filesDir == null) return null;
            File assetDir = new File(filesDir.getAbsolutePath(), path);
            if (assetDir.mkdirs() || assetDir.isDirectory())
                return assetDir.getAbsolutePath();
            return null;
        } catch (Exception e) {
            logger.e("mkdirs failed: '" + path + "'", e);
            return null;
        }
    }


    /**
     * toColor extracts color with key from map which is of format [A, R, G, B]
     *
     * @param map map which has color key
     * @param key key in the map
     * @param defaults
     * @return @ColorInt
     */
    public static int toColor(Map<String, Object> map, String key, @ColorInt int defaults) {
        if (map == null || !map.containsKey(key)) return defaults;

        List<Number> colorList = (List<Number>) map.get(key);

        return Color.argb(
                colorList.get(0).intValue(),
                colorList.get(1).intValue(),
                colorList.get(2).intValue(),
                colorList.get(3).intValue()
        );
    }

    public static int toInt(Map<String, Object> map, String key, int defaults) {
        if (map != null && map.containsKey(key)) {
            Object obj = map.get(key);
            return obj == null ? defaults : ((Number) obj).intValue();
        }

        return defaults;
    }

    public static long toLong(Map<String, Object> map, String key, long defaults) {
        if (map != null && map.containsKey(key)) {
            Object obj = map.get(key);
            return obj == null ? defaults : ((Number) obj).longValue();
        }

        return defaults;
    }
}
