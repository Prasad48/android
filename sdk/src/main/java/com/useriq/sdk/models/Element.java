package com.useriq.sdk.models;

import android.graphics.Rect;
import android.view.View;
import android.widget.TextView;

import com.useriq.sdk.UIRootView;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.ViewNode;

import java.util.List;
import java.util.Map;

/**
 * @author sudhakar
 * @created 30-Sep-2018
 */
public class Element {
    /**
     * id is used for analytics tracking
     */
    public final String id;
    /**
     * key is used in predicates for screen matching
     */
    public final String key;
    public final Rect bounds;
    public final ElProps elProps;

    Element(Map<String, Object> obj) {
        id = (String) obj.get("id");
        key = (String) obj.get("key");

        List<Long> list = (List<Long>) obj.get("bounds");
        this.bounds = list == null ? new Rect() : new Rect(
                list.get(0).intValue(),
                list.get(1).intValue(),
                list.get(2).intValue(),
                list.get(3).intValue());

        elProps = new ElProps((Map<String, Object>) obj.get("props"));
    }

    public boolean matches(int wIndex, ViewNode viewNode) {
        View target = viewNode.getView();

        if (!viewNode.isVisible()) return false;

        if (target instanceof UIRootView) return false;

        if (elProps.wIndex != wIndex) return false;

        if (!elProps.matchesID(target)) return false;

        if (elProps.testID != null && ! elProps.testID.equals(viewNode.getTestID())) {
            return false;
        }

        if (elProps.contentDesc != null && !elProps.contentDesc.equals(target.getContentDescription())) {
            return false;
        }

        if (elProps.text != null) {
            boolean matches = target instanceof TextView
                    && ((TextView) target).getText().toString().equals(elProps.text);
            if (!matches) return false;
        }

        if (elProps.bgColor != null && elProps.bgColor != viewNode.getBgColor()) {
            return false;
        }

        if(elProps.clickable != null) {
            if(target.isClickable() != elProps.clickable) return false;
        }

        if(elProps.longClickable != null) {
            if(target.isLongClickable() != elProps.longClickable) return false;
        }

        if(elProps.enabled != null) {
            if(target.isEnabled() != elProps.enabled) return false;
        }

        if(elProps.isVisible != null) {
            boolean isVisible = target.getVisibility() == View.VISIBLE;
            if(isVisible != elProps.isVisible) return false;
        }

        // Get canonicalName is little costlier op. So do it last to improve perf
        if (elProps.cls != null && !elProps.cls.equals(viewNode.getClassName())) {
            return false;
        }

        long start = System.nanoTime();
        boolean visibleOnScreen = viewNode.isVisibleOnScreen();
        UserIQSDKInternal.getAnalyticsManager().onPref(start, "isVisibleOnScreen",
                "elId", id);

        return visibleOnScreen;
    }
}