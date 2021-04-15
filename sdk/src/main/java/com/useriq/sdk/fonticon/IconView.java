package com.useriq.sdk.fonticon;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.useriq.sdk.UserIQSDKInternal;

public class IconView extends TextView {
    public IconView(Context context) {
        this(context, null);
    }

    public IconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        Typeface iconfont = UnfoldFontIcon.getTypeface(UserIQSDKInternal.getContext());
        this.setTypeface(iconfont);
    }
}