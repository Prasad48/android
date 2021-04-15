package com.unfold.xposed;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.useriq.Logger;
import com.useriq.sdk.UIRouter;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.ctxHelp.CtxHelpCtrl;
import com.useriq.sdk.helpcenter.HelpCenterCtrl;
import com.useriq.sdk.models.Screen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class UnfoldSdk {
    private static final Logger logger = Logger.init(UnfoldSdk.class.getSimpleName());
    public static UnfoldSdk unfoldSdk;
    private UserIQSDKInternal internalSDK;
    Application application;
    LocalActivityChangeListener activityChangeListener;
    String sessionId = null;
    String hostURL = null;
    String apiKey = null;
    String serverURL = null;
    String clientKey = null;
    boolean disableAnalytics = false;
    boolean disableSDK = false;
    private boolean firstActivity = true;

    private UnfoldSdk(Application application) {
        this.application = application;
        activityChangeListener = new LocalActivityChangeListener();
        logger.d("Unfold Crawl Sdk Integrated");
        application.registerActivityLifecycleCallbacks(activityChangeListener);
    }

    static public UnfoldSdk init(Application application) {
        return unfoldSdk = new UnfoldSdk(application);
    }


    public void updateIntentData(String sessionId, String hostURL, String apiKey, String serverURL, boolean disableAnalytics, boolean disableSDK) {
        this.sessionId = sessionId;
        this.hostURL = hostURL;
        this.apiKey = apiKey;
        this.serverURL = serverURL;
        this.disableSDK = disableSDK;
        this.disableAnalytics = disableAnalytics;
    }

    private void onFirstActivityOpened(Activity activity) {
        logger.d(activity.getClass().getCanonicalName() + " Opened");
        //  application.unregisterActivityLifecycleCallbacks(activityChangeListener);
        if (hostURL == null) {
            logger.w("hostUrl not found for " + activity.getPackageName());
        } else {
            logger.d("hostUrl for " + activity.getPackageName() + " is " + hostURL);
        }
        if (sessionId == null) {
            logger.w("appunfoldUid not found for " + activity.getPackageName());
        } else {
            logger.d("appunfoldUid for " + activity.getPackageName() + " is " + sessionId);
        }

        if (serverURL == null) {
            logger.w("serverUrl not found for " + activity.getPackageName());
        } else {
            logger.d("serverUrl for " + activity.getPackageName() + " is " + serverURL);
        }

        if (apiKey == null) {
            logger.w("api key not found for " + activity.getPackageName());
        } else {
            logger.d("apiKey for " + activity.getPackageName() + " is " + apiKey);
        }
        if (apiKey == null) {
            logger.w("client key not found for " + activity.getPackageName());
        } else {
            logger.d("client key for " + activity.getPackageName() + " is " + apiKey);
        }
        if (disableAnalytics) {
            logger.d("Analytics disable command");
        }

        if (sessionId != null) {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String todayDate = dateFormat.format(new Date());

            HashMap<String, String> params = new HashMap<>();
            params.put("Location", "Planet C-53");
            HashMap<String, Object> user = new HashMap<>();
            user.put("userId", "UIQ_MOBILE");
            user.put("name", "UIQ Simulator");
            user.put("email", "nobody@useriq.com");
            user.put("accountId", "UIQ_ACCOUNT");
            user.put("accountName", "UIQ Simulator Account");
            user.put("signupDate", todayDate);
            try {
                internalSDK = UserIQSDKInternal.init(null, apiKey, user, sessionId, serverURL, activity.getApplication().getBaseContext().createPackageContext("com.unfold.xposed", Context.CONTEXT_IGNORE_SECURITY), activity, false);
                logger.d("SDK initialised with sessionId: " + sessionId + " serverUrl: " + serverURL);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            logger.e("SDK not initialised, sessionid is null", null);
        }
    }

    void showHelpCentre() {
        internalSDK.getCurrActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIRouter.getInstance().push(new HelpCenterCtrl());
                logger.d("showing HelpCentre from xposed");
            }
        });
    }

    void showCtxHelp() {
        Screen screen = internalSDK.getCurrScreen();
        if (screen == null) return;
        if (internalSDK.getSyncData().getCtxHelpForScreen(screen.id) == null) {
            return;
        }
        internalSDK.getCurrActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIRouter.getInstance().push(new CtxHelpCtrl());
                logger.d("showing Contextual Help from xposed");
            }
        });
    }

    private class LocalActivityChangeListener implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (firstActivity) {
                firstActivity = false;
                onFirstActivityOpened(activity);
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

}
