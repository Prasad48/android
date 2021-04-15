package com.useriq.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import com.useriq.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author sudhakar
 * @created 25-Sep-2018
 */
class ActivityTracker {
    private static final Logger logger = Logger.init(ActivityTracker.class.getSimpleName());

    private List<Callback> cbList = new ArrayList<>();
    private WeakReference<Activity> activityRef;
    private WeakHashMap<View, RootViewTracker> rootViewMap = new WeakHashMap<>();
    private OrientationEventListener orientationEventListener;

    ActivityTracker(Activity activity) {
        this.activityRef = new WeakReference<>(activity);
        if (activity != null) {
            lifecycleCallbacks.onActivityResumed(activity);
        }
    }

    void register(Application app) {
        orientationEventListener = new OrientationEventListener(app) {

            @Override
            public void onOrientationChanged(int orientation) {
                UserIQSDKInternal.getAnalyticsManager().onRotationEvent(orientation / 90);
            }

        };
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
        }
        app.registerActivityLifecycleCallbacks(lifecycleCallbacks);
    }

    void unregister(Application app) {
        app.unregisterActivityLifecycleCallbacks(lifecycleCallbacks);
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Window window = activity.getWindow();
            Window.Callback winCallback = window.getCallback();
            if (winCallback instanceof WindowCB) {
                window.setCallback(((WindowCB) winCallback).oldCb);
            }
        }
        orientationEventListener.disable();
    }

    Activity getCurrentActivity() {
        return activityRef.get();
    }

    public void addCallback(Callback cb) {
        this.cbList.add(cb);
    }

    public void removeCallback(Callback cb) {
        this.cbList.remove(cb);
    }

    private ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifecycleCallbacks() {
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
        public void onActivityStarted(Activity activity) { }
        public void onActivityResumed(Activity activity) {
            logger.d("onActivityResumed(): " + activity.getComponentName() + ", cbList.size() = " + cbList.size());

            int orientation = activity.getResources().getConfiguration().orientation;
            if (UserIQSDKInternal.getAnalyticsManager() != null) {
                if (orientation == 1) {
                    UserIQSDKInternal.getAnalyticsManager().onRotationEvent(0);
                } else {
                    UserIQSDKInternal.getAnalyticsManager().onRotationEvent(1);
                }
            }
            activityRef = new WeakReference<>(activity);

            Window window = activity.getWindow();
            window.setCallback(new WindowCB(window.getCallback(), cbList));

            RootViewTracker rootViewTracker = new RootViewTracker(cbList);
            activity.getFragmentManager().addOnBackStackChangedListener(rootViewTracker);
            View view = activity.getWindow().getDecorView().getRootView();
            view.addOnAttachStateChangeListener(rootViewTracker);
            view.getViewTreeObserver().addOnScrollChangedListener(rootViewTracker);
            rootViewMap.put(view, rootViewTracker);

            for (Callback cb : cbList) {
                cb.onActivityResumed(activity);
            }
        }

        public void onActivityPaused(Activity activity) {
            Window window = activity.getWindow();
            Window.Callback winCallback = window.getCallback();
            if (winCallback instanceof WindowCB) {
                window.setCallback(((WindowCB) winCallback).oldCb);
            }

            for (Callback cb : cbList) {
                cb.onActivityPaused(activity);
            }

            for (Map.Entry<View, RootViewTracker> entry : rootViewMap.entrySet()) {
                RootViewTracker rootViewTracker = entry.getValue();
                rootViewTracker.reset(entry.getKey());
                activity.getFragmentManager().removeOnBackStackChangedListener(rootViewTracker);
            }
        }
        public void onActivityStopped(Activity activity) { }
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
        public void onActivityDestroyed(Activity activity) { }
    };

    static abstract class Callback {
        void onActivityResumed(Activity activity) {}
        void onActivityPaused(Activity activity) {}
        void onFragmentChanged() {}
        boolean onKeyEvent(KeyEvent event) { return false; }
        boolean onTouchEvent(MotionEvent event) { return false; }
        void onWindowFocused(boolean hasFocus) {}
        void onAttachedToWindow() {}
        void onScrollStart() {}
        void onScrollStop() {}
    }

    static class RootViewTracker implements ViewTreeObserver.OnScrollChangedListener, View.OnAttachStateChangeListener, FragmentManager.OnBackStackChangedListener {
        private static final int SCROLL_FINISH = 0;
        private final List<Callback> trackers;
        private final Handler handler;
        private boolean shouldNotifyScrollChange = true;

        RootViewTracker(List<Callback> trackerList) {
            this.trackers = trackerList;
            this.handler = new Handler(new Handler.Callback() {
                public boolean handleMessage(Message msg) {
                    if (msg.what == SCROLL_FINISH) {
                        for (Callback cb : trackers) {
                            cb.onScrollStop();
                        }
                        shouldNotifyScrollChange = true;
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public void onScrollChanged() {
            if (shouldNotifyScrollChange) {
                for (Callback cb : trackers) {
                    cb.onScrollStart();
                }
                shouldNotifyScrollChange = false;
            }

            this.handler.removeCallbacksAndMessages(null);
            this.handler.sendEmptyMessageDelayed(SCROLL_FINISH, 500);
        }

        @Override public void onViewAttachedToWindow(View v) { }

        @Override public void onViewDetachedFromWindow(View v) {
            this.reset(v);
        }

        void reset(View v) {
            handler.removeCallbacksAndMessages(null);
            v.removeOnAttachStateChangeListener(this);
            v.getViewTreeObserver().removeOnScrollChangedListener(this);
        }

        @Override
        public void onBackStackChanged() {
            for (Callback cb : trackers) {
                cb.onFragmentChanged();
            }
        }
    }

    static class WindowCB implements Window.Callback {
        private final Window.Callback oldCb;
        private final List<Callback> trackers;

        WindowCB(Window.Callback oldCb, List<Callback> trackers) {
            this.oldCb = oldCb;
            this.trackers = trackers;
        }

        public boolean dispatchKeyEvent(KeyEvent event) {
            for (Callback cb : trackers) {
                boolean processed = cb.onKeyEvent(event);
                if (processed) {
                    // first cb that handles keyevent wins!
                    // other cb's wont receive callbacks
                    return true;
                }
            }
            return oldCb.dispatchKeyEvent(event);
        }

        public boolean dispatchTouchEvent(MotionEvent event) {
            for (Callback cb : trackers) {
                boolean processed = cb.onTouchEvent(event);
                if (processed) {
                    // first cb that handles keyevent wins!
                    // other cb's wont receive callbacks
                    return true;
                }
            }
            return oldCb.dispatchTouchEvent(event);
        }

        public void onWindowFocusChanged(boolean hasFocus) {
            for (Callback cb : trackers) {
                cb.onWindowFocused(hasFocus);
            }
            oldCb.onWindowFocusChanged(hasFocus);
        }

        public void onAttachedToWindow() {
            for (Callback cb : trackers) {
                cb.onAttachedToWindow();
            }
            oldCb.onAttachedToWindow();
        }

        public boolean dispatchKeyShortcutEvent(KeyEvent event) { return oldCb.dispatchKeyEvent(event); }
        public boolean dispatchTrackballEvent(MotionEvent event) { return oldCb.dispatchTrackballEvent(event); }
        public boolean dispatchGenericMotionEvent(MotionEvent event) { return oldCb.dispatchGenericMotionEvent(event); }

        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            return oldCb.dispatchPopulateAccessibilityEvent(event);
        }

        public View onCreatePanelView(int featureId) { return oldCb.onCreatePanelView(featureId); }
        public boolean onCreatePanelMenu(int featureId, Menu menu) { return oldCb.onCreatePanelMenu(featureId, menu); }
        public boolean onPreparePanel(int featureId, View view, Menu menu) { return oldCb.onPreparePanel(featureId, view, menu); }
        public boolean onMenuOpened(int featureId, Menu menu) { return oldCb.onMenuOpened(featureId, menu); }
        public boolean onMenuItemSelected(int featureId, MenuItem item) { return oldCb.onMenuItemSelected(featureId, item); }
        public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
            oldCb.onWindowAttributesChanged(attrs);
        }

        public void onContentChanged() {
            oldCb.onContentChanged();
        }

        public void onDetachedFromWindow() {
            oldCb.onDetachedFromWindow();
        }

        public void onPanelClosed(int featureId, Menu menu) {oldCb.onPanelClosed(featureId, menu); }
        public boolean onSearchRequested() { return oldCb.onSearchRequested(); }
        public boolean onSearchRequested(SearchEvent searchEvent) { return oldCb.onSearchRequested(); }
        public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) { return oldCb.onWindowStartingActionMode(callback); }
        @TargetApi(Build.VERSION_CODES.M)
        public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) { return oldCb.onWindowStartingActionMode(callback, type); }
        public void onActionModeStarted(ActionMode mode) {oldCb.onActionModeStarted(mode); }
        public void onActionModeFinished(ActionMode mode) {oldCb.onActionModeFinished(mode); }
        @TargetApi(Build.VERSION_CODES.N)
        public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {oldCb.onProvideKeyboardShortcuts(data, menu, deviceId); }
        @TargetApi(Build.VERSION_CODES.O)
        public void onPointerCaptureChanged(boolean hasCapture) {oldCb.onPointerCaptureChanged(hasCapture); }
    }
}
