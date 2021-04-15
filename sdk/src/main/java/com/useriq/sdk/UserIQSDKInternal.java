package com.useriq.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.useriq.Logger;
import com.useriq.sdk.models.Screen;
import com.useriq.sdk.models.SyncData;

import java.io.File;
import java.util.Map;

/**
 * UserIQSDKInternal is a main class that got initialized on initialization of SDK. It handles the initialization of all required components.
 */

public final class UserIQSDKInternal {

    /***
     * UserIQSDKInternal takes care of initialization of SDK along with the sync, if manual sync is not enabled
     *
     */
    private UserIQSDKInternal(Application app, String apiKey, Map<String, Object> user, String emuId, String sdkServerUrl, Context context, Activity currActivity, boolean fabDisabled) {
        UserIQSDKInternal.instance = this;

        this.app = app;
        this.context = context;
        this.sdkConfig = new SDKConfig(app, apiKey, user, emuId, sdkServerUrl, fabDisabled);
        this.activityTracker = new ActivityTracker(currActivity);
        this.sdkManager = new SDKManager(app, sdkConfig, activityTracker);

        if (!mSdkDisabled) sdkManager.activate();
    }

    public static UserIQSDKInternal init(
            Application application,
            String apiKey,
            Map<String, Object> user,
            String emuId,
            String sdkServerUrl,
            Context context,
            Activity currentActivity,
            boolean fabDisabled) {
//        if (sdkServerUrl == null) {
//            sdkServerUrl = BuildConfig.SDK_SERVER_URL;
//        }
        if (application == null && currentActivity == null) {
            logger.e("both activity and application can't be null", null);
            return null;
        } else if (application == null) {
            application = currentActivity.getApplication();
        }

        if (instance == null)
            instance = new UserIQSDKInternal(application, apiKey, user, emuId, sdkServerUrl, context, currentActivity, fabDisabled);

        instance.sdkConfig.user = user;

        instance.sdkManager.attachRootView(false);

        return instance;
    }

    public static UserIQSDKInternal getInstance() {
        return instance;
    }

    public static AnalyticsManager getAnalyticsManager() {
        if (instance.sdkManager != null) {
            return instance.sdkManager.getAnalyticsManager();
        } else {
            return null;
        }
    }

    public static SDKConfig getSDKConfig() {
        return instance.sdkConfig;
    }

    void updateUserInSdkConfig(Map<String, Object> user) {
        instance.sdkConfig.user = user;
        instance.sdkManager.updateUser(user);
    }

    Map<String, Object> getSDKUser() {
        return sdkConfig.user;
    }

    public static SyncData getSyncData() {
        return instance.sdkManager.getSyncData();
    }

    /**
     * Required when sdk is initialized through vnc
     *
     * @return the context from which resources has to be loaded when sdk is initialized by hooking into the app
     */
    public static Context getContext() {
        if (instance.context != null) return instance.context;
        return getApp();
    }

    public static Application getApp() {
        if (instance != null) return instance.app;
        return null;
    }

    public static ScreenTracker getScreenTracker() {
        return instance.sdkManager.getScreenTracker();
    }

    public Resources getResources() {
        return getContext().getResources();
    }

    public File getAsset(String assetId) {
        return sdkManager.getAssetManager().getAsset(assetId);
    }

    public void onReactNativePause() {
        if (instance != null)
            instance.sdkManager.onReactNativePause();
    }

    public void onReactNativeResume() {
        if (instance != null)
            instance.sdkManager.onReactNativeResume();
    }

    public void onReactEvent(String eventName, int viewTag) {
        if (instance != null)
            instance.sdkManager.onReactEvent(eventName, viewTag);
    }

    public static int[] getScreenXY() {
        if (screenXY == null) {
            screenXY = new int[2];
            DisplayMetrics displayMetrics = new DisplayMetrics();
            UserIQSDKInternal.getCurrActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            screenXY[0] = displayMetrics.heightPixels;
            screenXY[1] = displayMetrics.widthPixels;
        }
        return screenXY;
    }

    public static Screen getCurrScreen() {
        return instance.sdkManager.getCurrScreen();
    }

    public static Activity getCurrActivity() {
        return instance.activityTracker.getCurrentActivity();
    }

    boolean isFABEnabled() {
        return sdkConfig.fabDisabled || getSyncData().fabEnabled;
    }

    private static final Logger logger = Logger.init(UserIQSDKInternal.class.getSimpleName());
    private static UserIQSDKInternal instance;
    private final ActivityTracker activityTracker;
    private final ISDKManager sdkManager;
    private Context context;
    private final Application app;
    private SDKConfig sdkConfig;
    private static boolean mSdkDisabled = false;
    private static int[] screenXY;

    interface ISDKManager {
        void activate();

        void deactivate();

        void attachRootView(boolean force);

        void onReactNativePause();

        void onReactNativeResume();

        void onReactEvent(String eventName, int tag);

        AssetManager getAssetManager();

        AnalyticsManager getAnalyticsManager();

        void updateUser(Map<String, Object> user);

        SyncData getSyncData();

        Screen getCurrScreen();

        ScreenTracker getScreenTracker();
    }
}
