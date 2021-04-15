package com.useriq.sdk.capture;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.useriq.sdk.capture.Reflect.getFieldValue;
import static java.lang.annotation.RetentionPolicy.SOURCE;

public final class ViewRoot {
    private static final String TAG = ViewRoot.class.getSimpleName();

    public final View view;
    final Rect winFrame;
    final WindowManager.LayoutParams lParams;

    private ViewRoot(@NonNull View view, Rect winFrame, WindowManager.LayoutParams lParams) {
        this.view = view;
        this.winFrame = winFrame;
        this.lParams = lParams;
    }

    public static List<ViewRoot> from(Activity activity) throws Exception {
        List<ViewRoot> viewRoots = new ArrayList<>();
        Object globalWindowManager;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            globalWindowManager = getFieldValue("mWindowManager", activity.getWindowManager());
        } else {
            globalWindowManager = getFieldValue("mGlobal", activity.getWindowManager());
        }
        Object rootObjects = getFieldValue("mRoots", globalWindowManager);
        Object paramsObject = getFieldValue("mParams", globalWindowManager);

        Object[] roots;
        WindowManager.LayoutParams[] params;

        //  There was a change to ArrayList implementation in 4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            roots = ((List) rootObjects).toArray();
            List<WindowManager.LayoutParams> paramsList = (List<WindowManager.LayoutParams>) paramsObject;
            params = paramsList.toArray(new WindowManager.LayoutParams[paramsList.size()]);
        } else {
            roots = (Object[]) rootObjects;
            params = (WindowManager.LayoutParams[]) paramsObject;
        }

        for (int i = 0; i < roots.length; i++) {
            Object root = roots[i];
            View view = (View) getFieldValue("mView", root);
            // fixes https://github.com/jraska/Falcon/issues/10
            if (view == null) {
                Log.e(TAG, "null View stored as root in Global window manager, skipping");
                continue;
            }
            //root is invisible so need not to consider
            if (view.getWindowVisibility() != View.VISIBLE) {
                continue;
            }
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            Rect area = new Rect(left, top, left + view.getWidth(), top + view.getHeight());
            ViewRoot viewRoot = new ViewRoot(view, area, params[i]);
            viewRoots.add(viewRoot);
        }

        if (viewRoots.size() != 0) {
            // Ignore transient activity window from top
            if (viewRoots.get(viewRoots.size() - 1).isActivityType()) {
                if (activity.getWindow().getDecorView() != viewRoots.get(viewRoots.size() - 1).view) {
                    viewRoots.remove(viewRoots.size() - 1);
                }
            }
        }

        // Remove toast & progress dialogs
        for (Iterator<ViewRoot> iterator = viewRoots.iterator(); iterator.hasNext(); ) {
            ViewRoot viewRoot = iterator.next();
            if (viewRoot.isToast()) iterator.remove();
        }

        if (viewRoots.isEmpty()) {
            return Collections.emptyList();
        }

        offsetRootsTopLeft(viewRoots);
        ensureDialogsAreAboveItsParentActivities(viewRoots);

        return viewRoots;
    }

    boolean isActivityType() {
        return lParams.type == WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
    }

    private static void offsetRootsTopLeft(List<ViewRoot> viewRoots) {
        int minTop = Integer.MAX_VALUE;
        int minLeft = Integer.MAX_VALUE;
        for (ViewRoot viewRoot : viewRoots) {
            if (viewRoot.winFrame.top < minTop) {
                minTop = viewRoot.winFrame.top;
            }

            if (viewRoot.winFrame.left < minLeft) {
                minLeft = viewRoot.winFrame.left;
            }
        }

        for (ViewRoot viewRoot : viewRoots) {
            viewRoot.winFrame.offset(-minLeft, -minTop);
        }
    }

    // This fixes issue #11. It is not perfect solution and maybe there is another case
    // of different type of view, but it works for most common case of dialogs.
    private static void ensureDialogsAreAboveItsParentActivities(List<ViewRoot> viewRoots) {
        if (viewRoots.size() <= 1) {
            return;
        }

        for (int dialogIndex = 0; dialogIndex < viewRoots.size() - 1; dialogIndex++) {
            ViewRoot viewRoot = viewRoots.get(dialogIndex);
            if (!viewRoot.isDialogType()) {
                continue;
            }

            Activity dialogOwnerActivity = ownerActivity(viewRoot.context());
            if (dialogOwnerActivity == null) {
                // make sure we will never compare null == null
                return;
            }

            for (int parentIndex = dialogIndex + 1; parentIndex < viewRoots.size(); parentIndex++) {
                ViewRoot possibleParent = viewRoots.get(parentIndex);
                if (possibleParent.isActivityType()
                        && ownerActivity(possibleParent.context()) == dialogOwnerActivity) {
                    viewRoots.remove(possibleParent);
                    viewRoots.add(dialogIndex, possibleParent);

                    break;
                }
            }
        }
    }

    boolean isDialogType() {
        return lParams.type == WindowManager.LayoutParams.TYPE_APPLICATION;
    }

    private static Activity ownerActivity(Context context) {
        Context currentContext = context;

        while (currentContext != null) {
            if (currentContext instanceof Activity) {
                return (Activity) currentContext;
            }

            if (currentContext instanceof ContextWrapper && !(currentContext instanceof Application)) {
                currentContext = ((ContextWrapper) currentContext).getBaseContext();
            } else {
                break;
            }
        }

        return null;
    }

    Context context() {
        return view.getContext();
    }

    public static ViewRoot getPrimary(List<ViewRoot> viewRoots) throws Exception {
        int rootPosition = viewRoots.size() - 1;
        for (; rootPosition > 0; rootPosition--) {
            ViewRoot viewRootData = viewRoots.get(rootPosition);
            if (isVisibleOnScreen(viewRootData.view))
                break;
        }
        return viewRoots.get(rootPosition);

    }

    @Nullable
    public static ViewGroup getViewGroup(ViewRoot viewRootData) {
        ViewGroup rootView;
        if (!(viewRootData.view instanceof ViewGroup))
            return null;
        rootView = (ViewGroup) viewRootData.view;
        return rootView;
    }

    private static boolean isVisibleOnScreen(View view) {
        if (view instanceof ViewGroup) {
            if (view.getVisibility() != View.VISIBLE)
                return false;
            if (!view.willNotDraw())
                return true;
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                if (isVisibleOnScreen(((ViewGroup) view).getChildAt(i))) {
                    return true;
                }
            }
        } else if (view.getVisibility() == View.VISIBLE) {
            return true;
        }

        return false;

    }

    public View getView() {
        return view;
    }

    public boolean isValidView() {
        if (isToast()) {
            return false;
        } else if (isProgressDialog()) {
            return false;
        }
        return true;
    }

    private boolean isToast() {
        return lParams.type == WindowManager.LayoutParams.TYPE_TOAST;
    }

    private boolean isProgressDialog() {
        if (containsProgressBar(view)) {
            return true;
        }
        return false;
    }

    private boolean containsProgressBar(View view) {
        if (view instanceof ViewGroup && view.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                if (view.getVisibility() == View.VISIBLE && containsProgressBar(((ViewGroup) view).getChildAt(i))) {
                    return true;
                }
            }
        }
        if (view instanceof ProgressBar && view.getVisibility() == View.VISIBLE) {
            return true;
        }

        return false;
    }

    boolean isPopUpType() {
        return lParams.type == WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
    }

    @ROOT_TYPE
    public int getType() {
        if (isActivityType()) return ACTIVITY;
        else if (isToast()) return TOAST;
        else if (isPopUpType()) return POPUP;
        else if (isDialogType()) return DIALOG;
        else if (isProgressDialog()) return PROGRESS_DIALOG;

        return NONE;
    }

    public static final int NONE = 0;
    public static final int ACTIVITY = 1;
    public static final int TOAST = 2;
    public static final int POPUP = 3;
    public static final int DIALOG = 4;
    public static final int PROGRESS_DIALOG = 5;

    @Retention(SOURCE)
    @IntDef({NONE, ACTIVITY, TOAST, POPUP, DIALOG, PROGRESS_DIALOG})
    public @interface ROOT_TYPE {
    }

}
