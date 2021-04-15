package com.useriq.sdk.walkthrough;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.useriq.sdk.R;
import com.useriq.sdk.UserIQSDKInternal;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class ToolTipViewOld extends TextView {

    final public static int BOTTOM = 0, TOP = 1;
    final static int RIGHT_ALIGNED = 0, LEFT_ALIGNED = 1, CENTRED_WRT_ANCHOR = 2;
    public final int mHorizontalPadding;
    public final int mVerticalPadding;
    public
    @VerticalPosition
    int mVerticalPosition;
    @ColorInt
    int mBgColor, mBorderColor, mTextColor;
    private int mArrowHeight;
    private int mArrowWidth;
    private Path mOutlinePath;
    private Path mArrowPath;
    private Rect mAnchorViewBounds;
    private Rect mScreenBounds;
    private Paint mOutlinePaint;
    private Paint mArrowPaint;
    private float mStrokeWidth;

    public ToolTipViewOld(Context context, Rect anchorViewBounds, Rect screenBounds, int arrowHeight, int arrowWidth, String text, @ColorInt int borderColor, @ColorInt int bgColor, @ColorInt int textColor, int textSize) {
        super(context);
        setMinLines(1);
        setMaxLines(2);
        setMaxWidth((int) (screenBounds.right * .8));
        setMinWidth((int) (screenBounds.right * .4));
        mScreenBounds = screenBounds;
        mArrowHeight = arrowHeight;
        mBgColor = bgColor;
        mBorderColor = borderColor;
        mTextColor = textColor;
        if (mBgColor == 0) {
            mBgColor = Color.WHITE;
        }
        if (mTextColor == 0) {
            mTextColor = Color.BLACK;
        }
        if (mBorderColor == 0) {
            mBorderColor = Color.BLACK;
        }
        setText(text);
        // textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, context.getResources().getDisplayMetrics());
        if (textSize != 0) {
            setTextSize(textSize);
        }
        setEllipsize(TextUtils.TruncateAt.END);
        setGravity(Gravity.CENTER_HORIZONTAL);
        mArrowWidth = arrowWidth;
        mScreenBounds = screenBounds;
        mOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokeWidth = UserIQSDKInternal.getInstance().getResources().getDimension(R.dimen.appunfold_outline_stroke);
        mOutlinePaint.setStrokeWidth(mStrokeWidth);
        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        //mArrowPaint.setColor(Color.parseColor("#e97800"));
        mArrowPaint.setColor(mBorderColor);
        //  setTextColor(Color.BLACK);
        setTextColor(mTextColor);

        mHorizontalPadding = UserIQSDKInternal.getInstance().getResources().getDimensionPixelSize(R.dimen.appunfold_tipbox_padding_horizontal);
        mVerticalPadding = UserIQSDKInternal.getInstance().getResources().getDimensionPixelSize(R.dimen.appunfold_tipbox_padding_vertical);
        mAnchorViewBounds = anchorViewBounds;

//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            setOutlineProvider(ViewOutlineProvider.BOUNDS);
//            float elevation = UserIQSDKInternal.getInstance().getResources().getDimension(R.dimen.tooltip_elevation);
//            setElevation(elevation);
//        }


        //setPadding(mHorizontalPadding, mHorizontalPadding, mHorizontalPadding, mHorizontalPadding);
        //   setBackgroundColor(Color.WHITE);

    }






    public void updateAnchorAndScreenBounds(Rect anchorBounds, Rect screenBounds) {
        if (mAnchorViewBounds != null && mAnchorViewBounds.equals(anchorBounds))
            return;
        mScreenBounds = screenBounds;
        mAnchorViewBounds = anchorBounds;
        //setLayoutParams(getFrameLayoutParams());
        //requestLayout();
    }

    public Rect getAnchorBounds() {
        return mAnchorViewBounds;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        mOutlinePath = null;
    }

    @Override
    public int getOffsetForPosition(float x, float y) {
        return super.getOffsetForPosition(x, y);
    }

    public
    @HorizontalPosition
    int findHorizontalPosition(int width) {
        int anchorCentreX = mAnchorViewBounds.centerX();
        int screenCentreX = mScreenBounds.centerX();
        if (anchorCentreX > screenCentreX) {
            if ((mScreenBounds.right - anchorCentreX) >= width / 2) {
                return CENTRED_WRT_ANCHOR;
            } else {
                return RIGHT_ALIGNED;
            }

        } else if (anchorCentreX < screenCentreX) {
            if ((anchorCentreX - mScreenBounds.left) >= width / 2) {
                return CENTRED_WRT_ANCHOR;
            } else {
                return LEFT_ALIGNED;
            }
        } else {
            return CENTRED_WRT_ANCHOR;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + mArrowHeight);
    }

    public
    @VerticalPosition
    int findVerticalPosition(int height) {
        int availableHeightInTop = mAnchorViewBounds.top - mScreenBounds.top;
        if (availableHeightInTop > height) {
            return TOP;
//            return BOTTOM;
        } else {
            return BOTTOM;
        }
    }

    @Override
    public int getGravity() {
        return super.getGravity();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mOutlinePath == null || mArrowPath == null) {
            configureDrawPath(canvas);
        }
        if (mOutlinePath != null && mArrowPath != null) {
            mOutlinePaint.setStyle(Paint.Style.FILL);
            // mOutlinePaint.setColor(Color.parseColor("#ffffff"));
            mOutlinePaint.setColor(mBgColor);
            canvas.drawPath(mOutlinePath, mOutlinePaint);
            mOutlinePaint.setStyle(Paint.Style.STROKE);
            // mOutlinePaint.setColor(Color.parseColor("#e97800"));
            mOutlinePaint.setColor(mBorderColor);
            canvas.drawPath(mOutlinePath, mOutlinePaint);
            canvas.drawPath(mArrowPath, mArrowPaint);
        }
        super.onDraw(canvas);
    }

    public void configureDrawPath(Canvas canvas) {
        float[] arrowStartLoc = new float[2], arrowEndLoc = new float[2], arrowMidLoc = new float[2];
        RectF rectF = new RectF(canvas.getClipBounds());
        mVerticalPosition = findVerticalPosition(getMeasuredHeight());
        getArrowLocations(mVerticalPosition, arrowStartLoc, arrowEndLoc, arrowMidLoc, rectF);
        if (mVerticalPosition == BOTTOM) {
            setPadding(mHorizontalPadding, mVerticalPadding + mArrowHeight, mHorizontalPadding, mVerticalPadding - mArrowHeight);
            rectF.bottom -= mStrokeWidth;
        } else {
            // setPadding(mHorizontalPadding, mHorizontalPadding - mArrowHeight, mHorizontalPadding, mHorizontalPadding + mArrowHeight);
            rectF.top += mStrokeWidth;
        }

        rectF.left += mStrokeWidth;
        rectF.right -= mStrokeWidth;
        mOutlinePath = new Path();
        float tooltipRadius = UserIQSDKInternal.getInstance().getResources().getDimension(R.dimen.appunfold_tooltip_radius);
        mOutlinePath.addRoundRect(rectF, tooltipRadius, tooltipRadius, Path.Direction.CCW);
        //mOutlinePath.addRect(rectF, Path.Direction.CCW);
        mOutlinePath.close();
        mArrowPath = new Path();
        mArrowPath.moveTo(arrowStartLoc[0], arrowStartLoc[1]);
        mArrowPath.lineTo(arrowMidLoc[0], arrowMidLoc[1]);
        mArrowPath.lineTo(arrowEndLoc[0], arrowEndLoc[1]);
        mArrowPath.close();
    }

    private void getArrowLocations(@VerticalPosition int verticalPos, float[] arrowStartLoc, float[] arrowEndLoc, float[] arrowMidLoc, RectF rectF) {
        int anchorCentreX = mAnchorViewBounds.centerX();
        if (verticalPos == BOTTOM) {
            arrowMidLoc[1] = rectF.top;
            rectF.top += mArrowHeight;
            arrowStartLoc[1] = rectF.top;
            arrowEndLoc[1] = rectF.top;
        } else if (verticalPos == TOP) {
            arrowMidLoc[1] = rectF.bottom;
            rectF.bottom -= mArrowHeight;
            arrowStartLoc[1] = rectF.bottom;
            arrowEndLoc[1] = rectF.bottom;
            //  setPadding(mHorizontalPadding, mHorizontalPadding + mArrowHeight, mHorizontalPadding, mHorizontalPadding - mArrowHeight);

        }


        Rect bounds = getBoundsOnScreen(getMeasuredWidth(), getMeasuredHeight());
        arrowStartLoc[0] = anchorCentreX - mArrowWidth / 2 - bounds.left;
        arrowEndLoc[0] = anchorCentreX + mArrowWidth / 2 - bounds.left;
        arrowMidLoc[0] = anchorCentreX - bounds.left;
    }

    public Rect getBoundsOnScreen(int width, int height) {
        Rect bounds;
        @HorizontalPosition int horizontalPos = findHorizontalPosition(width);
        mVerticalPosition = findVerticalPosition(height);
        bounds = new Rect();
        if (horizontalPos == RIGHT_ALIGNED) {
            bounds.right = mScreenBounds.right;
            bounds.left = mScreenBounds.right - width;
        } else if (horizontalPos == LEFT_ALIGNED) {
            bounds.left = mScreenBounds.left;
            bounds.right = mScreenBounds.left + width;
        } else if (horizontalPos == CENTRED_WRT_ANCHOR) {
            bounds.left = mAnchorViewBounds.centerX() - width / 2;
            bounds.right = mAnchorViewBounds.centerX() + width / 2;
        }

        if (mVerticalPosition == BOTTOM) {
            bounds.top = mAnchorViewBounds.bottom;
            bounds.bottom = mAnchorViewBounds.bottom + height;
        } else if (mVerticalPosition == TOP) {
            bounds.bottom = mAnchorViewBounds.top;
            bounds.top = mAnchorViewBounds.top - height;
        }

        return bounds;

    }

    public FrameLayout.LayoutParams getFrameLayoutParams() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setPadding(mHorizontalPadding, mVerticalPadding, mHorizontalPadding, mVerticalPadding);
        //  int[] dimens = getFavorableDimens();
        int specWidth = MeasureSpec.makeMeasureSpec(mScreenBounds.right - mScreenBounds.left, MeasureSpec.AT_MOST);
        int specHeight = MeasureSpec.makeMeasureSpec(mScreenBounds.bottom - mScreenBounds.top, MeasureSpec.AT_MOST);
        measure(specWidth, specHeight);
        Rect bounds = getBoundsOnScreen(getMeasuredWidth(), getMeasuredHeight());
        layoutParams.setMargins(bounds.left, bounds.top, 0, 0);
        layoutParams.gravity = Gravity.NO_GRAVITY;
        return layoutParams;
    }
/*

    public int[] getFavorableDimens() {
        int[] dimens = new int[2];
        dimens[0] = (int) (getPaint().measureText(getText().toString()) + getPaddingLeft() + getPaddingRight());
        float height;
        if (dimens[0] > getMaxWidth()) {
            dimens[0] = getMaxWidth();
            height = (getPaint().descent() - getPaint().ascent()) * 2 + getPaint().getFontSpacing();
            height += getPaddingTop() + getPaddingBottom() + mArrowHeight;
        } else {
            if (dimens[0] < getMinWidth()) {
                dimens[0] = getMinWidth();
            }
            height = getPaint().descent() - getPaint().ascent();
            height += getPaddingTop() + getPaddingBottom() + mArrowHeight;
        }
        dimens[1] = (int) height + 15; //+15 for worst case

        return dimens;
    }
*/

    @Override
    public int getMaxWidth() {
        return super.getMaxWidth();
    }

    @Override
    public int getMinWidth() {
        return super.getMinWidth();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RIGHT_ALIGNED, LEFT_ALIGNED, CENTRED_WRT_ANCHOR})
    @interface HorizontalPosition {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({BOTTOM, TOP})
    @interface VerticalPosition {

    }
}
