package com.unfold.xposed;

import android.content.ContentResolver;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.Parcel;
import android.provider.Settings;
import android.webkit.WebView;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class FakeBuildInfo {


    public FakeBuildInfo(LoadPackageParam sharePkgParam) {
       // FakeGPS(sharePkgParam);
        FakeAndroidID(sharePkgParam);
        FakeAndroidSerial(sharePkgParam);
        FakeIMEI(sharePkgParam);
        FakeBaseBand(sharePkgParam);
        FakeBuildProp(sharePkgParam);
        FakeUserAgent(sharePkgParam);
        // FakeGoogleAdsID(sharePkgParam);
    }

    public void FakeUserAgent(LoadPackageParam loadPkgParam) {

        //if(!loadPkgParam.packageName.contains("com.bbm")){
        //isProviderEnabled
        try {
            XposedHelpers.findAndHookMethod("com.android.webview.chromium.ContentSettingsAdapter", loadPkgParam.classLoader, "setUserAgentString", String.class, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    param.args[0] = FakeConfigurationConstants.USER_AGENT;
                }

            });
        } catch (ClassNotFoundError e) {
            XposedBridge.log("Fake UA ERROR: " + e.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod("com.android.webview.chromium.ContentSettingsAdapter", loadPkgParam.classLoader, "setUserAgentString", String.class, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    param.args[0] = FakeConfigurationConstants.USER_AGENT;
                }

            });
        } catch (ClassNotFoundError e) {
            XposedBridge.log("Fake UA ERROR: " + e.getMessage());
        }

        try {
            Method loadUrl1 = WebView.class.getDeclaredMethod("loadUrl", new Class[]{String.class});
            Method loadUrl2 = WebView.class.getDeclaredMethod("loadUrl", new Class[]{String.class, Map.class});

            XposedBridge.hookMethod(loadUrl1, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    XposedBridge.log("Load Url: " + param.args[0]);
                    if (param.args.length > 0 && (param.thisObject instanceof WebView)) {
                        String ua = FakeConfigurationConstants.USER_AGENT;
                        WebView webView = (WebView) param.thisObject;
                        if (webView.getSettings() != null) {
                            webView.getSettings().setUserAgentString(ua);
                        }
                    }
                }

            });
            XposedBridge.hookMethod(loadUrl2, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    XposedBridge.log("load url: " + param.args[0]);
                    if (param.args.length > 0 && (param.thisObject instanceof WebView)) {
                        String ua = FakeConfigurationConstants.USER_AGENT;
                        WebView webView = (WebView) param.thisObject;
                        if (webView.getSettings() != null) {
                            webView.getSettings().setUserAgentString(ua);
                        }
                    }
                }

            });

        } catch (Exception e) {
            XposedBridge.log("Fake User Agent ERROR: " + e.getMessage());
        }
        //}
    }

    public void FakeGPS(LoadPackageParam loadPkgParam) {
        try {

            XposedHelpers.findAndHookMethod("android.location.Location", loadPkgParam.classLoader, "getLatitude", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    param.setResult(FakeConfigurationConstants.LATITUDE);
                }

            });
            XposedHelpers.findAndHookMethod("android.location.Location", loadPkgParam.classLoader, "getLongitude", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    param.setResult(FakeConfigurationConstants.LONGITUDE);
                }

            });
            XposedHelpers.findAndHookMethod("android.location.Location", loadPkgParam.classLoader, "getAccuracy", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    param.setResult(FakeConfigurationConstants.ALTITUDE);
                }

            });
            XposedHelpers.findAndHookMethod("android.location.Location", loadPkgParam.classLoader, "getAltitude", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    param.setResult(FakeConfigurationConstants.ALTITUDE);
                }

            });
            XposedHelpers.findAndHookMethod("android.location.Location", loadPkgParam.classLoader, "getSpeed", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    param.setResult(FakeConfigurationConstants.SPEED);
                }

            });


        } catch (Exception e) {
            XposedBridge.log("Fake GPS ERROR: " + e.getMessage());
        }
    }

    public void FakeAndroidID(LoadPackageParam loadPkgParam) {
        try {
            XposedHelpers.findAndHookMethod("android.provider.Settings.Secure", loadPkgParam.classLoader, "getString", ContentResolver.class, String.class, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {

                    if (param.args[1].equals(Settings.Secure.ANDROID_ID)) {
                        param.setResult(FakeConfigurationConstants.ANDROID_ID);
                    }
                }
            });

        } catch (Exception ex) {
            XposedBridge.log("Fake Android ID ERROR: " + ex.getMessage());
        }
    }

    public void FakeAndroidSerial(LoadPackageParam loadPkgParam) {
        try {
            Class<?> classBuild = XposedHelpers.findClass("android.os.Build",
                    loadPkgParam.classLoader);
            XposedHelpers.setStaticObjectField(classBuild, "SERIAL",
                    FakeConfigurationConstants.ANDROID_SERIAL);
            Class<?> classSysProp = Class
                    .forName("android.os.SystemProperties");
            XposedHelpers.findAndHookMethod(classSysProp, "get", String.class,
                    new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param)
                                throws Throwable {
                            // TODO Auto-generated method stub
                            super.afterHookedMethod(param);
                            String serialno = (String) param.args[0];
                            if (serialno.equals("ro.serialno")
                                    || serialno.equals("ro.boot.serialno")
                                    || serialno.equals("ril.serialnumber")
                                    || serialno.equals("sys.serialnumber")) {
                                param.setResult(FakeConfigurationConstants.ANDROID_SERIAL);
                            }
                        }

                    });
            XposedHelpers.findAndHookMethod(classSysProp, "get", String.class,
                    String.class, new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param)
                                throws Throwable {
                            // TODO Auto-generated method stub
                            super.afterHookedMethod(param);

                            String serialno = (String) param.args[0];
                            if (serialno.equals("ro.serialno")
                                    || serialno.equals("ro.boot.serialno")
                                    || serialno.equals("ril.serialnumber")
                                    || serialno.equals("sys.serialnumber")) {
                                param.setResult(FakeConfigurationConstants.ANDROID_SERIAL);
                            }
                        }

                    });
            return;

        } catch (IllegalArgumentException ex) {
            XposedBridge.log("Fake AndroidSerial ERROR: " + ex.getMessage());
            return;
        } catch (ClassNotFoundException ex) {
            XposedBridge.log("Fake AndroidSerial ERROR: " + ex.getMessage());
        }
    }

    public void FakeIMEI(LoadPackageParam loadPkgParam) {
        try {
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPkgParam.classLoader, "getDeviceId", XC_MethodReplacement.returnConstant(FakeConfigurationConstants.IMEI));
            //     XposedHelpers.findAndHookMethod("com.android.internal.telephony.PhoneSubInfo", loadPkgParam.classLoader, "getDeviceId", XC_MethodReplacement.returnConstant(FakeConfigurationConstants.IMEI));

            if (VERSION.SDK_INT < 22) {
                XposedHelpers.findAndHookMethod("com.android.internal.telephony.gsm.GSMPhone", loadPkgParam.classLoader, "getDeviceId", XC_MethodReplacement.returnConstant(FakeConfigurationConstants.IMEI));
                XposedHelpers.findAndHookMethod("com.android.internal.telephony.PhoneProxy", loadPkgParam.classLoader, "getDeviceId", XC_MethodReplacement.returnConstant(FakeConfigurationConstants.IMEI));
            }
        } catch (Exception ex) {
            XposedBridge.log("Fake IMEI ERROR: " + ex.getMessage());
        }
    }

    public void FakeGoogleAdsID(LoadPackageParam loadPkgParam) {
        try {
            XposedHelpers.findAndHookMethod("android.os.Binder", loadPkgParam.classLoader, "execTransact", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {

                    super.beforeHookedMethod(param);
                    if (((IBinder) param.thisObject)
                            .getInterfaceDescriptor()
                            .equals("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService")
                            && ((Integer) param.args[0]).intValue() == 1) {
                        Parcel reply = null;
                        try {

                            Method methodObtain = Parcel.class.getDeclaredMethod("obtain", VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? int.class : long.class);
                            methodObtain.setAccessible(true);
                            reply = (Parcel) methodObtain.invoke(null,
                                    param.args[2]);
                        } catch (NoSuchMethodException ex) {
                            XposedBridge.log("Fake Google Ads NoSuchMethodException ERROR: " + ex.getMessage());
                        } catch (NullPointerException e) {
                            XposedBridge.log("Fake Google Ads NullPointerException ERROR: " + e.getMessage());
                        }
                        if (reply == null) {

                        } else {
                            reply.setDataPosition(0);
                            reply.writeNoException();
                            reply.writeString(FakeConfigurationConstants.GOOGLE_ADS_ID);
                        }

                        param.setResult(Boolean.valueOf(true));
                    }

                }

            });

        } catch (Exception ex) {
            XposedBridge.log("Fake Google Ads ID ERROR: " + ex.getMessage());
        }
    }

    public void FakeBaseBand(LoadPackageParam loadPkgParam) {
        try {
            if (VERSION.SDK_INT <= 14) {
                Class<?> classBuild = XposedHelpers.findClass(
                        "android.os.Build", loadPkgParam.classLoader);
                XposedHelpers.setStaticObjectField(classBuild, "RADIO", FakeConfigurationConstants.BASE_BAND);
            } else {
                XposedHelpers.findAndHookMethod("android.os.Build",
                        loadPkgParam.classLoader, "getRadioVersion", new XC_MethodHook() {

                            @Override
                            protected void afterHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                param.setResult(FakeConfigurationConstants.BASE_BAND);
                            }

                        });
            }
        } catch (Exception e) {
            XposedBridge.log("Fake BaseBand ERROR: " + e.getMessage());
        }


    }


    public void FakeBuildProp(LoadPackageParam loadPkgParam) {
        try {
            XposedHelpers.findField(Build.class, "BOARD").set(null, FakeConfigurationConstants.BOARD);
            XposedHelpers.findField(Build.class, "BRAND").set(null, FakeConfigurationConstants.BRAND);
            XposedHelpers.findField(Build.class, "CPU_ABI").set(null, FakeConfigurationConstants.ABI);
            XposedHelpers.findField(Build.class, "CPU_ABI2").set(null, FakeConfigurationConstants.ABI2);
            XposedHelpers.findField(Build.class, "DEVICE").set(null, FakeConfigurationConstants.DEVICE);
            XposedHelpers.findField(Build.class, "DISPLAY").set(null, FakeConfigurationConstants.DISPLAY);
            XposedHelpers.findField(Build.class, "FINGERPRINT").set(null, FakeConfigurationConstants.FINGERPRINT);
            XposedHelpers.findField(Build.class, "HARDWARE").set(null, FakeConfigurationConstants.HARDWARE_NAME);
            XposedHelpers.findField(Build.class, "ID").set(null, FakeConfigurationConstants.ID);
            XposedHelpers.findField(Build.class, "MANUFACTURER").set(null, FakeConfigurationConstants.MANUFACTURE);
            XposedHelpers.findField(Build.class, "MODEL").set(null, FakeConfigurationConstants.MODEL);
            XposedHelpers.findField(Build.class, "PRODUCT").set(null, FakeConfigurationConstants.DEVICE);
            XposedHelpers.findField(Build.class, "BOOTLOADER").set(null, FakeConfigurationConstants.BOOTLOADER);
            XposedHelpers.findField(Build.class, "HOST").set(null, "kpfj3.cbf.corp.google.com");

            XposedHelpers.findField(VERSION.class, "INCREMENTAL").set(null, FakeConfigurationConstants.BOOTLOADER);
            XposedHelpers.findField(VERSION.class, "RELEASE").set(null, FakeConfigurationConstants.ANDROID_VER);
            XposedHelpers.findField(VERSION.class, "SDK").set(null, FakeConfigurationConstants.API);
            XposedHelpers.findField(VERSION.class, "CODENAME").set(null, "REL");

        } catch (IllegalAccessException e) {
            XposedBridge.log("Fake BuilProp ERROR: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            XposedBridge.log("Fake BuilProp ERROR: " + e.getMessage());
        }

        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
            if (cls != null) {
                for (Member mem : cls.getDeclaredMethods()) {
                    XposedBridge.hookMethod(mem, new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param)
                                throws Throwable {
                            // TODO Auto-generated method stub
                            super.beforeHookedMethod(param);

                            if (param.args.length > 0 && param.args[0] != null && param.args[0].equals("ro.build.description")) {
                                param.setResult(FakeConfigurationConstants.DESCRIPTION);
                            }
                        }
                    });
                }
            }

        } catch (ClassNotFoundException e) {
            XposedBridge.log("Fake DESCRIPTION ERROR: " + e.getMessage());
        }
    }
}
