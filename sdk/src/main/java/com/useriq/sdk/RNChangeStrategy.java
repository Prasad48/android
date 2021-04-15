package com.useriq.sdk;

import android.view.View;
import android.view.ViewGroup;

import com.useriq.Logger;
import com.useriq.sdk.capture.Reflect;

import java.util.Set;
import java.util.WeakHashMap;

/**
 * RNChangeStrategy tracks changes to view hierarchy on React native views.
 * <br><br>
 * When <code>install</code> is called from <code>onReactNativeResume</code>, we
 * traverse from <code>rootView</code> to find RN_ROOT_CLASS & attach
 * <code>HierarchyChangeListener</code> to it & also to its immediate children.
 * <br><br>
 * Immediate children of <code>RN_ROOT_CLASS</code> is required to be tracked as
 * ReactNative's StackNavigator shuffles views one level down.
 */
public class RNChangeStrategy implements SDKManager.ScreenChangeStrategy {
    private static final String TAG = RNChangeStrategy.class.getSimpleName();
    private static final Logger logger = Logger.init(TAG);
    private static final String RN_ROOT_CLASS = "com.facebook.react.ReactRootView";

    private final SDKManager.ScreenDetector screenDetector;
    private WeakHashMap<ViewGroup, Boolean> observedRNViews = new WeakHashMap<>();
    private Class<?> rnClass;

    RNChangeStrategy(SDKManager.ScreenDetector screenDetector) {
        this.screenDetector = screenDetector;
    }

    @Override
    public void install(ViewGroup decorView) {
        // only one decor view can have HierarchyChangeListeners attached
        this.uninstall();

        if (rnClass == null) {
            try {
                rnClass = Class.forName(RN_ROOT_CLASS, true, UserIQSDKInternal.getApp().getClassLoader());
            } catch (ClassNotFoundException e) {
                logger.e("'" + RN_ROOT_CLASS + "' not found", e);
                return;
            }
        }

        ViewGroup reactRootView = ViewUtils.findViewByClass(decorView, rnClass);

        if (reactRootView != null) {
            observedRNViews.put(reactRootView, true);
            reactRootView.setOnHierarchyChangeListener(new HierarchyChangeListener(reactRootView, 1));
        }
    }

    @Override
    public void uninstall() {
        Set<ViewGroup> viewGroups = observedRNViews.keySet();

        for (ViewGroup vg : viewGroups) {
            if (vg == null) continue;
            ViewGroup.OnHierarchyChangeListener old = getOnHierarchyChangeListener(vg);
            if (old instanceof HierarchyChangeListener && ((HierarchyChangeListener) old).oldListener != null)
                vg.setOnHierarchyChangeListener(((HierarchyChangeListener) old).oldListener);
        }

        observedRNViews.clear();
    }

    class HierarchyChangeListener implements ViewGroup.OnHierarchyChangeListener {

        private final ViewGroup vg;
        private ViewGroup.OnHierarchyChangeListener oldListener;
        private int maxLevel = 4;
        private int currentLevel;

        HierarchyChangeListener(ViewGroup vg, int level) {
            this.vg = vg;
            this.oldListener = getOnHierarchyChangeListener(vg);
            this.currentLevel = level;
        }

        @Override
        public void onChildViewAdded(View parent, View child) {
            logger.d("onChildViewAdded, view cls: " + child.getClass().getCanonicalName());
            screenDetector.checkScreenChange(TAG + ": onChildViewAdded");
            if (oldListener != null) oldListener.onChildViewAdded(parent, child);

            if (child instanceof ViewGroup) {
                ViewGroup childGroup = (ViewGroup) child;
                if (currentLevel <= maxLevel) {
                    childGroup.setOnHierarchyChangeListener(new HierarchyChangeListener(childGroup, ++currentLevel));
                    observedRNViews.put(childGroup, true);
                }
            }
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            logger.d("onChildViewRemoved, view cls: " + child.getClass().getCanonicalName());
            screenDetector.checkScreenChange(TAG + ": onChildViewRemoved");
            if (oldListener != null) oldListener.onChildViewRemoved(parent, child);

            if (!(child instanceof ViewGroup)) return;

            ViewGroup childGroup = (ViewGroup) child;
            ViewGroup.OnHierarchyChangeListener old = getOnHierarchyChangeListener(childGroup);
            if (old instanceof HierarchyChangeListener && ((HierarchyChangeListener) old).oldListener != null) {
                childGroup.setOnHierarchyChangeListener(((HierarchyChangeListener) old).oldListener);
            }
            for (int i = 0; i < maxLevel - currentLevel; i++) {
                if (childGroup.getChildCount() > 0 && childGroup.getChildAt(0) instanceof ViewGroup) {
                    childGroup = (ViewGroup) childGroup.getChildAt(0);
                    old = getOnHierarchyChangeListener(childGroup);
                    if (old instanceof HierarchyChangeListener && ((HierarchyChangeListener) old).oldListener != null) {
                        childGroup.setOnHierarchyChangeListener(((HierarchyChangeListener) old).oldListener);
                    }
                    observedRNViews.remove(childGroup);
                }
            }
            observedRNViews.remove(childGroup);
        }
    }


    private static ViewGroup.OnHierarchyChangeListener getOnHierarchyChangeListener(ViewGroup vg) {
        try {
            return (ViewGroup.OnHierarchyChangeListener) Reflect.getFieldValue("mOnHierarchyChangeListener", vg);
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}