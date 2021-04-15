package com.useriq.sdk;

import android.app.Activity;
import android.view.View;

import com.useriq.Logger;
import com.useriq.sdk.SDKManager.ScreenDetector;
import com.useriq.sdk.models.Element;
import com.useriq.sdk.models.Screen;

import java.util.List;

/**
 * @author sudhakar
 * @created 24-Sep-2018
 */

class ScreenManager implements ScreenDetector {

    ScreenManager(ActivityTracker activityTracker, AnalyticsManager analyticsManager) {
        this.mActivityTracker = activityTracker;
        this.screenTracker = new ScreenTracker(this, analyticsManager);

        this.screenChange = new Throttler(400, 700, new Throttler.Callback() {
            public void invoke(String who) {
                Activity activity = mActivityTracker.getCurrentActivity();
                screenTracker.checkScreen(activity, who);
            }
        });
    }

    void reset() {
        screenChange.reset();
        screenTracker.reset();
    }

    @Override
    public void checkScreenChange(String who) {
        screenChange.attempt(who);
    }

    @Override
    public void checkScreenChangeNow(String who) {
        screenChange.attemptNow(who);
    }

    ScreenTracker getScreenTracker() {
        return screenTracker;
    }

    void updateScreens(List<Screen> screens) {
        screenTracker.updateScreens(screens);
    }

    private static final Logger logger = Logger.init(ScreenManager.class.getSimpleName());
    private final ActivityTracker mActivityTracker;
    private final ScreenTracker screenTracker;
    private final Throttler screenChange;

    interface ViewTracker {
        void onViewGone(View view, Element element);
        void onViewClick(View view, Element element);
    }
}

