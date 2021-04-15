package com.useriq.sdk.screentour;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public abstract class ShowcaseView extends FrameLayout {
    List<Element> mElements;
    @ColorInt
    int mColor;
    Context mContext;
    Canvas mCanvas;
    Bitmap mBitmap;
    int mOldHeight, mOldWidth;
    Paint mEraserPaint, mBorderPaint;
    int mPaddingFromTarget;
    boolean mDashedBorder;

    public ShowcaseView(@NonNull Context context, List<Element> elements, int paddingFromTarget, @ColorInt int bgColor, @ColorInt int borderColor, boolean dashedBorder) {
        super(context);
       /*
       TODO: remove after checking that it is not required
        addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                AppunfoldSDKInternal.getInstance().mUxController.setFab();
                AppunfoldSDKInternal.getInstance().mUxController.mScreenChangeDetector.layoutMightHaveChanged();
            }
        });*/
        init(context, elements, paddingFromTarget, bgColor, borderColor, dashedBorder);
    }

    public int getPreferredRadius(Rect bounds) {
        int centerX = bounds.centerX();
        int centerY = bounds.centerY();
        return (int) Math.sqrt((Math.pow(centerX - bounds.left, 2) + Math.pow(centerY - bounds.top, 2))) + 1;
    }



    private void init(Context context, List<Element> elements, int paddingFromTarget, @ColorInt int bgColor, @ColorInt int borderColor, boolean dashedBorder) {
        setWillNotDraw(false);
        setVisibility(INVISIBLE);
        mElements = elements;
        mColor = bgColor;
        mContext = context;
        mPaddingFromTarget = -1 * paddingFromTarget;
        mEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEraserPaint.setColor(0xFFFFFFFF);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mDashedBorder = dashedBorder;
        mBorderPaint.setStrokeWidth(1);
        mBorderPaint.setStrokeCap(Paint.Cap.ROUND);

        if (dashedBorder) {
            mBorderPaint.setPathEffect(new DashPathEffect(new float[]{5, 17}, 0));
        }
    }

    protected void setBorderWidth(int width) {
        mBorderPaint.setStrokeWidth(width);
        mBorderPaint.setStrokeCap(Paint.Cap.ROUND);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (width <= 0 || height <= 0) return;
        if (mBitmap == null || mCanvas == null || mOldHeight != height || mOldWidth != width) {
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mOldHeight = height;
            mOldWidth = width;
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mCanvas.drawColor(mColor);
            for (int i = 0; i < mElements.size(); i++) {
                Element element = mElements.get(i);
                switch (element.shape) {
                    case Element.CIRCLE:
                        if (!mDashedBorder) {
                            mCanvas.drawCircle(element.bounds.centerX(), element.bounds.centerY(), getPreferredRadius(element.bounds) + mPaddingFromTarget, mEraserPaint);
                        }
                        mCanvas.drawCircle(element.bounds.centerX(), element.bounds.centerY(), getPreferredRadius(element.bounds) + mPaddingFromTarget, mBorderPaint);
                        break;
                    case Element.RECT:
                        Rect newRect = new Rect(element.bounds);
                        newRect.inset(mPaddingFromTarget, mPaddingFromTarget);
                        if (!mDashedBorder) {
                            mCanvas.drawRect(newRect, mEraserPaint);
                        }
                        mCanvas.drawRect(newRect, mBorderPaint);
                        break;
                }
            }
        }
        canvas.drawBitmap(mBitmap, 0, 0, null);
        super.onDraw(canvas);
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Element.CIRCLE, Element.RECT})
    @interface Shape {
    }

    public static class Element {
        public final static int CIRCLE = 0;
        public final static int RECT = 1;
        public final
        @Shape
        int shape;
        public final Rect bounds;
        public final String desc;

        public Element(@Shape int shape, Rect bounds, String desc) {
            this.shape = shape;
            this.bounds = bounds;
            this.desc = desc;
        }
    }
}
