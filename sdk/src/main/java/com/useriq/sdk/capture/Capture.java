package com.useriq.sdk.capture;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.useriq.sdk.UIManager;
import com.useriq.sdk.UIRootView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smylsamy on 07/12/16.
 *
 * Based on https://github.com/jraska/Falcon
 */

public class Capture {
    private static final String ROTATION = "rotation";
    private static final Object lock = new Object();
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";

    public static Info from(Activity activity, boolean captureScreenImage) throws CaptureException {
        return from(activity, captureScreenImage, null);
    }

    public static Info from(final Activity activity, boolean captureScreenImage, PrimaryViewRootValidator primaryViewRootValidator) throws CaptureException {
        synchronized (lock) {
            boolean isValidPrimary = false;
            try {
                List<ViewRoot> viewRoots = ViewRoot.from(activity);
                ViewRoot rootView = ViewRoot.getPrimary(viewRoots);
                if (primaryViewRootValidator != null) {
                    isValidPrimary = primaryViewRootValidator.isValid(rootView);
                }
                int tagKey = -1;
                Bitmap image = null;
                Map<String, Object> meta = getMeta(activity);
                if (captureScreenImage) {
                    final UIRootView uiRootView = UIManager.getInstance().getUiRootView();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            uiRootView.detach();
                        }
                    });
                    image = ScreenImg.capture(viewRoots, (int) meta.get(WIDTH), (int) meta.get(HEIGHT));
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            uiRootView.attach(activity, ViewRoot.NONE);
                        }
                    });
                }

                ArrayList<ViewGroup> rootViewGroups = new ArrayList<>();
                for (int i = 0; i < viewRoots.size(); i++) {
                    rootViewGroups.add(ViewRoot.getViewGroup(viewRoots.get(i)));
                }
                ArrayList<Map<String, Object>> layout = LayoutTree.from(rootViewGroups);
                return new Info(image, layout, meta, isValidPrimary, tagKey);
            } catch (Exception e) {
                throw new CaptureException((e));
            }
        }
    }

    private static Map<String, Object> getMeta(Context ctx) {
        WindowManager window = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        Map<String, Object> map = new HashMap<>();
        map.put(ROTATION, display.getRotation());
        map.put(WIDTH, metrics.widthPixels);
        map.put(HEIGHT, metrics.heightPixels);
        return map;
    }

    /*
    TODO: remove the getfragment() call, as its usage is restricted for public
     */

    public static class Info {
        public final Bitmap image;
        public final ArrayList<Map<String, Object>> layout;
        public final Map<String, Object> meta;
        public final boolean isValid;
        public final int tag;

        Info(Bitmap image, ArrayList<Map<String, Object>> layout, Map<String, Object> meta, boolean isValid, Integer tag) {
            this.image = image;
            this.layout = layout;
            this.meta = meta;
            this.tag = tag;
            this.isValid = isValid;
        }
    }

    public static class CaptureException extends Exception {
        private CaptureException(Exception e) {
            super(e);
        }
    }
}
