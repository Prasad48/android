package com.useriq.sdk.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by arnold on 5/10/16.
 */

public class WindowUtil {

    private static final String TAG = WindowUtil.class.getSimpleName();

    public static boolean isViewDialogType(Activity activity) {
        List<WindowUtil.ViewRootData> rootViews = WindowUtil.getRootViews(activity);
        if (rootViews.size() > 0) {
            WindowUtil.ViewRootData viewRootData = rootViews.get(rootViews.size() - 1);
            if (viewRootData.isDialogType() || viewRootData.isPopUpType()) {
                return true;
            }
        }
        return false;
    }

    public static View getTopWindowForUxLayout(Activity activity) {
        List<WindowUtil.ViewRootData> rootViews = WindowUtil.getValidRootViews(activity);
        if (rootViews.size() == 0) {
            return null;
        }
        return rootViews.get(rootViews.size() - 1)._view;
    }

    public static List<ViewRootData> getValidRootViews(Activity activity) {
        List<ViewRootData> rootViews = getRootViews(activity);

        if (rootViews.size() == 0) {
            return rootViews;
        }
        if (rootViews.get(rootViews.size() - 1).isToast()) {
            rootViews.remove(rootViews.size() - 1);
        }
        // Ignore transient activity window from top

        if (rootViews.get(rootViews.size() - 1).isActivityType()) {
            if (activity.getWindow().getDecorView() != rootViews.get(rootViews.size() - 1)._view) {
                rootViews.remove(rootViews.size() - 1);
            }
        }
        return rootViews;
    }

    public static List<ViewRootData> getRootViews(Activity activity) {
        List<ViewRootData> rootViews = new ArrayList<>();

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

            Object attachInfo = getFieldValue("mAttachInfo", root);
            int top = (int) getFieldValue("mWindowTop", attachInfo);
            int left = (int) getFieldValue("mWindowLeft", attachInfo);

            Rect winFrame = (Rect) getFieldValue("mWinFrame", root);
            Rect area = new Rect(left, top, left + winFrame.width(), top + winFrame.height());
            rootViews.add(new ViewRootData(view, area, params[i]));
        }


        if (rootViews.isEmpty()) {
            return Collections.emptyList();
        }

        offsetRootsTopLeft(rootViews);
        ensureDialogsAreAboveItsParentActivities(rootViews);

        return rootViews;
    }

    private static Object getFieldValue(String fieldName, Object target) {
        try {
            return getFieldValueUnchecked(fieldName, target);
        } catch (Exception e) {
            throw new UnableToTakeScreenshotException(e);
        }
    }

    private static void offsetRootsTopLeft(List<ViewRootData> rootViews) {
        int minTop = Integer.MAX_VALUE;
        int minLeft = Integer.MAX_VALUE;
        for (ViewRootData rootView : rootViews) {
            if (rootView._winFrame.top < minTop) {
                minTop = rootView._winFrame.top;
            }

            if (rootView._winFrame.left < minLeft) {
                minLeft = rootView._winFrame.left;
            }
        }

        for (ViewRootData rootView : rootViews) {
            rootView._winFrame.offset(-minLeft, -minTop);
        }
    }

    // This fixes issue #11. It is not perfect solution and maybe there is another case
    // of different type of view, but it works for most common case of dialogs.
    private static void ensureDialogsAreAboveItsParentActivities(List<ViewRootData> viewRoots) {
        if (viewRoots.size() <= 1) {
            return;
        }

        for (int dialogIndex = 0; dialogIndex < viewRoots.size() - 1; dialogIndex++) {
            ViewRootData viewRoot = viewRoots.get(dialogIndex);
            if (!viewRoot.isDialogType()) {
                continue;
            }

            Activity dialogOwnerActivity = ownerActivity(viewRoot.context());
            if (dialogOwnerActivity == null) {
                // make sure we will never compare null == null
                return;
            }

            for (int parentIndex = dialogIndex + 1; parentIndex < viewRoots.size(); parentIndex++) {
                ViewRootData possibleParent = viewRoots.get(parentIndex);
                if (possibleParent.isActivityType()
                        && ownerActivity(possibleParent.context()) == dialogOwnerActivity) {
                    viewRoots.remove(possibleParent);
                    viewRoots.add(dialogIndex, possibleParent);

                    break;
                }
            }
        }
    }

    private static Object getFieldValueUnchecked(String fieldName, Object target)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = findField(fieldName, target.getClass());

        field.setAccessible(true);
        return field.get(target);
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

    private static Field findField(String name, Class clazz) throws NoSuchFieldException {
        Class currentClass = clazz;
        while (currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        throw new NoSuchFieldException("Field " + name + " not found for class " + clazz);
    }

    /**
     * For Dialogs which are the topMost Window
     */
    public static ViewRootData getTopWindowViewRoot(Activity activity) {
        List<WindowUtil.ViewRootData> rootViews = WindowUtil.getRootViews(activity);
        if (isViewDialogType(activity)) return rootViews.get(rootViews.size() - 1);

        return null;
    }

    /**
     * Custom exception thrown if there is some exception thrown during
     * screenshot capturing to enable better client code exception handling.
     */
    public static class UnableToTakeScreenshotException extends RuntimeException {
        private UnableToTakeScreenshotException(String detailMessage) {
            super(detailMessage);
        }

        private UnableToTakeScreenshotException(String detailMessage, Exception exception) {
            super(detailMessage, extractException(exception));
        }

        /**
         * Method to avoid multiple wrapping. If there is already our exception,
         * just wrap the cause again
         */
        private static Throwable extractException(Exception ex) {
            if (ex instanceof UnableToTakeScreenshotException) {
                return ex.getCause();
            }

            return ex;
        }

        private UnableToTakeScreenshotException(Exception ex) {
            super(extractException(ex));
        }
    }

    public static class ViewRootData {
        public final View _view;
        public final Rect _winFrame;
        public final WindowManager.LayoutParams _layoutParams;


        public ViewRootData(View view, Rect winFrame, WindowManager.LayoutParams layoutParams) {
            _view = view;
            _winFrame = winFrame;
            _layoutParams = layoutParams;
        }

        public boolean isDialogType() {
            // One of the bug reported by JustRide. checking null condition for now
            if (_layoutParams == null) return false;
            return _layoutParams.type == WindowManager.LayoutParams.TYPE_APPLICATION;
        }

        public boolean isPopUpType() {
            if (_layoutParams == null) return false;
            return _layoutParams.type == WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
        }

        public boolean isActivityType() {
            if (_layoutParams == null) return false;
            return _layoutParams.type == WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
        }

        public boolean isToast() {
            return _layoutParams.type == WindowManager.LayoutParams.TYPE_TOAST;
        }

        Context context() {
            return _view.getContext();
        }
    }

}
