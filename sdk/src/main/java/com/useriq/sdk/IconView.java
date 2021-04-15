package com.useriq.sdk;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class IconView extends TextView {
    private static final String FONT = "uiq_font.ttf";
    private static Typeface iconFont;

    static Typeface getIconFont(Context ctx) {
        if(iconFont != null) return iconFont;
        return (iconFont = Typeface.createFromAsset(ctx.getAssets(), FONT));
    }

    public IconView(Context context) {
        this(context, null);
    }

    public IconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setTypeface(getIconFont(context));
    }
}