package com.useriq.sdk.capture;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Checkable;
import android.widget.TextView;

import com.useriq.sdk.UIRootView;
import com.useriq.sdk.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smylsamy on 06/12/16.
 */

class LayoutTree {
    private static final String TAG = LayoutTree.class.getSimpleName();

    static ArrayList<Map<String, Object>> from(ArrayList<ViewGroup> root) {
        Map<String, Object> tree = null;
        ArrayList<Map<String, Object>> windows = new ArrayList<>();
        for (int i = 0; i < root.size(); i++) {
            tree = new HashMap<>();
            if (root.get(i).getLayoutParams() != null && root.get(i).getLayoutParams() instanceof WindowManager.LayoutParams) {
                tree.put("wTitle", ((WindowManager.LayoutParams) root.get(i).getLayoutParams()).getTitle());
                tree.put("wIndex", root.size() - 1 - i);
            }

            dump(root.get(i), tree);
            windows.add(tree);
        }
        return windows;
    }

    private static void dump(final View node, Map<String, Object> tree) {
        int[] pos = new int[2];
        int intId = node.getId();
        Resources resources = node.getContext().getResources();
        String id;

        try {
            id = (View.NO_ID == intId) ? "" : resources.getResourceName(intId);
        } catch (Exception e) {
            id = "";
        }
        List<Map<String, Object>> children = new ArrayList<>();
        node.getLocationOnScreen(pos);

        List<Integer> bounds = Arrays.asList(
                pos[0],
                pos[1],
                pos[0] + node.getWidth(),
                pos[1] + node.getHeight()
        );
        tree.put("children", children);
        tree.put("cls", node.getClass().getCanonicalName());
        tree.put("focusable", node.isFocusable());
        tree.put("focused", node.isFocused());
        tree.put("clickable", node.isClickable());
        tree.put("enabled", node.isEnabled());
        tree.put("longClickable", node.isLongClickable());
        tree.put("opacity", node.getAlpha());
        tree.put("isVisible", node.getVisibility() == View.VISIBLE);
        tree.put("bounds", bounds);
        tree.put("resourceId", id);
        tree.put("contentDesc", node.getContentDescription());

        Object tag = node.getTag();

        // React Native stores the TestID in the tag of the view
        if (tag instanceof String) {
            tree.put("testID", tag);
        } else if (Utils.getRNTestIdKey() != -1) {
            tag = node.getTag(Utils.getRNTestIdKey());
            if (tag != null) tree.put("testID", tag);
        }

        ArrayList<String> ignoredAttrs = new ArrayList<>();
        ignoredAttrs.add("focused");

        if (node instanceof Checkable) {
            tree.put("isChecked", ((Checkable) node).isChecked());
            ignoredAttrs.add("isChecked");
        }
        tree.put("ignoredAttrs", ignoredAttrs);


        if (node.getBackground() != null && node.getBackground() instanceof ColorDrawable) {
            ColorDrawable cd = ((ColorDrawable) node.getBackground());
            int alpha = cd.getAlpha();
            int color = cd.getColor();
            int red = Color.red(color);
            int blue = Color.blue(color);
            int green = Color.green(color);
            List<Integer> argb = Arrays.asList(alpha, red, green, blue);
            tree.put("bgColor", argb);
            tree.put("hasBgImage", false);
        } else if (node.getBackground() != null) {
            tree.put("hasBgImage", true);
        } else {
            tree.put("hasBgImage", false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            tree.put("importantForAccessibility", getImportantForAccessibility(node));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            tree.put("scrollable", node.isScrollContainer());
        }
        if (node instanceof TextView) {
            TextView textView = (TextView) node;
            tree.put("text", textView.getText().toString());
        }
        if ((node instanceof ViewGroup)) {
            ViewGroup child = (ViewGroup) node;
            for (int i = 0; i < child.getChildCount(); i++) {
                Map<String, Object> obj = new HashMap<>();
                if (!(child.getChildAt(i) instanceof UIRootView)) {
                    dump(child.getChildAt(i), obj);
                    children.add(obj);
                }
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    static String getImportantForAccessibility(View node) {
        switch (node.getImportantForAccessibility()) {
            case View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS:
                return "noHideDescendants";
            case View.IMPORTANT_FOR_ACCESSIBILITY_YES:
                return "yes";
            case View.IMPORTANT_FOR_ACCESSIBILITY_NO:
                return "no";
            case View.IMPORTANT_FOR_ACCESSIBILITY_AUTO:
                return "auto";
            default:
                return null;
        }
    }

}
