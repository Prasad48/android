package com.useriq.sdk.walkthrough;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

public class WalkthroughImage extends View {
    Bitmap mScreen;

    public WalkthroughImage(Context context, Bitmap screen) {
        super(context);
        mScreen = screen;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mScreen, 0, 0, null);

        super.onDraw(canvas);
    }


}
