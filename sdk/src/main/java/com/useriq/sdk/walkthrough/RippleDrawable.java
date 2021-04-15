package com.useriq.sdk.walkthrough;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.useriq.sdk.util.UnitUtil;

import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.LinkedList;

public class RippleDrawable extends Drawable implements Animatable {
    private static final int DEFAULT_RIPPLE_TO_COLOR = Color.parseColor("#00FFFFFF");
    private static final int DEFAULT_RIPPLE_FROM_COLOR = Color.parseColor("#FFF44336");
    private static final int DEFAULT_RIPPLE_DURATION = 3000;
    private static final int DEFAULT_RIPPLE_COUNT = 3;
    ValueAnimator mValueAnimator;
    int mRippleCount;
    int mRippleDuration;
    int mRippleToColor;
    int mRippleFromColor;
    Deque<RippleEntry> mRippleEntries;
    float mLastAnimatedValue;
    float mMaxRadius;
    Paint mShapePaint;
    float mWidth;
    WeakReference<View> mTargetView;

    public RippleDrawable(View targetView, int color, int radius) {
        super();
        mShapePaint = new Paint();
        mShapePaint.setAntiAlias(true);
        mShapePaint.setDither(true);
        mShapePaint.setStyle(Paint.Style.FILL);
        mWidth = targetView.getWidth();
        mTargetView = new WeakReference<>(targetView);
        mRippleCount = DEFAULT_RIPPLE_COUNT;
        mRippleDuration = DEFAULT_RIPPLE_DURATION;
        mRippleFromColor = color;
        if (mRippleFromColor == Color.TRANSPARENT) {
            mRippleFromColor = DEFAULT_RIPPLE_FROM_COLOR;
        }
        mRippleToColor = DEFAULT_RIPPLE_TO_COLOR;
        mRippleEntries = new LinkedList<>();
        if (radius == 0) {
            radius = 20;
        }
        mMaxRadius = UnitUtil.dpToPx(radius);
        for (int i = 0; i < mRippleCount; i++) {
            mRippleEntries.add(new RippleEntry(i));
        }

        mValueAnimator = ValueAnimator.ofFloat(0f, 1f);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setDuration(DEFAULT_RIPPLE_DURATION);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                update((Float) animation.getAnimatedValue());
            }
        });
    }

    public static int evaluateTransitionColor(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB))));
    }


    private void update(float animatedValue) {
        RippleEntry firstRippleEntry = mRippleEntries.peekFirst();
        float currentMultiplier = (1f / mRippleCount) * firstRippleEntry.mIndex;
        if (currentMultiplier == 0 && mLastAnimatedValue > animatedValue) {
            RippleEntry pop = mRippleEntries.pop();
            pop.mRender = false;
            mRippleEntries.add(pop);
        } else if (animatedValue >= currentMultiplier && mLastAnimatedValue < currentMultiplier) {
            RippleEntry pop = mRippleEntries.pop();
            pop.mRender = false;
            mRippleEntries.add(pop);
        }
        for (RippleEntry rippleEntry : mRippleEntries) {
            float actualAnimatedValue;
            currentMultiplier = (1f / mRippleCount) * rippleEntry.mIndex;
            if (currentMultiplier == 0) {
                actualAnimatedValue = animatedValue;
            } else if (currentMultiplier > animatedValue) {
                actualAnimatedValue = (1 - currentMultiplier) + animatedValue;
            } else {
                actualAnimatedValue = animatedValue - currentMultiplier;
            }
            rippleEntry.mColor = evaluateTransitionColor(actualAnimatedValue, mRippleFromColor, mRippleToColor);
            rippleEntry.mRadius = mMaxRadius * actualAnimatedValue;
            rippleEntry.mRender = true;
        }
        mLastAnimatedValue = animatedValue;
        if (mTargetView.get() != null && mTargetView.get().getWindowToken() != null) {
            mTargetView.get().invalidate();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        for (RippleEntry rippleEntry : mRippleEntries) {
            if (rippleEntry.mRender) {
                mShapePaint.setColor(rippleEntry.mColor);
                canvas.drawCircle(mWidth - mMaxRadius, mMaxRadius, rippleEntry.mRadius, mShapePaint);
            }
        }

    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void start() {
        mValueAnimator.start();

    }

    @Override
    public void stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mValueAnimator.pause();
        }

    }

    @Override
    public boolean isRunning() {
        return mValueAnimator.isRunning();
    }

    class RippleEntry {
        public final float mIndex;
        public int mColor;
        public float mRadius;
        public boolean mRender;

        RippleEntry(float index) {
            this.mIndex = index;
        }
    }

}
