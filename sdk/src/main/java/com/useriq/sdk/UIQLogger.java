package com.useriq.sdk;

import android.util.Log;

import com.useriq.Logger;
import com.useriq.Logger.Level;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static com.useriq.sdk.UserIQSDK.TAG;

/**
 * @author sudhakar
 * @created 03-Oct-2018
 */
public class UIQLogger {
    static void setup(final IO io, SDKConfig sdkConfig) {
        Logger.Printer logcatPrinter = new Logger.Printer() {
            @Override
            public void print(Level level, String tag, String msg, Throwable throwable) {
                if (level == Level.DEBUG) {
                    Log.d(TAG, tag + ": " + msg);
                } else if (level == Level.INFO) {
                    Log.i(TAG, tag + ": " + msg);
                } else {
                    Log.e(TAG, tag + ": " + msg, throwable);
                }
            }
        };

        Logger.Printer remotePrinter = new Logger.Printer() {
            @Override
            public void print(Level level, String tag, String msg, Throwable throwable) {
                if (!io.isConnected()) {
                    // Silently ignore. Its assumed that other logger can
                    // still log the message to the console
                    return;
                }
                int myLevel = level.ordinal() + 1;
                if (UserIQSDKInternal.getSyncData().logLevel.ordinal() <= level.ordinal()) {
                    Map<String, Object> entry = new HashMap<>();
                    String data = tag + ": " + msg;

                    if (throwable != null) {
                        StringWriter errors = new StringWriter();
                        throwable.printStackTrace(new PrintWriter(errors));
                        data += "\n" + errors.toString();
                    }

                    entry.put("ts", System.currentTimeMillis());
                    entry.put(AnalyticsManager.TZ,AnalyticsManager.getTimeZoneOffsetValue());
                    entry.put("source", 1);
                    entry.put("level", myLevel);
                    entry.put("data", data);
                    io.notify("onLog", entry);
                }
            }
        };

        Logger.setLevel(BuildConfig.DEBUG ? Level.DEBUG : Level.ERROR);

        Logger.clear();
        Logger.addPrinters(logcatPrinter, remotePrinter);
    }
}
