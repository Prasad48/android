package com.useriq.sdk.walkthrough;

import android.graphics.Rect;
import android.support.annotation.IntDef;

import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.util.ScreenBoundsUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ToolTipPositionHelper {
    final public static int BOTTOM = 0, TOP = 1;
    final static int RIGHT_ALIGNED = 0, LEFT_ALIGNED = 1, CENTRED_WRT_ANCHOR = 2;
    @VerticalPositionWRTAnchor
    int verticalPosition;
    @HorizontalPositionInScreen
    private int horizontalPosition;
    Rect toolTipPosition;
    private Rect anchorViewPosition, screenBounds, toolTipBounds;

    ToolTipPositionHelper(Rect toolTipBounds, Rect screenBounds, Rect anchorViewPosition) {
        this.anchorViewPosition = anchorViewPosition;
        this.screenBounds = screenBounds;
        this.toolTipBounds = toolTipBounds;
    }

    Rect findToolTipPosition(WTPlacement placement) {
        updateToolTipPositionInScreenBounds(placement);
        return toolTipPosition;
    }

    private Rect updateToolTipPositionInScreenBounds(WTPlacement placement) {
        if (placement.type == WTStepView.WTPlacementType.AUTO) {
            verticalPosition = findVerticalPosition(anchorViewPosition, screenBounds, toolTipBounds.height());
        } else {
            if (placement.location == WTStepView.WTLocation.TOP_CENTER) {
                verticalPosition = TOP;
            } else if (placement.location == WTStepView.WTLocation.BOTTOM_CENTER) {
                verticalPosition = BOTTOM;
            } else {
                verticalPosition = findVerticalPosition(anchorViewPosition, screenBounds, toolTipBounds.height());
            }
        }
        horizontalPosition = findHorizontalPosition(anchorViewPosition, screenBounds, toolTipBounds.width());
        int toolTipHeight = toolTipBounds.height();
        int toolTipWidth = toolTipBounds.width();
        toolTipPosition = new Rect();
        switch (verticalPosition) {
            case BOTTOM:
                toolTipPosition.top = anchorViewPosition.bottom - ScreenBoundsUtil.getStatusHeight(UserIQSDKInternal.getCurrActivity()) + placement.offset.get(1).intValue();
                toolTipPosition.bottom = anchorViewPosition.bottom + toolTipHeight + placement.offset.get(0).intValue();
                break;
            case TOP:
                toolTipPosition.top = anchorViewPosition.top - toolTipHeight - ScreenBoundsUtil.getStatusHeight(UserIQSDKInternal.getCurrActivity()) + placement.offset.get(1).intValue();
                toolTipPosition.bottom = anchorViewPosition.top + placement.offset.get(0).intValue();
                break;
        }
        switch (horizontalPosition) {
            case LEFT_ALIGNED:
                toolTipPosition.left = screenBounds.left;
                toolTipPosition.right = screenBounds.left + toolTipWidth;
                break;
            case RIGHT_ALIGNED:
                toolTipPosition.right = screenBounds.right;
                toolTipPosition.left = screenBounds.right - toolTipWidth;
                break;
            case CENTRED_WRT_ANCHOR:
                toolTipPosition.left = anchorViewPosition.centerX() - toolTipWidth / 2;
                toolTipPosition.right = anchorViewPosition.centerX() + toolTipWidth / 2;
                break;
        }
        toolTipPosition.left += placement.offset.get(0);
        toolTipPosition.right += placement.offset.get(0);
        return toolTipPosition;
    }

    public
    @VerticalPositionWRTAnchor
    int findVerticalPosition(Rect anchorViewBounds, Rect screenBounds, int tootTipHeight) {
        int availableHeightInTop = anchorViewBounds.top - screenBounds.top;
        if (availableHeightInTop > tootTipHeight) {
            return TOP;
        } else {
            return BOTTOM;
        }
    }

    public
    @HorizontalPositionInScreen
    int findHorizontalPosition(Rect anchoViewBounds, Rect screenBounds, int toolTipWidth) {
        int anchorCentreX = anchoViewBounds.centerX();
        int screenCentreX = screenBounds.centerX();
        if (anchorCentreX > screenCentreX) {
            if ((screenBounds.right - anchorCentreX) >= toolTipWidth / 2) {
                return CENTRED_WRT_ANCHOR;
            } else {
                return RIGHT_ALIGNED;
            }

        } else if (anchorCentreX < screenCentreX) {
            if ((anchorCentreX - screenBounds.left) >= toolTipWidth / 2) {
                return CENTRED_WRT_ANCHOR;
            } else {
                return LEFT_ALIGNED;
            }
        } else {
            return CENTRED_WRT_ANCHOR;
        }
    }

    public boolean isBoundDifferent(Rect anchorViewBounds, Rect screenBounds) {
        return !(this.anchorViewPosition.equals(anchorViewBounds) && this.screenBounds.equals(screenBounds));
    }

//    public void updateBounds(Rect anchorViewBounds, Rect screenBounds) {
//        this.anchorViewPosition = anchorViewBounds;
//        this.screenBounds = screenBounds;
//        updateToolTipPositionInScreenBounds();
//    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TOP, BOTTOM})
    @interface VerticalPositionWRTAnchor {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RIGHT_ALIGNED, LEFT_ALIGNED, CENTRED_WRT_ANCHOR})
    @interface HorizontalPositionInScreen {
    }

    /*
     *    */

  /*  public void decorateContentView() {
        setContentViewPadding();
        measureContentView();
        verticalPosition = findVerticalPosition(anchorViewPosition, screenBounds, toolTipView.getMeasuredHeight());
        horizontalPosition = findHorizontalPosition(anchorViewPosition, screenBounds, toolTipView.getMeasuredWidth());
        toolTipPositionHelper = updateToolTipPositionInScreenBounds(toolTipView.getMeasuredWidth(), toolTipView.getMeasuredHeight());
        ToolTipBgDrawable toolTipBgDrawable = ToolTipBgDrawable.getBgDrawable(borderColor, bgColor, borderWidth, tooltipRadius, verticalPosition,
                toolTipPositionHelper.right - this.anchorViewPosition.centerX() - arrowWidth / 2, arrowHeight, arrowWidth);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            toolTipView.setBackground(toolTipBgDrawable);
        } else {
            toolTipView.setBackgroundDrawable(toolTipBgDrawable);
        }
        decorated = true;
    }*/

}
