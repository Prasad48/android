package com.useriq.sdk;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.useriq.Logger;
import com.useriq.sdk.ctxHelp.UserIQMorphTransition;

import java.util.ArrayList;
import java.util.Stack;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * @author sudhakar
 * @created 27-Oct-2018
 */
public class UIRouter {
    private static final Logger logger = Logger.init(UIRouter.class.getSimpleName());

    private static UIRouter instance;
    private Stack<Controller> backStack = new Stack<>();
    private UIRootView rootView = null;

    public static UIRouter getInstance() {
        if(instance == null) instance = new UIRouter();
        return instance;
    }

    private UIRouter() {}

    public boolean pop() {
        // last item in the stack is always assumed to be FabCtrl
        // so pop until last but leave FabCtrl
        if(backStack.size() <= 1) return false;

        if (rootView == null) {
            logger.d("pop(): rootView not set. Cant transition.");
            return false;
        }

        Controller currCtrl = backStack.pop();
        Controller prevCtrl = backStack.peek();

        doTransition(currCtrl, prevCtrl, true);

        return true;
    }

    public void push(Controller nextCtrl) {
        Class<?> klass = nextCtrl.getClass();

        Controller currCtrl = null;
        if(!backStack.empty()) {
            currCtrl = backStack.peek();
        }

        int idx = -1;
        for(int i = 0; i < backStack.size(); i++) {
            Controller controller = backStack.get(i);

            if(klass.equals(controller.getClass())) {
                idx = i;
                break;
            }
        }

        // Trim stack when uri is already present in the stack
        // We trim upto its current idx
        if (idx > -1) {
            backStack.setSize(idx);
        }

        backStack.push(nextCtrl);

        doTransition(currCtrl, nextCtrl, false);
    }

    public Controller getCurrent() {
        if(backStack.isEmpty()) return null;
        return backStack.peek();
    }

    void setRootView(UIRootView rootView) {
        this.rootView = rootView;
    }

    private void doTransition(Controller currCtrl, Controller nextCtrl, boolean isReversed) {

        if(currCtrl != null) {
            currCtrl.onExit();
        }

        View endView = nextCtrl.onEnter();

        if (rootView.getChildCount() == 0 && SDK_INT >= LOLLIPOP) {
            // then we dont have any start views to animate

            Context ctx = rootView.getContext();
            FrameLayout frame = new FrameLayout(ctx);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            lp.gravity = Gravity.BOTTOM;
            frame.setLayoutParams(lp);
            frame.setTransitionName(ctx.getResources().getString(R.string.transition_fade_view));

            FrameLayout pixel = new FrameLayout(ctx);
            lp = new FrameLayout.LayoutParams(1, 1);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            pixel.setLayoutParams(lp);
            pixel.setTransitionName(ctx.getResources().getString(R.string.transition_morph_view));
            pixel.setTag(R.id.viewBgColor, Color.BLACK);
            pixel.setTag(R.id.viewRadius, 0);

            frame.addView(pixel);
            rootView.addView(frame);
        }

        // TODO: handle same controller update hint: onPreDraw remove oldView to minimize flicker

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if(endView != null) {
                String transition_morph_view = endView.getResources().getString(R.string.transition_morph_view);
                String transition_fade_view = endView.getResources().getString(R.string.transition_fade_view);

                UserIQMorphTransition uiqTransition = new UserIQMorphTransition(isReversed);
                uiqTransition.addTarget(transition_morph_view);

                Fade fadeTransition = new Fade();
                fadeTransition
                        .addTarget(transition_fade_view)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator());

                TransitionSet transitionSet = new TransitionSet();
                transitionSet
                        .addTransition(uiqTransition)
                        .addTransition(fadeTransition);

                // record hierarchy changes & play them
                TransitionManager.beginDelayedTransition(rootView, transitionSet);

            }
        }

        rootView.removeAllViews();
        if(endView != null) rootView.addView(endView);
    }

    private ArrayList<View> getAllChildren(View v) {
        ArrayList<View> visited = new ArrayList<>();
        ArrayList<View> unvisited = new ArrayList<>();
        unvisited.add(v);

        while (!unvisited.isEmpty()) {
            View view = unvisited.remove(0);
            visited.add(view);
            if (view instanceof ViewGroup) {
                int childCount = ((ViewGroup) view).getChildCount();
                for (int i = 0; i < childCount; i++) {
                    unvisited.add(((ViewGroup) view).getChildAt(i));
                }
            }
        }
        return visited;
    }

    void clear() {
        backStack.clear();
    }

    boolean handleBackPressed() {
        Controller ctrl = getCurrent();
        return ctrl != null && ctrl.onBackPressed();
    }

    /**
     * Controller is responsible to attaching & detaching view hierarchies
     */
    public interface Controller {
        View onEnter();
        void onExit();
        boolean onBackPressed();
    }

    public interface Callback {
        void onRouteChange(Controller next);
    }
}
