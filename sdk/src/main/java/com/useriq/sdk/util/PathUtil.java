package com.useriq.sdk.util;


import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class PathUtil {
    private static final Pattern KV_PAIRS = Pattern.compile("^(?:([^=]+)=([^|,]+)\\|?)+");

    public static View getView(Activity activity, String selector) {
        Resources res = activity.getResources();
        String packageName = activity.getApplicationInfo().packageName;
        List<WindowUtil.ViewRootData> viewRoots = WindowUtil.getValidRootViews(activity);
        //   ViewRoot viewRoot = ViewRoot.getPrimary(viewRoots);
        //ViewGroup rootView = ViewRoot.getViewGroup(viewRoot);
        View lastView = null;
        String[] parts = selector.split("\\|");  // |

        for (String part : parts) {
            Matcher kvMatcher = KV_PAIRS.matcher(part);
            Map<String, String> props = new LinkedHashMap<>();

            while (kvMatcher.find()) {
                String key = kvMatcher.group(1);
                String val = kvMatcher.group(2);
                props.put(key, val);
            }
            if (props.containsKey("wTitle")) {
                String val = props.remove("wTitle");
                for (int i = 0; i < viewRoots.size(); i++) {
                    View view = viewRoots.get(i)._view;
                    WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) view.getLayoutParams();
                    if (layoutParams.getTitle() != null && layoutParams.getTitle().equals(val)) {
                        lastView = view;
                        break;
                    }
                }
            } else if (props.containsKey("wIndex")) {
                int val = Integer.parseInt(props.remove("wIndex"));
                if (val < viewRoots.size()) {
                    lastView = viewRoots.get(viewRoots.size() - 1 - val)._view;
                }
            } else if (props.containsKey("sysId")) {
                String val = props.remove("sysId");
                //lastView = lastView.findViewById(res.getIdentifier(val, "id", "android"));
                lastView = findFirstViewWithProperty(activity, lastView, ViewProperty.ID, res.getIdentifier(val, "id", "android"));
            } else if (props.containsKey("id")) {
                String val = props.remove("id");
                // lastView = lastView.findViewById(res.getIdentifier(val, "id", packageName));
                lastView = findFirstViewWithProperty(activity, lastView, ViewProperty.ID, res.getIdentifier(val, "id", packageName));
            } else if (lastView != null && lastView instanceof ViewGroup) {
                for (Map.Entry<String, String> entry : props.entrySet()) {
                    switch (entry.getKey()) {
                        case "cls":
                            lastView = getViewByClass((ViewGroup) lastView, entry.getValue());
                            break;
                        case "i":
                        case "index":
                            lastView = getChildAtIndex((ViewGroup) lastView, Integer.parseInt(entry.getValue()));
                            break;
                        case "clickable":
                            lastView = findFirstViewWithProperty(activity, lastView, ViewProperty.CLICKABLE, null);
                            break;
                        case "longClickable":
                            lastView = findFirstViewWithProperty(activity, lastView, ViewProperty.LONGCLICKABLE, null);
                            break;
                        case "focusable":
                            lastView = findFirstViewWithProperty(activity, lastView, ViewProperty.FOCUSABLE, null);
                            break;
                        case "bgColor":
                            lastView = findFirstViewWithProperty(activity, lastView, ViewProperty.BGCOLOR, props.get("bgColor"));
                            break;
                        case "text":
                            lastView = findFirstViewWithProperty(activity, lastView, ViewProperty.TEXT, props.get("text"));
                            break;
                    }
                }
            } else lastView = null;

            if (lastView == null || lastView.getVisibility() != View.VISIBLE || !isInBounds(activity, lastView)) {
                return null;
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                (lastView == null || lastView.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)) {
            return null;
        }
        return lastView;
    }


    public static View getChildAtIndex(ViewGroup viewGroup, int childIndex) {
        int count = viewGroup.getChildCount();
        int currentIndex = 0;
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view.getVisibility() == View.VISIBLE && childIndex == currentIndex++) return view;
        }
        return null;
    }

    private static View getViewByClass(ViewGroup parent, String cls) {
        int len = parent.getChildCount();
        for (int i = 0; i < len; i++) {
            View child = parent.getChildAt(i);
            String name = child.getClass().getCanonicalName();
            if (name.equals(cls))
                return child;
        }
        return null;
    }

    public static Rect getPositionForView(View view) {
        if (view == null) return null;
        int[] locAnchorView = new int[2];
        view.getLocationInWindow(locAnchorView);
        return new Rect(locAnchorView[0], locAnchorView[1], locAnchorView[0] + view.getWidth(), locAnchorView[1] + view.getHeight());
    }

    private static View findFirstViewWithProperty(Activity activity, View parent, ViewProperty property, Object value) {
        if (parent == null || parent.getVisibility() != View.VISIBLE || !isInBounds(activity, parent)) {
            return null;
        }
        /**
         * TODO: We cant use IMPORTANT_FOR_ACCESSIBILITY as it is above api 16; SO FIX IT
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && (parent.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_NO || parent.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)) {
            return null;
        }
        switch (property) {
            case CLICKABLE:
                if (parent.isClickable()) return parent;
                break;
            case FOCUSABLE:
                if (parent.isFocusable()) return parent;
                break;
            case LONGCLICKABLE:
                if (parent.isLongClickable()) return parent;
                break;
            case BGCOLOR:
                if (parent.getBackground() != null && parent.getBackground() instanceof ColorDrawable) {
                    ColorDrawable colorDrawable = (ColorDrawable) parent.getBackground();
                    String hexColor = String.format("#%06X", (0xFFFFFF & colorDrawable.getColor()));//https://stackoverflow.com/questions/6539879/how-to-convert-a-color-integer-to-a-hex-string-in-android
                    if (hexColor.equals(value))
                        return parent;
                }
                break;
            case ID:
                if (parent.getId() == (int) value) return parent;
                break;
            case TEXT:
                if (parent instanceof TextView && ((TextView) parent).getText().toString().equals(value)) {
                    return parent;
                }
                break;
        }
        if (parent instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) parent).getChildCount(); i++) {
                View child = ((ViewGroup) parent).getChildAt(i);
                View view = findFirstViewWithProperty(activity, child, property, value);
                if (view != null)
                    return view;
            }
        }
        return null;
    }

    private static boolean isInBounds(Activity activity, View view) {
        Rect screenBound = ScreenBoundsUtil.getScreenBoundWithoutNav(activity);
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        Rect bounds = new Rect();
        bounds.left = loc[0];
        bounds.right = loc[0] + view.getMeasuredWidth();
        bounds.top = loc[1];
        bounds.bottom = loc[1] + view.getMeasuredHeight();
        return isInBounds(screenBound, bounds);
      /*  if ((loc[0] == screenBound.right || loc[0] < screenBound.right) && (loc[0] == screenBound.left || loc[0] > screenBound.left)
                && (loc[1] == screenBound.bottom || loc[1] < screenBound.bottom) && (loc[1] == screenBound.top || loc[1] > screenBound.top)) {
            return true;
        }*/
        //  return false;

    }

    public static boolean isInBounds(Rect screenBound, Rect anchorBounds) {
        return ((anchorBounds.right <= screenBound.right && anchorBounds.right > screenBound.left) ||
                (anchorBounds.left >= screenBound.left && anchorBounds.left < screenBound.right))
                && ((anchorBounds.bottom <= screenBound.bottom && anchorBounds.bottom > screenBound.top)
                || (anchorBounds.top >= screenBound.top && anchorBounds.top < screenBound.bottom));

    }


    enum ViewProperty {
        CLICKABLE,
        FOCUSABLE,
        LONGCLICKABLE,
        BGCOLOR,
        ID,
        TEXT
    }


}
