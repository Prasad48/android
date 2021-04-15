package com.useriq.sdk.fonticon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.useriq.sdk.UserIQSDKInternal;

public class IconDrawable extends Drawable {

    final private char fontCode;

    final private Paint paint;

    final private int size;

    final private int padding;

    public IconDrawable(Context ctx, UnfoldFontIcon icon, int size, int padding) {
        this.fontCode = icon.getChar(ctx);
        this.size = size;
        this.padding = padding;
        this.paint = new IconPaint(UnfoldFontIcon.getTypeface(UserIQSDKInternal.getContext()));
    }

    public void setColor(int color) {
        this.paint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(createTextPath(), paint);
    }

    private Path createTextPath() {
        final Path path = createTextPathBase();
        RectF textBounds = createTextBounds(path);
        applyPadding(path, textBounds, createPaddingBounds());
        applyOffset(path, textBounds);
        path.close();
        return path;
    }

    private Path createTextPathBase() {
        Path path = new Path();
        Rect viewBounds = getBounds();
        float textSize = (float) viewBounds.height();
        paint.setTextSize(textSize);
        paint.getTextPath(String.valueOf(fontCode), 0, 1, 0, viewBounds.height(), path);
        return path;
    }

    private RectF createTextBounds(Path path) {
        RectF textBounds = new RectF();
        path.computeBounds(textBounds, true);
        return textBounds;
    }

    private void applyPadding(Path path, RectF textBounds, Rect paddingBounds) {
        final Rect viewBounds = getBounds();
        float deltaWidth = ((float) paddingBounds.width() / textBounds.width());
        float deltaHeight = ((float) paddingBounds.height() / textBounds.height());
        float attenuate = (deltaWidth < deltaHeight) ? deltaWidth : deltaHeight;
        float textSize = paint.getTextSize();
        textSize *= attenuate;
        paint.setTextSize(textSize);
        paint.getTextPath(String.valueOf(fontCode), 0, 1, 0, viewBounds.height(), path);
        path.computeBounds(textBounds, true);
    }

    private void applyOffset(Path path, RectF textBounds) {
        Rect viewBounds = getBounds();
        float startX = viewBounds.centerX() - (textBounds.width() / 2);
        float offsetX = startX - textBounds.left;
        float startY = viewBounds.centerY() - (textBounds.height() / 2);
        float offsetY = startY - (textBounds.top);
        path.offset(offsetX, offsetY);
    }

    private Rect createPaddingBounds() {
        Rect viewBounds = getBounds();
        return new Rect(viewBounds.left + padding, viewBounds.top + padding, viewBounds.right - padding, viewBounds.bottom - padding);
    }

    @Override
    public int getIntrinsicWidth() {
        return size;
    }

    @Override
    public int getIntrinsicHeight() {
        return size;
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}