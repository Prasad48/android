package com.useriq.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.useriq.Logger;
import com.useriq.SimpleRPC;
import com.useriq.sdk.capture.Capture;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.useriq.Logger.Level.DEBUG;

public class SDKService implements SimpleRPC.IService {
    private static Logger logger = Logger.init(SDKService.class.getSimpleName());

    private ActivityTracker activityTracker;
    private SyncListener syncListener;

    public SDKService(ActivityTracker activityTracker, @NonNull SyncListener syncListener) {
        this.activityTracker = activityTracker;
        this.syncListener = syncListener;
    }

    @SimpleRPC.Expose
    public Map getAppInfo() {
        if (activityTracker.getCurrentActivity() == null) {
            logger.e("capture failed; as current activity is null", null);
            return null;
        }
        Activity currentActivity = activityTracker.getCurrentActivity();
        Context appCtx = currentActivity.getApplicationContext();
        return buildAppInfo(appCtx);
    }

    @SimpleRPC.Expose
    public Map captureScreen() {
        if (activityTracker.getCurrentActivity() == null) {
            logger.e("capture failed; as current activity is null", null);
            return null;
        }
        try {
            return getScreenInfo(activityTracker.getCurrentActivity(), true);
        } catch (Capture.CaptureException e) {
            logger.e("capture screen failed: " + e.getMessage(), e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.e("capture screen failed: " + e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @SimpleRPC.Expose
    public void syncApp(@Nullable Map data) {
        if (data == null) {
            logger.e("syncApp called with null data", null);
            return;
        }
        if(logger.isAtleast(DEBUG)) {
            String syncKeys = new JSONArray(data.keySet()).toString();
            logger.d("Received Sync with keys: " + syncKeys);
        }

        syncListener.onSync(data);
    }

    @SimpleRPC.Expose
    public void activate(Map data) {
        if(logger.isAtleast(DEBUG)) {
            String str = new JSONObject(data).toString();
            logger.d("Received activate: " + str);
        }

        String type = (String) data.get("type");
        Object object = data.get("value");
        String value = object instanceof String ? (String) object : null;

        syncListener.onActivate(type, value);
    }

    private static HashMap<String, Object> getScreenInfo(final Activity activity, boolean captureScreenImage) throws Capture.CaptureException, InterruptedException {
        HashMap<String, Object> screenInfo = new HashMap<>();
        Capture.Info captureInfo = Capture.from(activity, captureScreenImage);
        HashMap<String, Object> image = new HashMap<>();
        if (!captureInfo.isValid) {
            screenInfo.put("invalid", true);
        }
        screenInfo.put("controller", activity.getClass().getCanonicalName());
        screenInfo.put("windows", captureInfo.layout);
        screenInfo.put("props", captureInfo.meta);
        if (captureScreenImage) {
            ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
            captureInfo.image.compress(Bitmap.CompressFormat.JPEG, 50, imgOut);
            image.put("data", imgOut.toByteArray());
            image.put("fileName", UUID.randomUUID() + ".jpeg");
            screenInfo.put("img", image);
        }
        return screenInfo;
    }

    private static HashMap<String, Object> buildAppInfo(Context ctx) {
        PackageManager pm = ctx.getPackageManager();

        HashMap<String, Object> map = new HashMap<>();

        String title = pm.getApplicationLabel(ctx.getApplicationInfo()).toString();
        map.put("title", title);

        map.put("logo", getLogoBytes(ctx));

        return map;
    }

    private static byte[] getLogoBytes(Context ctx) {
        String packageName = ctx.getPackageName();
        PackageManager pm = ctx.getPackageManager();

        Drawable appLogo;
        try {
            appLogo = pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
        drawableToBitmap(appLogo).compress(Bitmap.CompressFormat.JPEG, 100, imgOut);

        return imgOut.toByteArray();
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.RGB_565);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    interface SyncListener {
        void onSync(Map data);
        void onActivate(String type, String value);
    }
}
