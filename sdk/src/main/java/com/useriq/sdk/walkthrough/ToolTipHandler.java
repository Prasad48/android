package com.useriq.sdk.walkthrough;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.useriq.sdk.R;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.models.WTTheme;
import com.useriq.sdk.util.PathUtil;
import com.useriq.sdk.util.Utils;

import static com.useriq.sdk.util.UnitUtil.dpToPx;

public class ToolTipHandler {

    private View toolTipView;
    private TextView tvTitle, tvDesc;
    private Button btnClose, btnNext, btnPrev;
    private float toolTipRadius;
    private int arrowHeight, arrowWidth, borderWidth;
    private ToolTipPositionHelper toolTipPositionHelper;
    private int toolTipWidth;
    private WTPlacement placement;

    ToolTipHandler() {
        Resources res = UserIQSDKInternal.getInstance().getResources();
        this.toolTipRadius = res.getDimension(R.dimen.appunfold_tooltip_radius);
        this.borderWidth = dpToPx(1);
        this.arrowHeight = res.getDimensionPixelSize(R.dimen.appunfold_tipbox_arrow_height);
        this.arrowWidth = res.getDimensionPixelSize(R.dimen.appunfold_tipbox_arrow_width);
        LayoutInflater inflater = LayoutInflater.from(UserIQSDKInternal.getContext());
        toolTipView = inflater.cloneInContext(UserIQSDKInternal.getContext()).inflate(R.layout.appunfold_tooltip, null);

        tvTitle = toolTipView.findViewById(R.id.tv_title);
        tvDesc = toolTipView.findViewById(R.id.tv_desc);
        btnClose = toolTipView.findViewById(R.id.btn_close);
        btnClose.setText("\u2715");
        btnNext = toolTipView.findViewById(R.id.btn_next);
        btnPrev = toolTipView.findViewById(R.id.btn_prev);
        setContentViewPadding();
    }

    private void setContentViewPadding() {
        int padding = toolTipView.getPaddingBottom();
        toolTipView.setPadding(padding + borderWidth, padding + this.arrowHeight + borderWidth / 2, padding + borderWidth, padding + this.arrowHeight + borderWidth / 2);
    }

    void addToolTip(String titleText, String descriptionText, View.OnClickListener nextClickListener, View.OnClickListener prevClickListener, View.OnClickListener closeClickListener,
                    final WTTheme theme, final View targetView, final ViewGroup rootView, final Rect uiRootViewRect) {
        this.borderWidth = theme.borderWidth;
        this.placement = theme.placement;
        if (UserIQSDKInternal.getCurrActivity() != null && Utils.isTablet(UserIQSDKInternal.getCurrActivity()))
            toolTipWidth = dpToPx(310);
        else
            toolTipWidth = dpToPx(270);
        updateToolTipContent(titleText, descriptionText, theme, nextClickListener, prevClickListener, closeClickListener);
        animateInToolTipView();
        final Rect anchorRect = PathUtil.getPositionForView(targetView);

        anchorRect.left -= uiRootViewRect.left;
        anchorRect.top -= uiRootViewRect.top;
        anchorRect.right = anchorRect.left + targetView.getWidth();
        anchorRect.bottom = anchorRect.top + targetView.getHeight();

        if (targetView.getWindowToken() != null) {
            updateToolTipPositionAndBg(theme.borderColor, theme.bgColor, anchorRect, rootView.getContext(), placement, uiRootViewRect);
            rootView.addView(toolTipView, getFrameLayoutParams());
        } else {
            targetView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        targetView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        targetView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    updateToolTipPositionAndBg(theme.borderColor, theme.bgColor, anchorRect, rootView.getContext(), placement, uiRootViewRect);
                    rootView.addView(toolTipView, getFrameLayoutParams());
                }
            });
        }
    }

    public void clear() {
        ViewParent parent = toolTipView.getParent();

        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(toolTipView);
        }

        btnNext.setOnClickListener(null);
        btnPrev.setOnClickListener(null);
        btnClose.setOnClickListener(null);
    }

    private void updateToolTipContent(String titleText, String descriptionText, WTTheme theme, View.OnClickListener nextClickListener, View.OnClickListener prevClickListener, View.OnClickListener closeClickListener) {
        toolTipView.setVisibility(View.VISIBLE);

        tvTitle.setText(titleText);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, theme.titleSize);
        tvTitle.setTextColor(theme.titleColor);

        tvDesc.setText(descriptionText);
        tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, theme.textSize);
        tvDesc.setTextColor(theme.color);

        btnNext.setTextColor(Color.WHITE);
        btnClose.setTextColor(Color.WHITE);
        btnPrev.setTextColor(Color.WHITE);
        btnNext.setOnClickListener(nextClickListener);
        btnPrev.setOnClickListener(prevClickListener);
        btnClose.setOnClickListener(closeClickListener);
        btnNext.setVisibility(View.VISIBLE);
        btnPrev.setVisibility(View.VISIBLE);
        btnClose.setVisibility(View.VISIBLE);
        if (closeClickListener == null) btnClose.setVisibility(View.INVISIBLE);
        if (nextClickListener == null) btnNext.setVisibility(View.GONE);
        if (prevClickListener == null) btnPrev.setVisibility(View.GONE);
    }

    private void animateInToolTipView() {
        toolTipView.setVisibility(View.VISIBLE);
        toolTipView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    toolTipView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    toolTipView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                PropertyValuesHolder translationValueHoder = null;
                int margin = UserIQSDKInternal.getInstance().getResources().getDimensionPixelOffset(R.dimen.appunfold_animation_offset);
                if (toolTipPositionHelper.verticalPosition == ToolTipViewOld.TOP) {
                    translationValueHoder = PropertyValuesHolder.ofFloat("translationY", -1 * margin, 0);
                } else if (toolTipPositionHelper.verticalPosition == ToolTipViewOld.BOTTOM) {
                    translationValueHoder = PropertyValuesHolder.ofFloat("translationY", margin, 0);
                }
                PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofFloat("alpha", .2f, 1f);
                ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, translationValueHoder, alphaHolder);
                objectAnimator.setInterpolator(new DecelerateInterpolator());
                objectAnimator.setDuration(200);
                objectAnimator.start();
            }
        });
    }

    private void updateToolTipPositionAndBg(@ColorInt int borderColor, @ColorInt int bgColor, Rect anchorViewPosition, Context ctx, WTPlacement placement, Rect uiRootViewRect) {
        Rect toolTipBounds = getBoundsOfUpdatedContentView(uiRootViewRect);
        toolTipPositionHelper = new ToolTipPositionHelper(toolTipBounds, uiRootViewRect, anchorViewPosition);
        toolTipPositionHelper.findToolTipPosition(placement);
        updateToolTipBg(borderColor, bgColor, anchorViewPosition);
    }

    private FrameLayout.LayoutParams getFrameLayoutParams() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(toolTipWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(toolTipPositionHelper.toolTipPosition.left, toolTipPositionHelper.toolTipPosition.top, 0, 0);
        layoutParams.gravity = Gravity.NO_GRAVITY;
        return layoutParams;
    }

    private Rect getBoundsOfUpdatedContentView(Rect screenBounds) {
        int specWidth = View.MeasureSpec.makeMeasureSpec(toolTipWidth, View.MeasureSpec.EXACTLY);
        int specHeight = View.MeasureSpec.makeMeasureSpec(screenBounds.bottom - screenBounds.top, View.MeasureSpec.AT_MOST);
        this.toolTipView.measure(specWidth, specHeight);
        return new Rect(0, 0, this.toolTipView.getMeasuredWidth(), this.toolTipView.getMeasuredHeight());
    }

    private void updateToolTipBg(@ColorInt int borderColor, @ColorInt int bgColor, Rect anchorViewPosition) {
        ToolTipBgDrawable toolTipBgDrawable = ToolTipBgDrawable.getBgDrawable(borderColor, bgColor, borderWidth, toolTipRadius, toolTipPositionHelper.verticalPosition,
                anchorViewPosition.centerX() - arrowWidth / 2 - toolTipPositionHelper.toolTipPosition.left, arrowHeight, arrowWidth, placement);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            toolTipView.setBackground(toolTipBgDrawable);
        else toolTipView.setBackgroundDrawable(toolTipBgDrawable);
    }

}
