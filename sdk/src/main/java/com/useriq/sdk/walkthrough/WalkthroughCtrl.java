package com.useriq.sdk.walkthrough;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.useriq.Logger;
import com.useriq.sdk.R;
import com.useriq.sdk.ScreenTracker;
import com.useriq.sdk.UIManager;
import com.useriq.sdk.UIRootView;
import com.useriq.sdk.UIRouter;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.models.Screen;
import com.useriq.sdk.models.SyncData;
import com.useriq.sdk.models.WTStep;
import com.useriq.sdk.models.WTTheme;
import com.useriq.sdk.models.Walkthrough;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * @author sudhakar
 * @created 29-Oct-2018
 */

public class WalkthroughCtrl implements UIRouter.Controller {
    private static final Logger logger = Logger.init(WalkthroughCtrl.class.getSimpleName());
    private final FrameLayout backdrop;
    private final String wtId;
    private final Walkthrough wt;

    private WTTheme defaultNumberTheme;
    private WTStepView currStepView;
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private boolean isRendering;

    private ScreenTracker.Callback screenTracker = new ScreenTracker.Callback() {
        @Override
        public void onScreenChange(Screen newScreen, Screen oldScreen) {
            if (currStepView == null) {
                onBackPressed();
                return;
            }
            if (currStepView.step.finishOn.onNextAvailable) {
                WTStepView view = currStepView;
                currStepView = currStepView.getNext();
                boolean isRendered = renderStepNow();
                if (!isRendered) {
                    currStepView = view;
                } else {
                    view.clear();
                }
            }
        }
    };

    public WalkthroughCtrl(String wtId) {
        this.wtId = wtId;
        Context ctx = UserIQSDKInternal.getContext();

        this.backdrop = new FrameLayout(ctx);
//        backdrop.setBackgroundColor(ctx.getResources().getColor(R.color.containerBackdrop));
        backdrop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (SDK_INT >= LOLLIPOP) {
            backdrop.setTransitionName(ctx.getResources().getString(R.string.transition_fade_view));
        }

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("color", Arrays.asList(255L, 100L, 100L, 100L));
        attrs.put("bgColor", Arrays.asList(200L, 200L, 200L, 100L));
        attrs.put("borderColor", Arrays.asList(150L, 50L, 20L, 30L));
        attrs.put("borderRadius", 14);

        Map<String, Object> theme = new HashMap<>();
        theme.put("type", "number");
        theme.put("attrs", attrs);

        this.defaultNumberTheme = new WTTheme(theme);

        this.wt = UserIQSDKInternal.getSyncData().getWalkthroughById(wtId);

        if (this.wt == null || this.wt.steps.size() == 0) {
            return;
        }

        int size = wt.steps.size();
        List<WTStepView> stepList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            WTStep step = wt.steps.get(i);
            WTStepView currStep = new WTStepView(wtId, step, i + 1, stepCb);
            stepList.add(currStep);
            if (i > 0) {
                WTStepView lastStep = stepList.get(i - 1);
                lastStep.setNext(currStep);
                currStep.setPrev(lastStep);
            }
        }

        currStepView = stepList.get(0);
    }

    @Override
    public View onEnter() {

        if (this.wt == null || this.wt.steps.size() == 0) {
            logger.w("Exiting WT. Walkthrough is null or has no steps");
            Toast.makeText(UserIQSDKInternal.getContext(), "Invalid Walkthrough", Toast.LENGTH_SHORT).show();
            this.onBackPressed();
            return backdrop;
        }

        UserIQSDKInternal.getScreenTracker().addCallback(screenTracker);

        UIRootView uiRootView = UIManager.getInstance().getUiRootView();

        uiRootView.setCallback(rootViewCb);

        if (currStepView != null) {
            boolean isFinished = currStepView.stepNum == wt.steps.size()-1;
            UserIQSDKInternal.getAnalyticsManager().onWT(wtId, currStepView.step.id, true, isFinished, currStepView.stepNum);
        }

        renderFirstStep();

        return backdrop;
    }

    @Override
    public void onExit() {
        // needed if the view open the help centre, it ends up in clearing the element
        // tracking callback hence the walkthrough doesn't end.
        if (currStepView != null) {
            showNextStep(0);
        }

        clearStep(currStepView);
        UserIQSDKInternal.getScreenTracker().removeCallBack(screenTracker);
        UIManager.getInstance().getUiRootView().setCallback(null);
        uiThreadHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onBackPressed() {
        UIRouter.getInstance().pop();
        if (!SyncData.getInstance().getIsSoftActivate()) {
            Toast.makeText(UserIQSDKInternal.getContext(), "Walkthrough Ended", Toast.LENGTH_SHORT).show();
        }
        if (currStepView != null) {
            boolean isFinished = currStepView.stepNum == wt.steps.size();
            UserIQSDKInternal.getAnalyticsManager().onWT(wtId, currStepView.step.id, false, isFinished, currStepView.stepNum);
        }
        return true;
    }

    public void pause() {
//        if (currStepView.step.theme.type == WTTheme.Type.tooltip) {
            currStepView.clear();
            backdrop.removeAllViews();
//            currStepView.render(backdrop, defaultNumberTheme);
//        }
    }

    public void resume() {
        if (currStepView == null) stepCb.onNext();
        else {
            boolean isRendered = renderStepNow();
            if (!isRendered) stepCb.onNext();
        }
    }

    private void renderFirstStep() {
        boolean renderedFirstStep = false;

        while (currStepView != null) {
            renderedFirstStep = renderStepNow();
            if (renderedFirstStep) break;
            currStepView = currStepView.getNext();
        }

        if (!renderedFirstStep) {
            logger.e("renderStep(): No valid elements", null);
            String text = "No valid elements";
            Toast.makeText(UserIQSDKInternal.getContext(), text, Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    private boolean renderStepNow() {
        try {
            clearStep(currStepView);

            boolean isRendered = currStepView.render(backdrop);
            if (!isRendered) return false;

            if (currStepView.step.finishOn.onOutsideClick) {
                backdrop.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        clearStep(currStepView);
                        stepCb.onNext();
                    }
                });
            }

            return true;
        } catch (Exception e) {
            logger.e("WalkthroughCtrl.renderStepNow()", e);
        }
        return false;
    }

    private void renderDelayed(int ms, final boolean lastAttempt) {
        isRendering = true;
        uiThreadHandler.postDelayed(new Runnable() {
            public void run() {
                if (!currStepView.isRendered()) {
                    boolean isRendered = renderStepNow();
                    if (!isRendered && !lastAttempt) renderDelayed(currStepView.step.nextStepWait, true);
                    if (!isRendered && lastAttempt) stepCb.onNext();
                }
                isRendering = false;
            }
        }, ms);
    }

    private void clearStep(WTStepView stepView) {
        backdrop.setOnClickListener(null);
        backdrop.setClickable(false);
        if (stepView != null) {
            stepView.clear();
        }
    }

    private WTStepView.Callback stepCb = new WTStepView.Callback() {
        @Override
        public void onPrev() {
            if (UserIQSDKInternal.getSDKConfig().isEmu()) {
                showPrevStep(2000);
            } else {
                showPrevStep(1000);
            }
        }

        @Override
        public void onNext() {
            if (UserIQSDKInternal.getSDKConfig().isEmu()) {
                showNextStep(2000);
            } else {
                showNextStep(1000);
            }
        }

        @Override
        public void onClose() {
            clearStep(currStepView);
            onBackPressed();
        }
    };

    private UIRootView.Callback rootViewCb = new UIRootView.Callback() {
        public void onAttached() {
            logger.d("onAttached(): rootViewCb");

            if (isRendering) {
                return;
            }
            if (currStepView == null) {
                onBackPressed();
                return;
            }

            if (!currStepView.isRendered()) {
                boolean isRendered = renderStepNow();
                if (!isRendered) renderDelayed(1000, false);
            }

            View target = currStepView.viewRef.get();

            if (target != null && target.getRootView() != backdrop.getRootView()) {
                stepCb.onNext();
            }
        }

        public void onDetached() {
//            isRootAttached = false;
//            if (currStepView == null) {
//                return;
//            }
//
//            View target = currStepView.viewRef.get();
//            if (target == null) {
//                stepCb.onNext();
//            }
        }
    };

    public WTStepView getCurrStepView() {
        return currStepView;
    }

    private void showPrevStep(int ms) {
        clearStep(currStepView);
        WTStepView prevStep = currStepView.getPrev();

        if (prevStep != null) {
            prevStep.isRendered(false);
            currStepView = prevStep;
            boolean isRendered = renderStepNow();
            if (!isRendered) renderDelayed(ms, false);
        } else {
            stepCb.onClose();
        }
    }

    public void showNextStep(int ms) {
        clearStep(currStepView);
        WTStepView nextStep = currStepView.getNext();
        if (nextStep != null) {
            nextStep.isRendered(false);
            currStepView = nextStep;
            boolean isRendered = renderStepNow();
            if (!isRendered) renderDelayed(ms, false);
        } else {
            stepCb.onClose();
        }
    }
}
