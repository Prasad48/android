package com.useriq.sdk;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.useriq.sdk.models.SyncData;
import com.useriq.sdk.util.ScreenBoundsUtil;

import java.util.HashMap;
import java.util.Map;

public final class SDKConfig {
    public final String apiKey;
    public final String versionCode;
    public final String versionString;
    public final String storeKey;
    public final boolean fabDisabled;
    public final String emuId;
    public final String url;
    public Map<String, Object> user;

    SDKConfig(Application app, String apiKey, Map<String, Object> user, String emuId, String url, boolean fabDisabled) {
        this.apiKey = apiKey;
        this.versionCode = getVersionCode(app);
        this.versionString = getVersionString(app);
        this.emuId = emuId;
        this.url = url;
        this.user = user;
        this.storeKey = app.getPackageName();
        this.fabDisabled = fabDisabled;
    }

    private String getVersionCode(Application app) {
        PackageInfo pInfo = null;
        try {
            pInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            return String.valueOf(pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getVersionString(Application app) {
        PackageInfo pInfo = null;
        try {
            pInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            return String.valueOf(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean isEmu() {
        return emuId != null;
    }

    public Map<String, String> getHeaders() {
        SyncData syncData = UserIQSDKInternal.getSyncData();
        String formFactor = String.valueOf(ScreenBoundsUtil.isTablet() ? 2 : 1);

        Map<String, String> headers = new HashMap<>();
        headers.put("user-id", (String) user.get("userId"));
        headers.put("api-key", apiKey);
        headers.put("version-code", versionCode);
        headers.put("version-string", versionString);
        headers.put("store-key", storeKey);
        headers.put("sdk-version", String.valueOf(BuildConfig.VERSION_CODE));
        headers.put("sync-version", String.valueOf(syncData.version));
        headers.put("form-factor", formFactor);

        if (emuId != null) {
            headers.put("emu-id", emuId);
        }
        return headers;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nAPI_KEY: ");
        sb.append(apiKey);
        sb.append("\nSTORE_KEY: ");
        sb.append(storeKey);
        sb.append("\nUSER_ID: ");
        sb.append(user.get("userId"));

        return sb.toString();
    }
}
