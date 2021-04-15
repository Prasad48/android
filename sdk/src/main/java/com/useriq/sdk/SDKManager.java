package com.useriq.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.useriq.Logger;
import com.useriq.SimpleRPC;
import com.useriq.sdk.AssetManager.ProgressListener;
import com.useriq.sdk.SDKService.SyncListener;
import com.useriq.sdk.capture.ViewRoot;
import com.useriq.sdk.ctxHelp.FABCtrl;
import com.useriq.sdk.models.Screen;
import com.useriq.sdk.models.SyncData;
import com.useriq.sdk.util.Utils;
import com.useriq.ws.Connection;
import com.useriq.ws.WS;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.useriq.sdk.models.SyncData.ACTIVATE;
import static com.useriq.sdk.models.SyncData.ASSETS;
import static com.useriq.sdk.models.SyncData.FAB_ENABLED;
import static com.useriq.sdk.models.SyncData.LOG_LEVEL;
import static com.useriq.sdk.models.SyncData.SCREENS;
import static com.useriq.sdk.models.SyncData.SDK_ENABLED;

/**
 * @author sudhakar
 * @created 25-Sep-2018
 */
class SDKManager implements UserIQSDKInternal.ISDKManager {

    SDKManager(Application app, final SDKConfig sdkConfig, ActivityTracker activityTracker) {
        this.sdkConfig = sdkConfig;
        this.activityTracker = activityTracker;

        this.sdkService = new SDKService(activityTracker, new SyncListener() {
            public void onSync(Map data) {
                onSyncApp(data);
            }

            public void onActivate(final String type, final String value) {
                syncData.setIsSoftActivate(false);
                uiThreadHandler.post(new Runnable() {
                    public void run() {
                        uiManager.schedule(type, value);
                    }
                });
            }
        });

        this.io = new IO(sdkService, jobExecutor, ioConnectListener);

        String uiqDir = Utils.mkdirs(app, SyncDataCache.UIQ_DIR);
        SharedPreferences uiqPrefs = app.getSharedPreferences("UIQPrefs", Context.MODE_PRIVATE);
        this.syncCache = new SyncDataCache(uiqDir);
        this.analyticsManager = new AnalyticsManager(io, sdkConfig, syncData, uiqDir, uiqPrefs);
        this.screenManager = new ScreenManager(activityTracker, analyticsManager);

        this.rnManager = new RNManager(analyticsManager);
        this.rnChangeStrategy = new RNChangeStrategy(screenManager);

        String assetFolder = Utils.mkdirs(app, ASSET_DIR);
        this.assetManager = new AssetManager(assetFolder);
        this.assetManager.setListener(new ProgressListener() {
            public void onProgress(int percent) {
            }

            public void onFinish(int failed) {
                logger.d("assetManager.onFinish(): Applying SyncChanges");
                syncChangeSet.remove(ASSETS);
                uiThreadHandler.post(applySyncChanges);
            }
        });

        this.uiManager = UIManager.getInstance();

        UIQLogger.setup(io, sdkConfig);
        this.analyticsManager.onAppEnter();
    }

    public void activate() {
        jobExecutor.execute(bootstrap);
        this.activityTracker.addCallback(trackerCb);
        activityTracker.register(UserIQSDKInternal.getApp());
    }

    public void deactivate() {
        if (!sdkConfig.isEmu()) io.stop();
        this.activityTracker.removeCallback(trackerCb);
        if (UserIQSDKInternal.getApp() != null) {
            activityTracker.unregister(UserIQSDKInternal.getApp());
        }
        screenManager.reset();
        ElementTracker.getInstance().reset();
        if (UserIQSDKInternal.getCurrActivity() != null) {
            final View view = UserIQSDKInternal.getCurrActivity().getWindow().getDecorView().getRootView();
            if (view instanceof ViewGroup) {
                UserIQSDKInternal.getCurrActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ViewGroup) view).removeView(UIManager.getInstance().getUiRootView());
                    }
                });
            } else {
                UIManager.getInstance().getUiRootView().removeAllViews();
            }
        }
        UIManager.getInstance().stop();
        UIRouter.getInstance().clear();
    }

    public void updateUser(Map<String, Object> user) {
        sdkConfig.user = user;
        jobExecutor.execute(new Runnable() {
            @Override
            public void run() {
                logger.d("onUpdateUser: " + sdkConfig.user);
                io.notify("onUpdateUser", sdkConfig.user);
            }
        });
    }

    @Override
    public void attachRootView(boolean force) {
        final Activity currActivity = activityTracker.getCurrentActivity();
        if (currActivity == null) {
            return;
        }

        final UIRootView uiRootView = uiManager.getUiRootView();

        if (uiRootView != null) {
            boolean attachedToWindow = ViewUtils.isAttachedToWindow(uiRootView);
            if (!attachedToWindow || force) {
                currActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uiRootView.attach(currActivity, ViewRoot.NONE);
                    }
                });
            }
        }
    }

    public void onReactNativeResume() {
        screenManager.checkScreenChange(TAG + ": onReactNativeResume");

        Activity currActivity = activityTracker.getCurrentActivity();
        if (currActivity != null) {
            ViewGroup rootView = (ViewGroup) currActivity.getWindow().getDecorView().getRootView();
            this.rnChangeStrategy.install(rootView);
        }
    }

    public void onReactNativePause() {
        this.rnChangeStrategy.uninstall();
    }

    public void onReactEvent(String eventName, int viewTag) {
        Screen currScreen = screenManager.getScreenTracker().getCurrScreen();
        Activity currentActivity = activityTracker.getCurrentActivity();
        rnManager.onReactEvent(currentActivity, eventName, viewTag, currScreen);
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public AnalyticsManager getAnalyticsManager() {
        return analyticsManager;
    }

    public SyncData getSyncData() {
        return syncData;
    }

    @Override
    public Screen getCurrScreen() {
        return screenManager.getScreenTracker().getCurrScreen();
    }

    @Override
    public ScreenTracker getScreenTracker() {
        return this.screenManager.getScreenTracker();
    }

    public void addScreenCallback(ScreenTracker.Callback cb) {
        this.screenManager.getScreenTracker().addCallback(cb);
    }

    public void removeScreenCallBack(ScreenTracker.Callback cb) {
        this.screenManager.getScreenTracker().removeCallBack(cb);
    }

    private void onSyncApp(final @NonNull Map<String, Object> data) {
        jobExecutor.execute(new Runnable() {
            public void run() {

                syncChangeSet.addAll(data.keySet());

                if (data.containsKey(SDK_ENABLED)) {
                    boolean isEnable = (boolean) data.get(SDK_ENABLED);
                    if (isEnable) {
                        activate();
                    } else {
                        deactivate();
                        return;
                    }
                }

                try {
                    syncData.update(data);
                } catch (Exception e) {
                    logger.e("SyncData.update()", e);
                }

                if (data.containsKey(ACTIVATE)) {
                    Map<String, String> actMap = (Map<String, String>) data.get(ACTIVATE);
                    syncData.setIsSoftActivate(true);

                    // Ignore `activate=null` as we've already reset the UI
                    if (actMap != null) {
                        String type = actMap.get("type");
                        String value = actMap.get("value");
                        uiManager.resetActivateQueue();
                        uiManager.schedule(type, value);
                    }

                    data.remove(ACTIVATE);
                }

                try {
                    syncCache.save(data);

                    if (data.containsKey(ASSETS)) {
                        assetManager.download(syncData.assets, jobExecutor);
                    } else {
                        uiThreadHandler.post(applySyncChanges);
                    }
                } catch (IOException e) {
                    logger.e("onSyncApp failed", e);
                    e.printStackTrace();
                }
            }
        });
    }

    // Should always run in UI
    private final Runnable applySyncChanges = new Runnable() {
        public void run() {
            try {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    logger.w("applySyncChanges: called from non-ui. Ignoring");
                    return;
                }

                if (syncChangeSet.remove(ACTIVATE)) {
                    // reset the UI to empty state
                    UIRouter.getInstance().push(new FABCtrl());
                    UIManager.getInstance().applyNext();
                }

                if (syncChangeSet.remove(FAB_ENABLED)) {
                    UIRouter.getInstance().push(new FABCtrl());
                }

                if (syncChangeSet.remove(LOG_LEVEL)) {
                    Logger.setLevel(syncData.logLevel);
                }

                if (syncChangeSet.remove(SCREENS)) {
                    ElementTracker.getInstance().reset();
                    screenManager.reset();
                    screenManager.updateScreens(syncData.screens);
                    screenManager.checkScreenChangeNow(TAG + ": applySyncChanges");
                }

                uiManager.start(activityTracker);
                syncChangeSet.clear();
            } catch (Exception e) {
                logger.e("SDKManager.applySyncChanges()", e);
            }
        }
    };

    private final Runnable bootstrap = new Runnable() {
        public void run() {
            try {
                Application app = UserIQSDKInternal.getApp();
                Map<String, Object> cacheData = syncCache.bootstrap(app);

                //noinspection unchecked
                syncChangeSet.addAll(cacheData.keySet());

                try {
                    syncData.update(cacheData);

                    // Pushing FAB on bootstrap when no cache is available
                    if (syncChangeSet.size() == 0) {
                        syncChangeSet.add(FAB_ENABLED);
                        syncData.fabEnabled = false;
                    }
                } catch (Exception e) {
                    logger.e("SyncData.update()", e);
                }

                if (cacheData.containsKey(ASSETS)) {
                    assetManager.download(syncData.assets, jobExecutor);
                } else {
                    uiThreadHandler.post(applySyncChanges);
                }
            } catch (IOException e) {
                logger.e("bootstrap failed", e);
            }

            io.start();
            logger.i("Activated: " + sdkConfig.toString());
        }
    };

    private final ActivityTracker.Callback trackerCb = new ActivityTracker.Callback() {
        void onActivityResumed(Activity activity) {
            analyticsManager.restore();

            // TODO: Seems to be redundant with onWindowFocused
            // screenManager.checkScreenChange(TAG + ": onActivityResumed");

            analyticsManager.onEmuEvent(AnalyticsManager.APP_RESUME);
        }

        void onActivityPaused(Activity activity) {
            analyticsManager.save();

            analyticsManager.onEmuEvent(AnalyticsManager.APP_PAUSE);
        }

        void onFragmentChanged() {
            screenManager.checkScreenChange(TAG + ": onFragmentChanged");
        }

        void onWindowFocused(boolean hasFocus) {
            screenManager.checkScreenChange(TAG + ": onWindowFocused");
        }

        void onAttachedToWindow() {
            // TODO: checkScreenChange here is redundant with onWindowFocused
            // TODO: Profile & enable if required!
            // screenManager.checkScreenChange(TAG + ": onAttachedToWindow");
        }

        boolean onKeyEvent(KeyEvent event) {
            boolean isBackBtn = (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                    && event.getAction() == KeyEvent.ACTION_UP);
            if (isBackBtn) screenManager.checkScreenChange(TAG + ": isBackBtn");
            return false;
        }

        boolean onTouchEvent(MotionEvent event) {
            boolean isTouchUp = (event.getAction() == KeyEvent.ACTION_UP);
            if (isTouchUp) screenManager.checkScreenChange(TAG + ": isTouchUp");
            return false;
        }
    };

    private static final String TAG = SDKManager.class.getSimpleName();
    private static final Logger logger = Logger.init(TAG);
    private static final String ASSET_DIR = SyncDataCache.UIQ_DIR + "/assets";

    private final AnalyticsManager analyticsManager;
    private final UIManager uiManager;
    private final SimpleRPC.IService sdkService;
    private final ActivityTracker activityTracker;
    private final AssetManager assetManager;
    private IO io;
    private IO.ConnectListener ioConnectListener = new IO.ConnectListener() {
        public void onIOConnect(WS ws, Connection connection) {
            analyticsManager.onEmuEvent(AnalyticsManager.APP_RESUME);
            io.notify("onUpdateUser", sdkConfig.user);
        }
    };
    private final SDKConfig sdkConfig;
    private final ThreadExecutor jobExecutor = new ThreadExecutor("JobExecutor");
    private final ScreenManager screenManager;
    private final SyncData syncData = SyncData.getInstance();
    private final SyncDataCache syncCache;
    private final RNManager rnManager;
    private final RNChangeStrategy rnChangeStrategy;

    private final Set<String> syncChangeSet = new LinkedHashSet<>();
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());

    interface ScreenChangeStrategy {
        void install(ViewGroup viewGroup);

        void uninstall();
    }

    interface ScreenDetector {
        void checkScreenChange(String who);

        void checkScreenChangeNow(String who);
    }
}
