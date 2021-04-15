package com.useriq.sdk;

import android.content.SharedPreferences;
import android.support.annotation.IntDef;

import com.useriq.Logger;
import com.useriq.sdk.models.Screen;
import com.useriq.sdk.models.SyncData;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class AnalyticsManager {
    private final static Logger logger = Logger.init(AnalyticsManager.class.getSimpleName());

    final static int APP_ENTER = 1;
    final static int APP_EXIT = 2;
    final static int SCREEN_ENTER = 3;
    final static int SCREEN_EXIT = 4;
    final static int TAP = 5;
    final static int MODAL_SHOWN = 11;
    final static int MODAL_DISMISSED = 12;
    final static int MODAL_BTN_TAP = 13;
    final static int FAB_CLICK = 14;
    final static int CTX_HELP_ENTER = 15;
    final static int CTX_HELP_EXIT = 16;
    final static int WT_START = 17;
    final static int WT_STEP = 18;
    final static int WT_STOP = 19;
    final static int HELP_CENTER_ENTER = 20;
    final static int HELP_CENTER_EXIT = 21;
    final static int HELP_DETAIL_ENTER = 22;
    final static int HELP_DETAIL_EXIT = 23;
    final static int QUESTION_SEARCH = 24;
    final static int TOUR_START = 25;
    final static int TOUR_STEP = 26;
    final static int TOUR_STOP = 27;
    final static int NPSSubmit = 28;
    final static int RatingSubmit = 29;

    private static final String CACHE_SIZE = "cacheSize";
    private static final String TS = "ts";
    public static final String TZ = "tzOffset";
    private static final String EVT = "evt";
    private static final String SCREEN_ID = "screenId";
    private static final String NODE_ID = "nodeId";
    private static final String VALUE = "value";
    private static final String RESPONSE = "response";
    private static final String ID = "id";
    private static final String QID = "qId";
    private static final String ON_EMU_EVENT = "onEMUEvent";

    private final AnalyticsCache cache;
    private IO io;
    private final SDKConfig sdkConfig;
    private final SyncData syncData;
    private final SharedPreferences uiqPrefs;
    private List<Map> sdkEvents;
    private int cacheSize = 0;

    AnalyticsManager(IO io, SDKConfig sdkConfig, SyncData syncData, String assetDir, SharedPreferences uiqPrefs) {
        this.io = io;
        this.sdkConfig = sdkConfig;
        this.syncData = syncData;
        this.uiqPrefs = uiqPrefs;
        this.sdkEvents = new ArrayList<>();
        this.cache = new AnalyticsCache(this, assetDir);
    }

    public static int getTimeZoneOffsetValue(){
        Calendar calendar = new GregorianCalendar();
        TimeZone timeZone = calendar.getTimeZone();
        int offsetMinutes = -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET))/(1000*60);
        return offsetMinutes;
    }

    void restore() {
        cache.init(true);
        cacheSize = uiqPrefs.getInt(CACHE_SIZE, cacheSize);
    }

    void save() {
        cache.save(sdkEvents);
        int totalSize = cacheSize + sdkEvents.size();
        uiqPrefs.edit().putInt(CACHE_SIZE, totalSize).apply();
        sdkEvents = new ArrayList<>();
    }

    void onAppEnter() {
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, APP_ENTER);
        trackEvent(event);
    }

    void onScreenEnter(String screenId) {
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, SCREEN_ENTER);
        event.put(SCREEN_ID, screenId);
        trackEvent(event);
    }

    void onScreenExit(String screenId) {
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, SCREEN_EXIT);
        event.put(SCREEN_ID, screenId);
        trackEvent(event);
    }

    void onClick(String elId) {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, TAP);
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        event.put("eleId", elId);
        trackEvent(event);
    }

    public void onModalBtnClick(String id, String buttonId) {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, MODAL_BTN_TAP);
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        event.put(ID, id);
        event.put(NODE_ID, buttonId);
        trackEvent(event);
    }

    public void onModalVisible(String modalId, boolean isVisible) {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, isVisible ? MODAL_SHOWN : MODAL_DISMISSED);
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        event.put(ID, modalId);
        trackEvent(event);
    }

    public void onNpsSubmit(String modalId, String value, String response) {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, NPSSubmit);
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        event.put(VALUE, value);
        event.put(RESPONSE, response);
        event.put(ID, modalId);
        trackEvent(event);
    }

    public void onRatingSubmit(String modalId, String value, String response) {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, RatingSubmit);
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        event.put(VALUE, value);
        event.put(RESPONSE, response);
        event.put(ID, modalId);
        trackEvent(event);
    }

    public void onFabClick() {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, FAB_CLICK);
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        trackEvent(event);
    }

    public void onCtxHelp(boolean isEnter) {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, isEnter ? CTX_HELP_ENTER : CTX_HELP_EXIT);
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        trackEvent(event);
    }

    public void onWT(String wtId, String stepId, boolean isStart, boolean isFinish, int stepNo) {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, isStart ? WT_START : WT_STOP);
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        event.put("wtId", wtId);
        event.put("stepNo", stepNo);
        event.put("stepId", stepId);
        event.put("finished", isFinish);
        logger.d("onWT: " + "wtId->" + wtId + " stepId->" + stepId + " startingWalkthrough->" + isStart);
        trackEvent(event);
    }

    public void onWTStep(String wtId, String stepId, int stepNo) {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, WT_STEP);
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        event.put("wtId", wtId);
        event.put("stepId", stepId);
        event.put("stepNo", stepNo);
        logger.d("onWTStep: " + "wtId->" + wtId + " stepId->" + stepId);
        trackEvent(event);
    }

    public void onHelpCenter(boolean isEnter) {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, isEnter ? HELP_CENTER_ENTER : HELP_CENTER_EXIT);
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        trackEvent(event);
    }

    public void onHelpDetail(String qID, boolean isEnter) {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, isEnter ? HELP_DETAIL_ENTER : HELP_DETAIL_EXIT);
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        event.put(QID, qID);
        trackEvent(event);
    }

    public void onQuestionSearch(String query) {
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, QUESTION_SEARCH);
        event.put("query", query);
        trackEvent(event);
    }

    public void onTour(String tourId, String stepId, boolean isStart) {
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, isStart ? TOUR_START : TOUR_STOP);
        event.put("tourId", tourId);
        event.put("stepId", stepId);
        trackEvent(event);
    }

    public void onTourStep(String tourId, String stepId) {
        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(EVT, TOUR_STEP);
        event.put("tourId", tourId);
        event.put("stepId", stepId);
        trackEvent(event);
    }

    public void onPref(long startNs, String source, String ...kvPairs) {
        if (!syncData.trackPerformance || !io.isConnected()) return;

        double elapsed = Math.round((System.nanoTime() - startNs) / (1000)) / (1000.0); // 3 decimals
        Screen currScreen = UserIQSDKInternal.getCurrScreen();

        Map<String, Object> event = new HashMap<>();
        event.put(TS, System.currentTimeMillis());
        event.put(TZ, getTimeZoneOffsetValue());
        event.put(SCREEN_ID, currScreen != null ? currScreen.id : null);
        event.put("elapsed", elapsed);
        event.put("source", source);

        int i = 0, length = kvPairs.length - 1;
        while (i < length) {
            String key = kvPairs[i];
            String val = kvPairs[++i];
            event.put(key, val);
            i++;
        }

        io.notify("onPrefEvent", event);
    }

    public void onEmuEvent(@EMU_EVENT_KIND int kind) {
        if (!io.isConnected()) return;

        Map<String, Object> emuEvent = new HashMap<>();
        emuEvent.put("kind", kind);
        io.notify(ON_EMU_EVENT, emuEvent);
    }

    private void trackEvent(Map event) {
        sdkEvents.add(event);

        if (!io.isConnected()) {
            logger.d("IO is not connected hence ignoring the events");
            return;
        }

        long size = cacheSize + sdkEvents.size();
        if (size >= syncData.eventOpts.buffSize) {
            if (!cache.isFlushed) {
                cache.flush();
                uiqPrefs.edit().putInt(CACHE_SIZE, cacheSize).apply();
                cacheSize = 0;
            }
            sendSDKEvents(sdkEvents);
            sdkEvents = new ArrayList<>();
        }
    }

    public void onRotationEvent(int orientation) {
        io.notify("onRotation", orientation);
    }

    private void sendSDKEvents(List<Map> eventList) {
        Map<String, Object> events = new HashMap<>();
        events.put(TS, System.currentTimeMillis());
        events.put(TZ, getTimeZoneOffsetValue());
        events.put("events", eventList);
        io.notify("onSDKEvent", events);
    }

    private static class AnalyticsCache {
        private final AnalyticsManager manager;
        private final String assetDir;
        private int BATCH_SIZE = 1000;

        private boolean isFlushed = false;
        private BufferedReader reader;
        private BufferedWriter writer;

        AnalyticsCache(AnalyticsManager manager, String assetDir) {
            this.manager = manager;
            this.assetDir = assetDir;
            init(true);
        }

        void init(boolean append) {
            try {
                File cacheFile = new File(assetDir, "events.json");
                writer = new BufferedWriter(new FileWriter(cacheFile, append));
                reader = new BufferedReader(new FileReader(cacheFile));
            } catch (Exception e) {
                logger.e("Analytics events cache disabled", e);
            }
        }

        void flush() {
            try {
                while (reader.ready()) {
                    List<Map> eventList = new ArrayList<>();
                    for (int i = 0; i < BATCH_SIZE; i++) {
                        String line = reader.readLine();
                        if (line == null) break;
                        eventList.add(JSON.toMap(new JSONObject(line)));
                    }
                    manager.sendSDKEvents(eventList);
                }
                isFlushed = true;
                writer.close();
                init(false);
            } catch (Exception e) {
                logger.d("AnalyticsCache flush failed");
            }
        }

        /**
         * save saves events separated by new lines
         *
         * @param sdkEvents
         */
        void save(List<Map> sdkEvents) {
            try {
                for (Map event : sdkEvents) {
                    JSONObject json = new JSONObject(event);
                    writer.write(json.toString());
                    writer.newLine();
                }
                writer.flush();
                writer.close();
                reader.close();
            } catch (IOException e) {
                logger.d("AnalyticsCache save failed");
            }

            isFlushed = false;
        }
    }


    public static final int APP_PAUSE = 1;
    public static final int APP_RESUME = 2;

    @Retention(SOURCE)
    @IntDef({APP_PAUSE, APP_RESUME})
    public @interface EMU_EVENT_KIND {
    }

}
