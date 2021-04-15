package com.useriq.sdk.walkthrough;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.View;

import com.useriq.sdk.R;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.util.ScreenBoundsUtil;
import com.useriq.sdk.util.UnitUtil;

import java.lang.ref.WeakReference;

public class NumberDot extends Drawable {

    private Paint mPaint;
    private float mRadius;
    private int mSize;
    private WeakReference<View> mTargetView;
    private String mNo;
    private Paint mTextPaint;
    private Paint mBorderPaint;
    private float mBorderWidth;
    private WTPlacement placement;
    public float cx;
    public float cy;
    private int mWidth;
    private boolean forOverlay;

    public NumberDot(View targetView, @ColorInt int bgColor, int number, @ColorInt int textColor, @ColorInt int borderColor, float radius, WTPlacement placement, boolean forOverlay, Rect uiRootViewRect) {
        this.placement = placement;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(bgColor);
        mPaint.setAntiAlias(true);
        mBorderWidth = UserIQSDKInternal.getInstance().getResources().getDimension(R.dimen.appunfold_dot_border_width);
        mSize = (int) (mRadius * 2 + mBorderWidth * 2);
        mWidth = targetView.getWidth();
        mNo = String.valueOf(number);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (textColor == Color.TRANSPARENT) {
            textColor = Color.BLACK;
        }
        mTextPaint.setColor(textColor);
        mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (borderColor == 0) {
            borderColor = Color.BLACK;
        }
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStyle(Paint.Style.FILL);
        this.forOverlay = forOverlay;

        mTargetView = new WeakReference<>(targetView);
        mRadius = UnitUtil.dpToPx(radius);
        mTextPaint.setTextSize(mRadius);
        if (mRadius == 0) {
            mTextPaint.setTextSize(UserIQSDKInternal.getContext().getResources().getDimension(R.dimen.appunfold_dot_text_size));
            mRadius = UserIQSDKInternal.getContext().getResources().getDimension(R.dimen.appunfold_dot_radius_lower_limit);
        }

        int[] loc = new int[2];
        targetView.getLocationInWindow(loc);
        if (placement.type == WTStepView.WTPlacementType.MANUAL) {
            switch (placement.location) {
                case WTStepView.WTLocation.TOP_LEFT: {
                    cx = loc[0];
                    cy = loc[1];
                    break;
                }
                case WTStepView.WTLocation.TOP_CENTER: {
                    cx = loc[0] + targetView.getWidth() / 2;
                    cy = loc[1];
                    break;
                }
                case WTStepView.WTLocation.TOP_RIGHT: {
                    cx = loc[0] + targetView.getWidth();
                    cy = loc[1];
                    break;
                }
                case WTStepView.WTLocation.CENTER_LEFT: {
                    cx = loc[0];
                    cy = loc[1] + targetView.getHeight() / 2;
                    break;
                }
                case WTStepView.WTLocation.CENTER_CENTER: {
                    cx = loc[0] + targetView.getWidth() / 2;
                    cy = loc[1] + targetView.getHeight() / 2;
                    break;
                }
                case WTStepView.WTLocation.CENTER_RIGHT: {
                    cx = loc[0] + targetView.getWidth();
                    cy = loc[1] + targetView.getHeight() / 2;
                    break;
                }
                case WTStepView.WTLocation.BOTTOM_LEFT: {
                    cx = loc[0];
                    cy = loc[1] + targetView.getHeight();
                    break;
                }
                case WTStepView.WTLocation.BOTTOM_CENTER: {
                    cx = loc[0] + targetView.getWidth() / 2;
                    cy = loc[1] + targetView.getHeight();
                    break;
                }
                case WTStepView.WTLocation.BOTTOM_RIGHT: {
                    cx = loc[0] + targetView.getWidth();
                    cy = loc[1] + targetView.getHeight();
                    break;
                }
            }
        } else {
            Rect screenBounds = ScreenBoundsUtil.getScreenBoundWithoutNav(targetView.getContext());
            int availableSpaceInBottom = screenBounds.bottom - (loc[1] + targetView.getHeight());
            int availableSpaceInTop = loc[1] - screenBounds.top;
            int availableSpaceInLeft = loc[0] - screenBounds.left;
            int availableSpaceInRight = screenBounds.right - (loc[0] + targetView.getWidth());

            if (availableSpaceInBottom >= mSize) {
                cx = loc[0] + targetView.getWidth() / 2;
                cy = loc[1] + targetView.getHeight();
            } else if (availableSpaceInTop >= mSize) {
                cx = loc[0] + targetView.getWidth() / 2;
                cy = loc[1];
            } else if (availableSpaceInLeft >= mSize) {
                cx = loc[0];
                cy = loc[1] + targetView.getHeight() / 2;
            } else if (availableSpaceInRight >= mSize) {
                cx = loc[0] + targetView.getWidth();
                cy = loc[1] + targetView.getHeight() / 2;
            } else {
                cx = loc[0] + targetView.getWidth() / 2;
                cy = loc[1] + targetView.getHeight() / 2;
            }
        }
        cx += placement.offset.get(0);
        cy += placement.offset.get(1) - ScreenBoundsUtil.getStatusHeight(targetView.getContext());

        cx -= uiRootViewRect.left;
        cy -= uiRootViewRect.top;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // mPath.moveTo();
        if (mNo != null) {
            if (forOverlay){
                renderForOverLay(canvas);
                return;
            }
            canvas.drawCircle(cx, cy, mRadius + mBorderWidth, mBorderPaint);
            canvas.drawCircle(cx, cy, mRadius, mPaint);
            canvas.drawText(mNo, cx - mTextPaint.measureText(mNo) / 2, cy + (-1 * (mTextPaint.ascent() + mTextPaint.descent()) / 2), mTextPaint);
        }
    }

    private void renderForOverLay(Canvas canvas) {
        canvas.drawCircle(mWidth - mRadius - mBorderWidth, mRadius + mBorderWidth, mRadius + mBorderWidth, mBorderPaint);
        canvas.drawCircle(mWidth - mRadius - mBorderWidth, mRadius + mBorderWidth, mRadius, mPaint);
        canvas.drawText(mNo, mWidth - mRadius - mBorderWidth - mTextPaint.measureText(mNo) / 2, mRadius + mBorderWidth + (-1 * (mTextPaint.ascent() + mTextPaint.descent()) / 2), mTextPaint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}


