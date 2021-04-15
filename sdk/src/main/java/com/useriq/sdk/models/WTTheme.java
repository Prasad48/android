package com.useriq.sdk.models;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.useriq.sdk.walkthrough.WTPlacement;
import com.useriq.sdk.walkthrough.WTStepView;

import java.util.ArrayList;
import java.util.Map;

import static com.useriq.sdk.util.Utils.toColor;
import static com.useriq.sdk.util.Utils.toInt;

/**
 * @author sudhakar
 * @created 05-Oct-2018
 */
public class WTTheme {
    public final Type type;
    @ColorInt
    public final int color;
    public final int borderRadius;
    @ColorInt
    public final int bgColor;
    @ColorInt
    public final int borderColor;
    @ColorInt
    public final int titleColor;
    public final int titleSize;
    public final int textSize;
    public final int borderWidth;
    public final int progressOpacity;
    public final WTPlacement placement;

    public WTTheme(Map<String, Object> map) {
        this.type = Type.valueOf((String) map.get("type"));
        Map<String, Object> attrs = (Map<String, Object>) map.get("attrs");
        this.color = toColor(attrs, "color", Color.WHITE);
        this.borderRadius = (int) toInt(attrs, "borderRadius", 4);
        this.bgColor = toColor(attrs, "bgColor", Color.RED);
        this.borderColor = toColor(attrs, "borderColor", Color.RED);
        this.titleColor = toColor(attrs, "titleColor", Color.RED);
        this.titleSize = toInt(attrs, "titleSize", 12);
        this.textSize = toInt(attrs, "textSize", 10);
        this.borderWidth = toInt(attrs, "borderWidth", 2);
        this.progressOpacity = toInt(attrs, "progressOpacity", 1);
        if (map.containsKey("placement")) {
            long location = WTStepView.WTLocation.TOP_CENTER;
            boolean isInside = false;
            long placementType = WTStepView.WTPlacementType.AUTO;
            ArrayList<Long> offset = new ArrayList<>();
            offset.add(0L);
            offset.add(0L);
            Map<String, Object> placement = (Map<String, Object>) map.get("placement");
            if (placement.containsKey("location")) {
                location = (long) placement.get("location");
            }
            if (placement.containsKey("isInside")) {
                isInside = (boolean) placement.get("isInside");
            }
            if (placement.containsKey("type")) {
                placementType = (long) placement.get("type");
            }
            if (placement.containsKey("offset")) {
                offset = (ArrayList<Long>) placement.get("offset");
                ArrayList<Object> offsetList = (ArrayList<Object>) placement.get("offset");
                if (offsetList.get(0) instanceof Double) {
                    offset.set(0, ((Double) offsetList.get(0)).longValue());
                }
                if (offsetList.get(1) instanceof Double) {
                    offset.set(1, ((Double) offsetList.get(1)).longValue());
                }
            }
            this.placement = new WTPlacement((int) placementType, isInside, (int) location, offset);
        } else {
            ArrayList<Long> offset = new ArrayList<>();
            offset.add(0L);
            offset.add(0L);
            this.placement = new WTPlacement(WTStepView.WTPlacementType.AUTO, false, WTStepView.WTLocation.BOTTOM_CENTER, offset);
        }
    }

    public enum Type {
        tooltip,
        number,
        ripple,
    }

}
