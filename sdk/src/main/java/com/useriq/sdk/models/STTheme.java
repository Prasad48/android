package com.useriq.sdk.models;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import java.util.Map;

import static com.useriq.sdk.util.Utils.toColor;
import static com.useriq.sdk.util.Utils.toInt;

/**
 * @author sudhakar
 * @created 09-Oct-2018
 */
public class STTheme {
    @ColorInt public final int borderColor;
    @ColorInt public final int textColor;
    @ColorInt public final int bgColor;
    public final int textSize;

    STTheme(Map<String, Object> map) {
        this.borderColor = toColor(map, "borderColor", Color.RED);
        this.textColor = toColor(map, "textColor", Color.RED);
        this.bgColor = toColor(map, "bgColor", Color.RED);
        this.textSize = toInt(map, "textSize", 14);
    }
}
