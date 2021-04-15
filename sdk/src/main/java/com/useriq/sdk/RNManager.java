package com.useriq.sdk;

import android.app.Activity;
import android.view.View;
import android.view.ViewParent;

import com.useriq.Logger;
import com.useriq.sdk.capture.ViewRoot;
import com.useriq.sdk.models.Element;
import com.useriq.sdk.models.Screen;

import java.util.List;

/**
 * RNManager listens to all react events, filters on tap events & track them
 */
class RNManager {
    private final AnalyticsManager analyticsManager;
    private int targetTag = -1;
    private static final Logger logger = Logger.init(RNManager.class.getSimpleName());

    RNManager(AnalyticsManager analyticsManager) {
        this.analyticsManager = analyticsManager;
    }

    void onReactEvent(Activity currActivity, String eventName, int viewTag, Screen currScreen) {
        if (isPress(eventName, viewTag)) {
            this.trackPress(currActivity, viewTag, currScreen);
        }
    }

    private boolean isPress(String eventName, int viewTag) {
        if (eventName.equals("topTouchStart")) {
            targetTag = viewTag;
            return false;
        }

        if (eventName.equals("topTouchEnd")) {
            boolean isTap = viewTag == targetTag;
            targetTag = -1;
            return isTap;
        }

        return false;
    }

    private void trackPress(Activity currActivity, int viewTag, Screen currScreen) {
        if (currActivity == null) logger.d("trackPress(): currActivity is null");

        View target = null;
        try {
            List<ViewRoot> roots = ViewRoot.from(currActivity);
            for (ViewRoot viewRoot : roots) {
                target = viewRoot.view.findViewById(viewTag);
                if (target != null) break;
            }
        } catch (Exception e) {
            logger.e("trackPress(): failed in finding view", e);
            e.printStackTrace();
        }

        if (target == null) {
            logger.d("Cannot find view for tracking");
            return;
        }
        Element matchedEl = getMatchedEl(target, currScreen);

        if (matchedEl == null) {
            // Log for the parent check
            ViewParent parent = target.getParent();
            if (parent instanceof View) {
                matchedEl = getMatchedEl((View) parent, currScreen);
            }
        }

        if (matchedEl != null) {
            analyticsManager.onClick(matchedEl.id);
        }
    }

    private static Element getMatchedEl(View target, Screen currScreen) {
        if (currScreen == null) return null;
        for (Element el : currScreen.elements) {
            ViewNode viewNode = new ViewNode(target);
            if (el.matches(0, viewNode)) return el;
        }

        return null;
    }
}
