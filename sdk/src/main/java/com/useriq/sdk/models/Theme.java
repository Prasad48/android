package com.useriq.sdk.models;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import java.util.Map;

import static com.useriq.sdk.util.Utils.toColor;
import static com.useriq.sdk.util.Utils.toInt;

/**
 * @author sudhakar
 * @created 30-Sep-2018
 */
public class Theme {
    private static final String COLOR = "color";
    private static final String TEXT_SIZE = "textSize";
    private static final String TITLE_COLOR = "titleColor";
    private static final String TITLE_SIZE = "titleSize";
    private static final String BG_COLOR = "bgColor";
    private static final String BORDER_COLOR = "borderColor";
    private static final String BORDER_RADIUS = "borderRadius";
    private static final String BORDER_WIDTH = "borderWidth";
    private static final String FAB_COLOR = "fabColor";
    private static final String FAB_BG_COLOR = "fabBgColor";

    private static final String CTX_COLOR = "ctxColor";
    private static final String CTX_BG_COLOR = "ctxBgColor";
    private static final String CTX_BTN_COLOR = "ctxBtnColor";
    private static final String CTX_BTN_BORDER_COLOR = "ctxBtnBorderColor";

    @ColorInt
    public final int color;
    public final int textSize;

    @ColorInt
    public final int titleColor;
    public final int titleSize;

    @ColorInt
    public final int bgColor;

    @ColorInt
    public final int borderColor;
    public final int borderRadius;
    public final int borderWidth;

    @ColorInt
    public final int fabColor;
    @ColorInt
    public final int fabBgColor;
    @ColorInt
    public final int ctxColor;
    @ColorInt
    public final int ctxBgColor;
    @ColorInt
    public final int ctxBtnBorderColor;
    @ColorInt
    public final int ctxBtnColor;

    Theme(Map<String, Object> map) {
        this.color = toColor(map, COLOR, Color.RED);
        this.textSize = toInt(map, TEXT_SIZE, 14);
        this.titleSize = toInt(map, TITLE_SIZE, 16);
        this.titleColor = toColor(map, TITLE_COLOR, Color.RED);
        this.bgColor = toColor(map, BG_COLOR, Color.RED);
        this.borderColor = toColor(map, BORDER_COLOR, Color.RED);
        this.borderRadius = toInt(map, BORDER_RADIUS, 4);
        this.borderWidth = toInt(map, BORDER_WIDTH, 2);
        this.fabColor = toColor(map, FAB_COLOR, Color.WHITE);
        this.fabBgColor = toColor(map, FAB_BG_COLOR, Color.BLUE);

        this.ctxColor = toColor(map, CTX_COLOR, Color.DKGRAY);
        this.ctxBtnBorderColor = toColor(map, CTX_BTN_BORDER_COLOR, Color.GRAY);
        this.ctxBtnColor = toColor(map, CTX_BTN_COLOR, Color.BLUE);
        this.ctxBgColor = toColor(map, CTX_BG_COLOR, Color.WHITE);
    }
}
