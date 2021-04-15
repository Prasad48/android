package com.useriq.sdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

/**
 * Created by arnold on 5/10/16.
 */

public class ScreenBoundsUtil {

   /* public static int[] getScreenBoundWithoutNav(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return new int[]{0, 0, metrics.widthPixels, metrics.heightPixels};
    }*/

    public static Rect getScreenBoundWithoutNav(Context ctx) {
        Rect rect = new Rect();
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        if(wm != null) wm.getDefaultDisplay().getRectSize(rect);
        return rect;
    }

/*
    public static Rect getScreenBoundWithoutNav(Activity activity, int orientation) {


        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        Rect rect = new Rect();

        rect.left = 0;
        rect.top = getStatusHeight(AppunfoldSDKInternal.getInstance().getContext());

        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                rect.right = width;
                if (hasNavBar(activity))
                    rect.bottom = height + getNavBarHeight(activity);
                else rect.bottom = height;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                if (hasNavBar(activity))
                    rect.right = width + getNavBarHeight(activity);
                else rect.right = width;
                rect.bottom = height;
                break;
        }

        return rect;
    }
*/

    public static int getStatusHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId <= 0 ? 0 :
            context.getResources().getDimensionPixelSize(resourceId);
    }

    public static View getSourceRoot(Activity activity) {
        WindowUtil.ViewRootData viewRootData = WindowUtil.getTopWindowViewRoot(activity);
        if (viewRootData != null)
            return viewRootData._view;

        return activity.getWindow().getDecorView().getRootView();
    }

    public static boolean hasNavBar(Context context) {
        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        return (!hasMenuKey && !hasBackKey) || hasNavBarAboveM(context);
    }

    private static boolean hasNavBarAboveM(Context context) {
        int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && context.getResources().getBoolean(id);
    }

    public static int getNavBarHeight(Context context) {
        int result = 0;
        Resources resources = context.getResources();
        //The device has a navigation bar
        int orientation = getCurrentOrientation(context);

        int resourceId;
        if (isTablet()) {
            resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
        } else {
            resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
        }

        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getCurrentOrientation(Context context) {
        Resources resources = context.getResources();

        return resources.getConfiguration().orientation;
    }

    public static boolean isTablet() {
        return (Resources.getSystem().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
