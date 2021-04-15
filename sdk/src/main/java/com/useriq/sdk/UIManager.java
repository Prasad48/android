package com.useriq.sdk;


import android.app.Activity;
import android.view.KeyEvent;

import com.useriq.Logger;
import com.useriq.sdk.capture.ViewRoot;
import com.useriq.sdk.ctxHelp.CtxHelpCtrl;
import com.useriq.sdk.ctxHelp.FABCtrl;
import com.useriq.sdk.helpcenter.AnswerWithToolbarCtrl;
import com.useriq.sdk.helpcenter.HelpCenterCtrl;
import com.useriq.sdk.models.Screen;
import com.useriq.sdk.util.UnitUtil;
import com.useriq.sdk.v1Modal.V1ModalCtrl;
import com.useriq.sdk.walkthrough.WTStepView;
import com.useriq.sdk.walkthrough.WalkthroughCtrl;

import java.util.concurrent.ConcurrentLinkedQueue;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

/**
 * @author sudhakar
 * @created 09-Oct-2018
 */
public class UIManager {
    private static final Logger logger = Logger.init(UIManager.class.getSimpleName());
    private static final int ACTIVATE_DELAY = 3 * 1000;
    private static UIManager instance;

    private final ConcurrentLinkedQueue<Activate> activateQueue = new ConcurrentLinkedQueue<>();
    private final UIRootView uiRootView;
    private boolean isStarted = false;
    private ActivityTracker activityTracker;

    private UIManager() {
        this.uiRootView = new UIRootView(UserIQSDKInternal.getContext());

        UIRouter.getInstance().setRootView(uiRootView);
    }

    public static UIManager getInstance() {
        if (instance != null) return instance;
        synchronized (UIManager.class) {
            instance = new UIManager();
            return instance;
        }
    }

    public UIRootView getUiRootView() {
        return uiRootView;
    }

    void start(ActivityTracker activityTracker) {
        this.activityTracker = activityTracker;
        UIRouter r = UIRouter.getInstance();

        if (!isStarted) {
            this.isStarted = true;
            activityTracker.addCallback(trackerCb);
            trackerCb.onActivityResumed(UserIQSDKInternal.getCurrActivity());
        }
    }

    void stop() {
        isStarted = false;
        activityTracker.removeCallback(trackerCb);
        activityTracker = null;
    }

    private final ActivityTracker.Callback trackerCb = new ActivityTracker.Callback() {
        private boolean consumedBackBtnDown = false;

        public void onActivityResumed(Activity activity) {
            if (activity != null) {
                uiRootView.attach(activity, ViewRoot.NONE);
//                UIRouter.Controller ctrl = UIRouter.getInstance().getCurrent();
//                if (ctrl instanceof WalkthroughCtrl) {
//                    if (((WalkthroughCtrl) ctrl).getCurrStepView().getNext() != null) {
//                        ((WalkthroughCtrl) ctrl).showNextStep(500);
//                    }
//                }
            }
        }

        void onWindowFocused(boolean hasFocus) {
            Activity activity = UserIQSDKInternal.getCurrActivity();
            if (activity != null) {
                int rootType = hasFocus ? ViewRoot.ACTIVITY : ViewRoot.NONE;
                if (hasFocus && uiRootView.getRootType() == ViewRoot.POPUP) {
                    // delay attaching root view when transitioning back from POPUP
                    // FIXME: Find a way to remove delay
                    uiRootView.attachDelayed(activity, rootType);
                } else {
                    uiRootView.attach(activity, rootType);
                }
            }
        }

        boolean onKeyEvent(KeyEvent event) {
            boolean isBack = event.getKeyCode() == KeyEvent.KEYCODE_BACK;
            boolean isDown = event.getAction() == KeyEvent.ACTION_DOWN;
            boolean isRepeating = event.getRepeatCount() != 0;

            if (isBack) {
                if (isDown && !isRepeating && !consumedBackBtnDown) {
                    UIRouter r = UIRouter.getInstance();
                    consumedBackBtnDown = r.handleBackPressed();

                    return consumedBackBtnDown;
                } else if (consumedBackBtnDown) {
                    consumedBackBtnDown = false;
                    return true;
                }
            }

            return false;
        }

        void onScrollStart() {
            UIRouter.Controller ctrl = UIRouter.getInstance().getCurrent();
            if (ctrl instanceof WalkthroughCtrl) {
                ((WalkthroughCtrl) ctrl).pause();
            }
        }

        void onScrollStop() {
            UIRouter.Controller ctrl = UIRouter.getInstance().getCurrent();
            if (ctrl instanceof WalkthroughCtrl) {
                ((WalkthroughCtrl) ctrl).resume();
            }
        }
    };

    public void applyNext() {
        try {
            for (Activate activate : activateQueue) {
                boolean isApplied = apply(activate);
                if (isApplied) {
                    activateQueue.remove(activate);
                    break;
                }
            }
        } catch (Exception e) {
            logger.e("UIManager.applyNext()", e);
        }
    }

    private boolean apply(Activate activate) {
        if (activate == null) {
            // then queue is null & we dont have anything to process
            return false;
        }

        UIRouter r = UIRouter.getInstance();
        UIRouter.Controller ctrl;

        switch (activate.type) {
            case "ctxHelp":
                ctrl = new CtxHelpCtrl(activate.value);
                r.push(ctrl);
                return true;
            case "v1Modal":
                ctrl = new V1ModalCtrl(activate.value);
                if (canShowModals(activate.value)) {
                    r.push(ctrl);
                    return true;
                }
                return false;
            case "walkthrough":
                ctrl = new WalkthroughCtrl(activate.value);
                if (SDK_INT < JELLY_BEAN_MR2) {
                    logger.w("Walkthrough is not supported in API_LEVEL=" + SDK_INT);
                    activateQueue.remove(activate);
                    return false;
                }
                if (canShowWalkthrough((WalkthroughCtrl) ctrl)) {
                    r.push(ctrl);
                    return true;
                }
                return false;
            case "helpCenter":
                ctrl = new HelpCenterCtrl();
                r.push(ctrl);
                return true;
            case "question":
                ctrl = new AnswerWithToolbarCtrl(activate.value);
                r.push(ctrl);
                return true;
            default:
                logger.i("schedule(): Unknown activation - type: " + activate.type + ", value: " + activate.value);
                // unknown activation.type, ignore & applyNext
                return false;
        }
    }

    private boolean canShowWalkthrough(WalkthroughCtrl ctrl) {
        Screen screen = UserIQSDKInternal.getCurrScreen();
        WTStepView currStepView = ctrl.getCurrStepView();

        while (currStepView != null) {
            if (currStepView.step.isValidOnScreen(screen)) return true;
            currStepView = ctrl.getCurrStepView().getNext();
        }

        return false;
    }

    private boolean canShowModals(String value) {
        int width = ViewUtils.getScreenWidth();
        int height = ViewUtils.getScreenHeight();
        if (UnitUtil.pxToDp(height) >= 480 && UnitUtil.pxToDp(width) > 320) {
            return true;
        } else {
            logger.w("Device too small. Not launching campaign: " + value + ", WxH: " + width + "x" + height);
            return false;
        }
    }

    void resetActivateQueue() {
        activateQueue.clear();
    }

    public void schedule(String type, String value) {
        try {
            if (!isStarted) {
                logger.w("schedule(): Ignoring activate [" + type + ", " + value + "]");
                return;
            }

            activateQueue.add(new Activate(type, value));

            UIRouter.Controller currCtrl = UIRouter.getInstance().getCurrent();
            if (currCtrl instanceof FABCtrl) {
                ((FABCtrl) currCtrl).applyNextDelayed();
            }
        } catch (Exception e) {
            logger.e("UIManager.schedule()", e);
        }
    }

    private static class Activate {
        private final String type;
        private final String value;

        private Activate(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }
}
