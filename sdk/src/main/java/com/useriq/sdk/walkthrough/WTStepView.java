package com.useriq.sdk.walkthrough;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.useriq.Logger;
import com.useriq.sdk.ElementTracker;
import com.useriq.sdk.MatchFinder;
import com.useriq.sdk.UIManager;
import com.useriq.sdk.UIRootView;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.ViewNode;
import com.useriq.sdk.capture.ViewRoot;
import com.useriq.sdk.models.Element;
import com.useriq.sdk.models.Screen;
import com.useriq.sdk.models.WTStep;
import com.useriq.sdk.models.WTTheme;

import java.lang.annotation.Retention;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * @author sudhakar
 * @created 11-Nov-2018
 */
public class WTStepView {
    private static final Logger logger = Logger.init(WTStepView.class.getSimpleName());

    private final String wtId;
    final int stepNum;
    private final Callback stepCb;
    private final ToolTipHandler tooltipHandler = new ToolTipHandler();
    private Drawable overlayDrawable = null;

    public final WTStep step;
    private boolean hasPrev;
    private boolean hasNext;

    private WTStepView prev;
    private WTStepView next;

    WeakReference<View> viewRef = new WeakReference<>(null);
    private boolean isRendered = false;
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private Runnable waitRunnable;

    WTStepView(String wtId, WTStep step, int stepNum, Callback stepCb) {
        this.wtId = wtId;
        this.step = step;
        this.stepNum = stepNum;
        this.stepCb = stepCb;
    }

    void setNext(WTStepView next) {
        this.next = next;
    }

    void setPrev(WTStepView prev) {
        this.prev = prev;
    }

    void setView(View target) {
        this.viewRef = new WeakReference<>(target);
    }

    boolean render(ViewGroup rootView) {
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        if (!step.isValidOnScreen(currScreen)) return false;

        View target = updateTarget();
        if (target == null) return false;

        setView(target);

        hasPrev = false;
        hasNext = false;

        if (step.theme.type == WTTheme.Type.tooltip) {
            if (prev != null) {
                View prevTarget = prev.updateTarget();
                hasPrev = prevTarget != null && !prev.step.finishOn.onNextAvailable;
            }

            if (next != null) {
                View nextTarget = next.updateTarget();
                hasNext = nextTarget != null;
                if (hasNext && next.step.finishOn.onNextAvailable) {
                    stepCb.onNext();
                    return true;
                }
            }
        }

        ElementTracker.getInstance().track(step.element, target, elTrackerCb);

        render(rootView, step.theme);

        if (step.finishOn.onWait >= 0 && waitRunnable == null) {
            waitRunnable = new Runnable() {
                public void run() {
                    stepCb.onNext();
                }
            };
            uiThreadHandler.postDelayed(waitRunnable, step.finishOn.onWait * 1000);
        }

        UserIQSDKInternal.getAnalyticsManager().onWTStep(wtId, step.id, stepNum);
        return true;
    }

    private void render(ViewGroup rootView, WTTheme theme) {
        View target = viewRef.get();

        if (target == null) {
            logger.i("render(): target is null wtId=" + wtId + ", idx=" + (stepNum - 1));
            return;
        }

        int[] loc = new int[2];
        UIRootView uiRootView = UIManager.getInstance().getUiRootView();
        uiRootView.getLocationInWindow(loc);
        Rect uiRootViewRect = new Rect(loc[0], loc[1], loc[0] + uiRootView.getWidth(), loc[1] + uiRootView.getHeight());

        switch (theme.type) {
            case tooltip:
                renderTooltip(rootView, target, theme, uiRootViewRect);
                break;
            case number:
                renderNumberDot(rootView, target, theme, uiRootViewRect);
                break;
            case ripple:
                renderBeacon(rootView, target, theme, uiRootViewRect);
                break;
        }
        isRendered(true);
    }

    void clear() {
        View target = viewRef.get();
        if (target != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (overlayDrawable != null) {
                    target.getOverlay().remove(overlayDrawable);
                }
            }
            target.invalidate();
        }
        tooltipHandler.clear();
        isRendered(false);
        uiThreadHandler.removeCallbacksAndMessages(null);
        ElementTracker.getInstance().unTrack(step.element, elTrackerCb);
    }

    public WTStepView getPrev() {
        return prev;
    }

    public WTStepView getNext() {
        return next;
    }

    private void renderTooltip(ViewGroup rootView, View target, WTTheme theme, Rect uiRootViewRect) {
        View.OnClickListener prevListener = hasPrev ? new View.OnClickListener() {
            public void onClick(View v) {
                stepCb.onPrev();
            }
        } : null;

        View.OnClickListener nxtListener = hasNext ? new View.OnClickListener() {
            public void onClick(View v) {
                stepCb.onNext();
            }
        } : null;

        View.OnClickListener closeListener = new View.OnClickListener() {
            public void onClick(View v) {
                stepCb.onClose();
            }
        };

        rootView.removeAllViews();
        tooltipHandler.addToolTip(
                step.title,
                step.desc,
                nxtListener,
                prevListener,
                closeListener,
                theme,
                target,
                rootView,
                uiRootViewRect);
    }

    private void renderNumberDot(ViewGroup rootView, View target, WTTheme theme, Rect uiRootViewRect) {
        rootView.removeAllViews();
        ImageView view = new ImageView(rootView.getContext());
        NumberDot numberView = new NumberDot(
                target,
                theme.bgColor,
                stepNum,
                theme.color,
                theme.borderColor,
                theme.borderRadius,
                theme.placement,
                false,
                uiRootViewRect);
        view.setImageDrawable(numberView);

        rootView.addView(view, rootView.getLayoutParams());
    }

    private void renderBeacon(ViewGroup rootView, View target, WTTheme theme, Rect uiRootViewRect) {
        int[] targetRect = new int[2];
        target.getLocationInWindow(targetRect);

        rootView.removeAllViews();

        if (UIManager.getInstance().getUiRootView().getRootType() != ViewRoot.ACTIVITY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final RippleDrawable mOverlayDrawable = new RippleDrawable(
                        target,
                        theme.color,
                        theme.borderRadius);
                target.getOverlay().add(mOverlayDrawable);
                target.invalidate();
                mOverlayDrawable.start();
                this.overlayDrawable = mOverlayDrawable;
            } else {
                logger.d("sdk version not supported for blinking dot");
            }
            return;
        }

        targetRect[0] -= uiRootViewRect.left;
        targetRect[1] -= uiRootViewRect.top;

        ShapeRipple ripple = new ShapeRipple(target.getContext(), theme, targetRect, target);
        ripple.setRippleMaximumRadius(40);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        ripple.setLayoutParams(params);
        rootView.addView(ripple, rootView.getLayoutParams());
    }

    void isRendered(boolean rendered) {
        this.isRendered = rendered;
    }

    boolean isRendered() {
        return isRendered;
    }

    private ElementTracker.Callback elTrackerCb = new ElementTracker.Callback() {
        @Override
        public void onViewDetached(View view, Element element) {
            stepCb.onNext();
            ElementTracker.getInstance().unTrack(element, this);
        }

        @Override
        public void onViewClick(View view, Element element) {
            if (step.finishOn.onElClick) {
                stepCb.onNext();
                ElementTracker.getInstance().unTrack(element, this);
            }
        }

        @Override
        public void onViewLongClick(View view, Element element) {
            if (step.finishOn.onElLongClick) {
                stepCb.onNext();
                ElementTracker.getInstance().unTrack(element, this);
            }
        }
    };

    private View updateTarget() {
        Activity currActivity = UserIQSDKInternal.getCurrActivity();

        if (step.validScreens != null && step.validScreens.size() > 0) {
            Screen screen = UserIQSDKInternal.getCurrScreen();

            if (!step.isValidOnScreen(screen)) return null;
        }

        View view = viewRef.get();
        if (view == null) {
            view = MatchFinder.findView(currActivity, step.element);
//            view = MatchFinder.findView(currActivity, step.element, 0);
        }

        boolean found = step.element.matches(0, new ViewNode(view));
        if (!found) return null;

        return view;
    }

    private WTTheme getDefaultNumberTheme() {
        Map<String, Object> attrs = new HashMap<>();
        List<Long> color = new ArrayList<>();
        color.add(230L);
        color.add(64L);
        color.add(40L);
        color.add(79L);
        attrs.put("bgColor", color);
        attrs.put("borderColor", color);
        attrs.put("borderRadius", 14);

        Map<String, Object> map = new HashMap<>();
        map.put("type", "number");
        map.put("attrs", attrs);
        return new WTTheme(map);
    }

    interface Callback {
        void onPrev();

        void onNext();

        void onClose();
    }

    @Retention(SOURCE)
    @IntDef({WTLocation.TOP_LEFT, WTLocation.TOP_CENTER, WTLocation.TOP_RIGHT, WTLocation.CENTER_LEFT, WTLocation.CENTER_CENTER,
            WTLocation.CENTER_RIGHT, WTLocation.BOTTOM_LEFT, WTLocation.BOTTOM_CENTER, WTLocation.BOTTOM_RIGHT})
    public @interface WTLocation {
        int TOP_LEFT = 1;
        int TOP_CENTER = 2;
        int TOP_RIGHT = 3;
        int CENTER_LEFT = 4;
        int CENTER_CENTER = 5;
        int CENTER_RIGHT = 6;
        int BOTTOM_LEFT = 7;
        int BOTTOM_CENTER = 8;
        int BOTTOM_RIGHT = 9;
    }

    @Retention(SOURCE)
    @IntDef({WTPlacementType.AUTO, WTPlacementType.MANUAL})
    public @interface WTPlacementType {
        int AUTO = 1;
        int MANUAL = 2;
    }
}
