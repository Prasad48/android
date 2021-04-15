package com.useriq.sdk.walkthrough;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

public class ToolTipBgDrawable extends LayerDrawable {

    @ToolTipPositionHelper.VerticalPositionWRTAnchor
    int tooltipVerticalPosition;
    int xOffset, borderWidth, arrowHeight, arrowWidth;
    @ColorInt
    int borderColor, bgColor;
    Paint arrowFillPaint, arrowOutlinePaint;
    Path fillArrowPath, strokeArrowPath;
    WTPlacement placement;

    /**
     * Creates a new layer drawable with the list of specified layers.
     *
     * @param layers a list of drawables to use as layers in this new drawable,
     *               must be non-null
     */
    private ToolTipBgDrawable(@NonNull Drawable[] layers, @ColorInt int borderColor, @ColorInt int bgColor, int borderWidth, @ToolTipPositionHelper.VerticalPositionWRTAnchor int tooltipVerticalPosition, int xOffset, int arrowHeight, int arrowWidth, WTPlacement placement) {
        super(layers);
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.arrowHeight = arrowHeight;
        this.arrowWidth = arrowWidth;
        this.tooltipVerticalPosition = tooltipVerticalPosition;
        this.xOffset = xOffset;
        this.bgColor = bgColor;
        setLayerInset(0, 0, arrowHeight, 0, arrowHeight);
        arrowFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowFillPaint.setStyle(Paint.Style.FILL);
        arrowFillPaint.setColor(borderColor);
        arrowOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowOutlinePaint.setStyle(Paint.Style.STROKE);
        arrowOutlinePaint.setStrokeWidth(borderWidth);
        arrowOutlinePaint.setColor(borderColor);
        this.placement = placement;
    }

    public static ToolTipBgDrawable getBgDrawable(@ColorInt int borderColor, @ColorInt int bgColor, int borderWidth, float toolTipRadius, @ToolTipPositionHelper.VerticalPositionWRTAnchor int tooltipVerticalPosition, int xOffsetForArrow, int arrowHeight, int arrowWidth, WTPlacement placement) {
        Drawable[] layers = new Drawable[1];
        layers[0] = getRoundedRect(borderColor, bgColor, toolTipRadius, borderWidth);
        return new ToolTipBgDrawable(layers, borderColor, bgColor, borderWidth, tooltipVerticalPosition, xOffsetForArrow, arrowHeight, arrowWidth, placement);
    }

    private static GradientDrawable getRoundedRect(@ColorInt int borderColor, @ColorInt int bgColor, float toolTipRadius, int borderWidth) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(toolTipRadius);
        gradientDrawable.setStroke(borderWidth, borderColor);
        gradientDrawable.setColor(bgColor);
        return gradientDrawable;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (fillArrowPath == null) {
            fillArrowPath = new Path();
            setArrowPath(canvas.getHeight());
        }
        canvas.drawPath(fillArrowPath, arrowFillPaint);
        canvas.drawPath(strokeArrowPath, arrowOutlinePaint);
    }

    private void setArrowPath(int canvasHeight) {
        fillArrowPath = new Path();
        strokeArrowPath = new Path();
        if (tooltipVerticalPosition == ToolTipPositionHelper.BOTTOM) {
            setUpArrowPath();
            setUpStrokeArrowPath();
        } else {
            setDownArrowPath(canvasHeight);
            setDownStrokeArrowPath(canvasHeight);
        }
    }

    private void setUpArrowPath() {
        fillArrowPath.moveTo(placement.offset.get(0) + xOffset - borderWidth / 2, arrowHeight + borderWidth);
        fillArrowPath.lineTo(placement.offset.get(0) + xOffset + arrowWidth / 2, borderWidth / 2);
        fillArrowPath.lineTo(placement.offset.get(0) + xOffset + arrowWidth + borderWidth / 2, arrowHeight + borderWidth);
    }

    private void setUpStrokeArrowPath() {
        strokeArrowPath.moveTo(placement.offset.get(0) + xOffset - borderWidth / 2, arrowHeight + borderWidth / 2);
        strokeArrowPath.lineTo(placement.offset.get(0) + xOffset + arrowWidth / 2, borderWidth / 2);
        strokeArrowPath.lineTo(placement.offset.get(0) + xOffset + arrowWidth + borderWidth / 2, arrowHeight + borderWidth / 2);
    }

    private void setDownArrowPath(int canvasHeight) {
        fillArrowPath.moveTo(placement.offset.get(0) + xOffset - borderWidth / 2, canvasHeight - arrowHeight - borderWidth);
        fillArrowPath.lineTo(placement.offset.get(0) + xOffset + arrowWidth / 2, canvasHeight);
        fillArrowPath.lineTo(placement.offset.get(0) + xOffset + arrowWidth + borderWidth / 2, canvasHeight - arrowHeight - borderWidth);

    }

    private void setDownStrokeArrowPath(int canvasHeight) {
        strokeArrowPath.moveTo(placement.offset.get(0) + xOffset - borderWidth / 2, canvasHeight - arrowHeight - borderWidth / 2);
        strokeArrowPath.lineTo(placement.offset.get(0) + xOffset + arrowWidth / 2, canvasHeight - borderWidth / 2);
        strokeArrowPath.lineTo(placement.offset.get(0) + xOffset + arrowWidth + borderWidth / 2, canvasHeight - arrowHeight - borderWidth / 2);

    }

}
