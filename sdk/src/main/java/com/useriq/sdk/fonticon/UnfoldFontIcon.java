package com.useriq.sdk.fonticon;

import android.content.Context;
import android.graphics.Typeface;

import com.useriq.sdk.R;

public enum UnfoldFontIcon {

    CLOSE(R.string.uiq_ic_close),
    ARROW_RIGHT(R.string.uiq_ic_arrow_right),
    SEARCH(R.string.uiq_ic_search),
//    WALKTHROUGH(R.string.uiq_ic_walkthrough),
    MODAL(R.string.uiq_ic_modals),
    WALKTHROUGH(R.string.uiq_ic_overlays),
    SCREEN_TOUR(R.string.uiq_ic_screentours),
    HELP(R.string.uiq_ic_help),
    INBOX(R.string.uiq_ic_inbox),
    KEYBOARD_ARROW_RIGHT(R.string.uiq_ic_keyboard_arrow_right),
    KEYBOARD_ARROW_LEFT(R.string.uiq_ic_keyboard_arrow_left),
    KEYBOARD_ARROW_DOWN(R.string.uiq_ic_keyboard_arrow_down),
    KEYBOARD_ARROW_UP(R.string.uiq_ic_keyboard_arrow_up);


    private static final String FONT = "uiq_font.ttf";

    public final int resId;

    UnfoldFontIcon(int resId) {
        this.resId = resId;
    }

    public static Typeface getTypeface(Context ctx) {
        return Typeface.createFromAsset(ctx.getAssets(), FONT);
    }

    public static String getIconString(Context ctx, int resId) {
        return ctx.getResources().getString(resId);
    }

    char getChar(Context ctx) {
        return ctx.getResources().getString(resId).charAt(0);
    }
}