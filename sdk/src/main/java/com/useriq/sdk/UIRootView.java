package com.useriq.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.useriq.Logger;
import com.useriq.sdk.capture.ViewRoot;

import java.util.List;

import static com.useriq.sdk.capture.ViewRoot.NONE;
import static com.useriq.sdk.util.ScreenBoundsUtil.getStatusHeight;

/**
 * @author sudhakar
 * @created 15-Oct-2018
 */
public class UIRootView extends FrameLayout {
    private static final Logger logger = Logger.init(UIRootView.class.getSimpleName());
    private Callback cb = null;
    private Runnable attachRunner = null;
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private int statusHeight;
    private int navHeight = 0;
    private int paddingBottom;
    private int paddingTop;

    @ViewRoot.ROOT_TYPE
    private int rootType = NONE;

    public UIRootView(@NonNull Context ctx) {
        super(ctx);

        statusHeight = getStatusHeight(ctx);
    }

    /**
     * @param view View to add
     * @param locX absolute position on screen
     * @param locY absolute position on screen
     */
    public void addView(View view, int locX, int locY, int height, int width) {
        FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(width, height);
        view.setLayoutParams(layoutParam);
        addView(view, locX, locY, false);
    }

    private void addView(View view, int locX, int locY, boolean invalidateRootView) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = locX;
        layoutParams.topMargin = locY;
        view.setLayoutParams(layoutParams);
        addView(view);

        if (invalidateRootView) invalidate();
    }

    public void attach(Activity activity, @ViewRoot.ROOT_TYPE int rootType) {
        try {
            ViewParent parent = getParent();
            ViewRoot topViewRoot = getTopViewRoot(activity, rootType);
            FrameLayout parentFrame;

            if (topViewRoot == null) {
                logger.i("attach(): topViewRoot is null. Using activity Root");
                View view = activity.getWindow().getDecorView().getRootView();

                if (view instanceof FrameLayout) {
                    parentFrame = (FrameLayout) view;
                    this.rootType = ViewRoot.ACTIVITY;
                } else {
                    logger.w("attach(): failed. activity root is also null");
                    this.rootType = ViewRoot.NONE;
                    return;
                }
            } else {
                parentFrame = (FrameLayout) topViewRoot.getView();
                this.rootType = topViewRoot.getType();
            }

            int uiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            boolean isImmersiveModeEnabled = false;
            boolean isNavHidden = ((uiVisibility | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == uiVisibility);
            boolean isStatusBarHidden = ((uiVisibility | View.SYSTEM_UI_FLAG_FULLSCREEN) == uiVisibility) || ((uiVisibility | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) == uiVisibility);
            int orientation = getResources().getConfiguration().orientation;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                isImmersiveModeEnabled = ((uiVisibility | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiVisibility);
            }

            int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navHeight = getResources().getDimensionPixelSize(resourceId);
            }

            if (orientation == Configuration.ORIENTATION_LANDSCAPE || isNavHidden || !isNavigationBarAvailable()) {
                paddingBottom = 0;
            } else {
                paddingBottom = navHeight;
            }

            if (isStatusBarHidden) {
                paddingTop = 0;
            } else {
                paddingTop = statusHeight;
            }

            setPadding(0, paddingTop, 0, paddingBottom);

            setVisibility(VISIBLE);

            // if parent is same as parentFrame then we are already having
            // the right parent. So do nothing!
            if (parent == parentFrame) return;
            else if (parent != null) detach();

            if (parentFrame != null) {
                parentFrame.addView(this);
            }
        } catch (Exception e) {
            logger.e("UIRootView.attach()", e);
        }
    }

    public boolean isNavigationBarAvailable() {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

        return (!(hasBackKey && hasHomeKey));
    }

    public void attachDelayed(final Activity activity, @ViewRoot.ROOT_TYPE final int rootType) {
        this.attachRunner = new Runnable() {
            public void run() {
                attach(activity, rootType);
            }
        };

        uiThreadHandler.postDelayed(attachRunner, 400);
    }

    public void detach() {
        if (attachRunner != null) uiThreadHandler.removeCallbacksAndMessages(attachRunner);
        ViewParent parent = getParent();
        if (parent instanceof FrameLayout) {
            ((FrameLayout) parent).removeView(this);
        }
    }

    @ViewRoot.ROOT_TYPE
    public int getRootType() {
        return rootType;
    }

    public void setCallback(Callback cb) {
        this.cb = cb;
    }

    private ViewRoot getTopViewRoot(Activity activity, @ViewRoot.ROOT_TYPE int rootType) {
        try {
            List<ViewRoot> viewRoots = ViewRoot.from(activity);
            if (viewRoots.size() > 0) {
                ViewRoot topViewRoot = null;
                if (rootType == NONE) {
                    // we are only interested on the topmost view
                    topViewRoot = viewRoots.get(viewRoots.size() - 1);
                } else {
                    for (int i = viewRoots.size() - 1; i >= 0; i--) {
                        ViewRoot viewRoot = viewRoots.get(i);
                        if (viewRoot.getType() == rootType) {
                            topViewRoot = viewRoot;
                            break;
                        }
                    }
                }

                if (topViewRoot == null || topViewRoot.getView() == null) {
                    logger.e("Root view is null", null);
                    return null;
                } else if (!(topViewRoot.getView() instanceof FrameLayout)) {
                    logger.e("Root view is not a frame layout", null);
                    return null;
                }

                return topViewRoot;
            }
        } catch (Exception e) {
            logger.e("getTopViewRoot(): failed", e);
        }

        return null;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (cb != null) cb.onAttached();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (cb != null) cb.onDetached();
    }

    public abstract static class Callback {
        public void onAttached() {
        }

        public void onDetached() {
        }
    }
}
