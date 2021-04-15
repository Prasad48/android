package com.useriq.sdk.fonticon;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.useriq.sdk.UserIQSDKInternal;

public class IconPaint extends Paint {

    @SuppressWarnings("unused")
    private IconPaint() {
        super();
        setColor(Color.BLACK);
    }

    @SuppressWarnings("unused")
    private IconPaint(int flags) {
        super(flags);
    }

    public IconPaint(Context context, String fontPathFromAssetsDir) {
        this(Typeface.createFromAsset(UserIQSDKInternal.getContext().getAssets(), fontPathFromAssetsDir));
    }

    public IconPaint(Typeface typeface) {
        super(ANTI_ALIAS_FLAG);
        setTypeface(typeface);
        setStyle(Style.FILL);
        setTextAlign(Align.CENTER);
        setUnderlineText(false);
        setAntiAlias(true);
    }
}