package com.useriq.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ViewUtils {

    private static Point realSize = new Point();

    private static boolean isWithinBounds(int top, int left, int right, int bottom) {
        int tolerance = 5;
        int width = Math.abs(right - left);
        int height = Math.abs(bottom - top);
        int screenLeft = tolerance;
        int screenTop = tolerance;
        int screenRight = width - tolerance;
        int screenBottom = height - tolerance;

        boolean doesIntersect = !(
                left > screenRight || right < screenLeft || top > screenBottom || bottom < screenTop
        );

        return width >= tolerance && height >= tolerance && doesIntersect;
    }

    /**
     * findViewByClass finds View matching classname using non-recursive BFS
     * <p>
     * TODO: Replace BFS with DFS
     * For now BFS wont cause issues with search for RN_ROOT_VIEW
     *
     * @param rootView  ViewGroup
     * @param klass class of view
     * @return ViewGroup | null
     */
    static ViewGroup findViewByClass(ViewGroup rootView, Class klass) {
        ViewGroup vg = rootView;
        List<ViewGroup> childGroups = new ArrayList<>();

        while (vg != null) {
            int numChilds = vg.getChildCount();

            int i = 0;
            while (i < numChilds) {
                View child = vg.getChildAt(i);

                if (child instanceof ViewGroup) {
                    if (klass.isInstance(child))
                        return (ViewGroup) child;
                    childGroups.add((ViewGroup) child);
                }
                i++;
            }

            if (childGroups.size() > 0) {
                vg = childGroups.remove(0);
            } else {
                return null;
            }
        }

        return null;
    }

    public static boolean isAttachedToWindow(View view) {
        if (view == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return view.isAttachedToWindow();
        } else {
            return view.getHandler() != null;
        }
    }

    public static boolean isVisible(final View view) {
        if (view == null) {
            return false;
        }
        if (!view.isShown()) {
            return false;
        }

        final Rect actualPosition = new Rect();
        view.getGlobalVisibleRect(actualPosition);

        return actualPosition.intersect(getScreenSize());
    }

    public static int getScreenWidth() {
        return getDisplaySize().x;
    }

    public static int getScreenHeight() {
        return getDisplaySize().y;
    }

    public static Point getDisplaySize() {
        Context ctx = UserIQSDKInternal.getContext();
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(realSize);
            } else {
                display.getSize(realSize);
            }
        }

        return realSize;
    }

    public static Rect getScreenSize() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        return new Rect(0, 0, screenWidth, screenHeight);
    }

    public static Object getProp(View view, String prop) {
        switch (prop) {
            case "selected":
                return view.isSelected();
            case "focused":
                return view.isFocused();
            case "enabled":
                return view.isEnabled();
            case "text":
                return view instanceof TextView ? ((TextView) view).getText().toString() : null;
            default:
                return null;
        }
    }

    public static Drawable getBgDrawable(List<Integer> bgColor, List<Integer> borderColor, float borderWidth, List<Integer> borderRadius) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        if (borderRadius != null) {
            gradientDrawable.setCornerRadii(new float[]{borderRadius.get(0), borderRadius.get(0), borderRadius.get(1), borderRadius.get(1), borderRadius.get(2), borderRadius.get(2), borderRadius.get(3), borderRadius.get(3)});
        }
        if (bgColor != null) {
            gradientDrawable.setColor(Color.argb(bgColor.get(0), bgColor.get(1), bgColor.get(2), bgColor.get(3)));
        }
        if (borderColor != null) {
            gradientDrawable.setStroke((int) borderWidth, Color.argb(borderColor.get(0), borderColor.get(1), borderColor.get(2), borderColor.get(3)));
        }
        return gradientDrawable;
    }

    public static Activity getActivity(View view) {
        if (view == null) return null;
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

}
