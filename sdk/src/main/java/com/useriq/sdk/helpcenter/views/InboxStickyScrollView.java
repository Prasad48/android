package com.useriq.sdk.helpcenter.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * InboxStickyScrollView is a background scrollview with Inbox type of animation
 *
 * <br><br>
 * Derived from following Libs
 *
 * <li>https://github.com/emilsjolander/StickyScrollViewItems</li>
 * <li>https://github.com/zhaozhentao/InboxLayout</li>
 *
 * @author sudhakar
 * @created 22-Oct-2018
 */
public class InboxStickyScrollView extends StickyScrollView {

    private boolean mTouchable = true;
    public boolean needToDrawSmallShadow = false;
    public boolean needToDrawShadow = false;
    protected static final int MAX_MENU_OVERLAY_ALPHA = 185;
    private Drawable mTopSmallShadowDrawable;
    private Drawable mBottomSmallShadowDrawable;
    private Drawable mTopShadow = new ColorDrawable(0xff000000);
    private Drawable mBottomShadow = new ColorDrawable(0xff000000);
    private int smallShadowHeight;

    public InboxStickyScrollView(Context context) {
        this(context, null);
    }

    public InboxStickyScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.scrollViewStyle);
    }

    public InboxStickyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTopSmallShadowDrawable = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0x77101010, 0});
        mBottomSmallShadowDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0x77101010, 0});
        smallShadowHeight = dpToPx(10);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if(mTouchable || !needToDrawSmallShadow) super.dispatchDraw(canvas);
        drawOverlay(canvas);
    }

    protected void drawOverlay(Canvas canvas){
        if(needToDrawShadow) {
            mTopShadow.draw(canvas);
            mBottomShadow.draw(canvas);
        }
        if(needToDrawSmallShadow){
            mTopSmallShadowDrawable.draw(canvas);
            mBottomSmallShadowDrawable.draw(canvas);
        }
    }

    public void drawTopShadow(int top, int height, int alpha){
        mTopShadow.setBounds(0, top, getWidth(), top+height);
        mTopShadow.setAlpha(alpha);
        if(needToDrawSmallShadow) {
            mTopSmallShadowDrawable.setBounds(0, top + height - smallShadowHeight, getWidth(), top + height);
        }
        //invalidate();
    }

    public void drawBottomShadow(int top, int bottom, int alpha){
        mBottomShadow.setBounds(0, top, getWidth(), bottom);
        mBottomShadow.setAlpha(alpha);
        if(needToDrawSmallShadow) {
            mBottomSmallShadowDrawable.setBounds(0, top, getWidth(), top + smallShadowHeight);
        }
        //invalidate();
    }

    public void setTouchable(boolean touchable){
        mTouchable = touchable;
        enableStickyView(mTouchable);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(!mTouchable) {
            /*
             * just eat the touch event
             * */
            return true;
        }
        return super.onTouchEvent(ev);
    }

    public int getScrollRange(){
        return computeVerticalScrollRange();
    }

    public int dpToPx(int dp){
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
