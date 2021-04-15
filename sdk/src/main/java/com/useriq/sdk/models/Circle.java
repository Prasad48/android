package com.useriq.sdk.models;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Circle extends BaseShape {

    @Override
    public void onSetup(Context context, Paint shapePaint) {}

    @Override
    public void onDraw(Canvas canvas, int x, int y, float radiusSize, int color, int rippleIndex, Paint shapePaint) {
        shapePaint.setColor(color);
        canvas.drawCircle(x, y, radiusSize, shapePaint);
    }
}
