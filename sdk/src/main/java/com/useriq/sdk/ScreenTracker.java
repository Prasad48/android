package com.useriq.sdk;

import android.app.Activity;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;

import com.useriq.Logger;
import com.useriq.sdk.SDKManager.ScreenDetector;
import com.useriq.sdk.capture.ViewRoot;
import com.useriq.sdk.models.Element;
import com.useriq.sdk.models.Screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static com.useriq.sdk.ViewUtils.isAttachedToWindow;

/**
 * @author sudhakar
 * @created 04-Oct-2018
 */
public class ScreenTracker {

    ScreenTracker(final ScreenDetector screenDetector, AnalyticsManager analyticsManager) {
        this.mAnalyticsManager = analyticsManager;

        this.currScreen = null;
        this.screens = new ArrayList<>();

        this.elTrackerCb = new ElementTracker.Callback() {
            @Override
            public void onViewDetached(View view, Element element) {

                if (currScreen == null) return;

                boolean screenInvalidated = currScreen.predicates.contains(element.key);
                if (screenInvalidated) {
                    screenDetector.checkScreenChange(TAG + ": onViewDetached");
                }
            }

            @Override
            public void onViewClick(View view, Element element) {
                logger.d(element + " clicked!");
                mAnalyticsManager.onClick(element.id);
            }
        };
    }

    public void addCallback(ScreenTracker.Callback cb) {
        this.screenCallbacks.add(cb);
    }

    public void removeCallBack(ScreenTracker.Callback cb) {
        this.screenCallbacks.remove(cb);
    }

    void updateScreens(List<Screen> screens) {
        this.screens = screens;
    }

    Screen getCurrScreen() {
        return currScreen;
    }

    void reset() {
        this.currScreen = null;
    }

    /**
     * checkScreen traverses view hierarchy to find the matched screen
     *
     * Once screen is identified, we attach ClickDelegate to all trackable elements
     * and store them as weakRef in trackedEls. If currScreen != null then we use
     * query trackedEls before invalidating the currScreen.
     *
     * Then we venture into full tree traversals & perform costly matching!
     * n - views
     * m - screens
     * o - elements
     * p - elProps
     *
     * worstCase = n * m * o * p
     *
     * TODO: Measure & Improve performance
     *
     * @param activity current activity
     */
    void checkScreen(Activity activity, String who) {
        if(activity == null) return;

        long start = System.nanoTime();

        boolean currentScreenValid = isCurrentScreenValid();

        if (currentScreenValid) {
            logPrefInfo("existing", who, start, 0, currScreen);
            return;
        }

        // Narrow down to sublist of screens tagged from current activity
        List<Screen> screenSubset = new ArrayList<>();
        String controller = activity.getClass().getCanonicalName();

        for(Screen screen: this.screens) {
            if(screen.controller.matches(controller))
                screenSubset.add(screen);
        }

        MatchFinder.Result result = MatchFinder.match(activity, screenSubset);

        Screen prevScreen = currScreen;
        currScreen = result != null ? result.screen : null;

        if (currScreen == prevScreen) {
            logPrefInfo("sameAgain", who, start, screenSubset.size(), prevScreen);
            return;
        }

        if (prevScreen != null && !prevScreen.equals(currScreen)) {
            ElementTracker.getInstance().reset();
            mAnalyticsManager.onScreenExit(prevScreen.id);
        }

        logPrefInfo("fresh", who, start, screenSubset.size(), prevScreen);

        if(result != null && currScreen != null) {
            mAnalyticsManager.onScreenEnter(currScreen.id);
            track(result.viewMap);
        }

        notifyCallbacks(currScreen, prevScreen);
    }

    private void logPrefInfo(String prefix, String who, long start, int subsetLen, Screen prevScreen) {
        UserIQSDKInternal.getAnalyticsManager().onPref(start, "checkScreen",
                "who", who,
                "prefix", prefix,
                "numScreens", String.valueOf(subsetLen),
                "prevScreen", (prevScreen == null ? null : prevScreen.id));
    }

    private void notifyCallbacks(Screen currScreen, Screen prevScreen) {
        if(prevScreen == currScreen) return;

        for(Callback cb: this.screenCallbacks) {
            cb.onScreenChange(currScreen, prevScreen);
        }
    }

    private boolean isCurrentScreenValid() {
        if(currScreen == null) return false;

        List<ViewGroup> viewParents = new ArrayList<>();
        for (Element el: currScreen.predicateEls) {
            View view = ElementTracker.getInstance().getView(el);
            if (!isAttachedToWindow(view)) {
                // Even if one view is gone, then currScreen is inValid!
                return false;
            }
            if (view.getParent() instanceof ViewGroup) {
                viewParents.add((ViewGroup) view.getParent());
            }
        }

        List<ViewRoot> viewRoots;
        try {
            Activity activity = UserIQSDKInternal.getCurrActivity();
            viewRoots = ViewRoot.from(activity);
        } catch (Exception e) {
            logger.e("isCurrentScreenValid(): finding root views failed", e);
            return false;
        }

        if (viewRoots.size() == 0) {
            // then most like we are in the background
            // lets invalidate the currScreen
            return false;
        }

        // we are only interested on the topmost view
        ViewRoot topViewRoot = viewRoots.get(viewRoots.size() - 1);
        IBinder windowToken = topViewRoot.getView().getWindowToken();

//        Set<ViewInfo> viewInfoSet = new LinkedHashSet<>();
//        for (ViewGroup parent: viewParents) {
//            if (parent.getWindowToken() != windowToken) {
//                // our parent does not belong to top window
//                // so most likely, curScreen is no-longer valid
//                return false;
//            }
//            List<ViewInfo> viewInfos = ViewInfo.flatten(parent, 1);
//            viewInfoSet.addAll(viewInfos);
//        }

        Map<Element, ViewNode> matched = new HashMap<>();
        ViewTree viewTree = new ViewTree(topViewRoot.view);

        // wIndex should be 0 as we expect the currScreen should be
        // part of the topWindow else its no longer valid
        int wIndex = 0;
        return MatchFinder.match(currScreen, wIndex, viewTree, matched);
    }

    private void track(WeakHashMap<View, Element> elMap) {
        for (Map.Entry<View, Element> entry : elMap.entrySet()) {
            View view = entry.getKey();
            Element element = entry.getValue();
            ElementTracker.getInstance().track(element, view, elTrackerCb);
        }
    }

    private static final String TAG = ScreenTracker.class.getSimpleName();
    private static final Logger logger = Logger.init(TAG);

    private final AnalyticsManager mAnalyticsManager;
    private final ElementTracker.Callback elTrackerCb;
    private ArrayList<ScreenTracker.Callback> screenCallbacks = new ArrayList<>();

    private Screen currScreen;
    private List<Screen> screens;

    public interface Callback {
        void onScreenChange(Screen newScreen, Screen oldScreen);
    }
}
