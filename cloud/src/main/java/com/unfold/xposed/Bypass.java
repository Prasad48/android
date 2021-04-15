package com.unfold.xposed;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewTreeObserver;
import android.widget.Button;

import com.useriq.Logger;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by appunfold on 21/04/17.
 */

public class Bypass implements IXposedHookLoadPackage {
    private static String TAG = Bypass.class.getSimpleName();
    static UnfoldSdk unfoldSdk;
    static Logger crashLogger = Logger.init(TAG);

    static ArrayList<String> mHijackedPackages = new ArrayList<>();

    static {
        Log.w(TAG, "Initializing Bypass");
    }

    public Bypass() {
        Log.w(TAG, "Constructing Bypass");
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.w(TAG, "handleLoadPackage: " + argsToString(new Object[]{lpparam}));
        autoAcceptPermissionsDialog(lpparam);
        new FakeBattery().fakePinStt(lpparam);
        new FakeHardwareInfo(lpparam);
        new FakeBuildInfo(lpparam);
        new FakeOpenGL().FakeDisplay(lpparam);
        new FakeEmail().fakeGmail(lpparam);
        new FakeCPU(lpparam);
        new ReactNativeSupport(lpparam);

        // While debugging, comment this. It hangs at "wait for debugger"
        new RootCloak().handleLoadPackage(lpparam);

        // For android version oreo or greater the "android package" is not called for any app at the time of booting.
        // But It gets called when the app is installed or re installed. The following adb commands work
        // 1. adb install yourApp.apk
        // 2. adb install -r yourApp.apk

        if (lpparam.packageName.equals("android")) {
            autoDismissAnrDialog(lpparam);
            // hookAndroidPackage(lpparam); // We no longer have UserIQActivity
        }

        disableIntegratedSdk(lpparam);
        disableRNSetUser(lpparam);
        disableUserIQPublicApis(lpparam);
        // TODO: check if this works for apps without support library
        ignoreUnhandledKeyDispatchEvent(lpparam);
//        disableOnReactEvent(lpparam);

        XposedHelpers.findAndHookMethod(android.app.Activity.class, "performCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.w(TAG, "before: Activity.performCreate" + argsToString(param.args));
                if (unfoldSdk != null) {
                    Activity activity = (Activity) param.thisObject;
                    Bundle extras = activity.getIntent().getExtras();
                    String sessionId = null;
                    String hostURL = null;
                    String apiKey = null;
                    String serverURL = null;
                    boolean disableSDK = false;
                    boolean disableAnalytics = false;
                    if (extras != null) {
                        sessionId = extras.getString("appunfoldUid");
                        hostURL = extras.getString("hostURL");
                        apiKey = extras.getString("apiKey");
                        serverURL = extras.getString("serverURL");
                        disableAnalytics = extras.getBoolean("disableAnalytics");
                        disableSDK = extras.getBoolean("disableSDK");
                    }
                    if (sessionId != null || hostURL != null || apiKey != null || serverURL != null) {
                        unfoldSdk.updateIntentData(sessionId, hostURL, apiKey, serverURL, disableAnalytics, disableSDK);
                    }
                }
                super.beforeHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod(android.app.Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.w(TAG, "before: Activity.performCreate" + argsToString(param.args));
                XposedBridge.log("onCreate: before");
                unfoldSdk = UnfoldSdk.init((Application) param.thisObject);

                hookUncaughtException((Application) param.thisObject);

            }

        });

     /*

     TODO: Need for touch handling

     XposedHelpers.findAndHookMethod(android.view.View.class, "onTouchEvent", android.view.MotionEvent.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if ((boolean) param.getResult()) {
                    MotionEvent motionEvent = (MotionEvent) param.args[0];
                    View view = (View) param.thisObject;
                    unfoldSdk.handleTouchOnView(motionEvent, view);
                }
            }
        });
        XposedHelpers.findAndHookMethod(android.widget.ScrollView.class, "onTouchEvent", android.view.MotionEvent.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if ((boolean) param.getResult()) {
                    MotionEvent motionEvent = (MotionEvent) param.args[0];
                    View view = (View) param.thisObject;
                    unfoldSdk.handleTouchOnView(motionEvent, view);
                }
            }
        });


        XposedHelpers.findAndHookMethod(android.widget.AbsListView.class, "onTouchEvent", android.view.MotionEvent.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if ((boolean) param.getResult()) {
                    MotionEvent motionEvent = (MotionEvent) param.args[0];
                    View view = (View) param.thisObject;
                    unfoldSdk.handleTouchOnView(motionEvent, view);

                }
            }
        });
*/

    }

    private void hookAndroidPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        /*
        PackageParser class parse the apk file and provide information regarding all the component declared in AndroidManifest.
        We will hook after the parseActivity method got called for the first time for some activity (it get called for receivers also).
        We will use the data passed into the parseActivity to create our UserIQActivity object. Thus we will add created UserIQActivity
        to list of activity to the list of activity
         */
        final Class<?> packageParserClass = XposedHelpers.findClass(
                "android.content.pm.PackageParser", lpparam.classLoader);
        final Class<?> packageClass = XposedHelpers.findClass(
                "android.content.pm.PackageParser.Package", lpparam.classLoader);

        Method parseActivity = (Util.isOreoOrGreater())
                    /*  source - https://github.com/aosp-mirror/platform_frameworks_base/blob/oreo-release/core/java/android/content/pm/PackageParser.java
                        Find Activity parseActivity(Package owner, Resources res,
                            XmlResourceParser parser, int flags, String[] outError,
                            boolean receiver, boolean hardwareAccelerated) */
                ? XposedHelpers.findMethodBestMatch(packageParserClass, "parseActivity", packageClass, Resources.class, XmlResourceParser.class, Integer.class, String[].class, boolean.class, Boolean.class)
                    /* source - https://android.googlesource.com/platform/frameworks/base/+/56a2301/core/java/android/content/pm/PackageParser.java
                        Find Activity parseActivity(Package owner, Resources res,
                            XmlPullParser parser, AttributeSet attrs, int flags, String[] outError,
                            boolean receiver, boolean hardwareAccelerated) */
                : XposedHelpers.findMethodBestMatch(packageParserClass, "parseActivity", packageClass, Resources.class, XmlPullParser.class, AttributeSet.class, Integer.class, String[].class, boolean.class, Boolean.class);

        XposedBridge.hookMethod(parseActivity, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Log.w(TAG, "after: PackageParser.parseActivity" + argsToString(param.args));
                Object owner = param.args[0];
                ArrayList activities = (ArrayList) XposedHelpers.getObjectField(owner, "activities");

                boolean isReceiver = (Util.isOreoOrGreater())
                        ? (boolean) param.args[5]
                        : (boolean) param.args[6];

                String packageName = (String) XposedHelpers.getObjectField(owner, "packageName");
                XposedBridge.log("I am Hooked: " + packageName);

                if (!isReceiver) {
                    //Need for debugging. To keep track of hijacked packages
                    mHijackedPackages.add(packageName);


                        /*
                         Activity(final ParseComponentArgs args, final ActivityInfo _info)
                         Thus activity constructor need ParseComponentArgs, we can reuse mParseComponentArgs but it is composed of TypedArray object of
                         activity's attributes and this TypedArray is recycled in parseActivity definition, so we have to recreate TypedArray and set
                         that in mParseComponentArgs object. TypedArray of activity attributes will be created using R.styleable.AndroidManifestActivity and attributeset.
                         */

                    /* Create TypedArray using R.styleable.AndroidManifestActivity and attributese*/
                    Resources res = (Resources) param.args[1];
                    Class r = XposedHelpers.findClass("com.android.internal.R$styleable", param.getClass().getClassLoader());
                    Field androidManifest = XposedHelpers.findField(r, "AndroidManifestActivity");
                    androidManifest.setAccessible(true);
                    int[] androidManifestAttrs = (int[]) androidManifest.get(int[].class);

                    TypedArray sa;
                    if (Util.isOreoOrGreater()) {
                        XmlResourceParser parser = (XmlResourceParser) param.args[2];
                        sa = res.obtainAttributes(parser, androidManifestAttrs);

                    } else {
                        AttributeSet attributeSet = (AttributeSet) param.args[3];
                        sa = res.obtainAttributes(attributeSet, androidManifestAttrs);
                    }

                    Object mParseActivityArgs = XposedHelpers.getObjectField(param.thisObject, "mParseActivityArgs");

                    /* Update TypedArray in mParseActivityArgs*/
                    XposedHelpers.setObjectField(mParseActivityArgs, "sa", sa);

                    String name = "com.useriq.sdk.UserIQActivity";
                    String pN = "com.unfold.xposed";
                    //Get the activity object that is about to be returned by parseActivity()
                    Object activity = param.getResult();

                    //Take the ActivityInfo object from activity object
                    ActivityInfo info = (ActivityInfo) XposedHelpers.getObjectField(activity, "info");

                    //clone the ActivityInfo object
                    ActivityInfo newInfo = new ActivityInfo(info);
                    //This theme is required by activity to make it visible. Also if title bar will be visible in the earlier activity then
                    //activity will try to get the application title icon from the resources and as we have overridden the
                    //getResource() thus it will not get the icon and thus crash.
                    newInfo.theme = android.R.style.Theme_Translucent;
                    //   newInfo.theme=R.style.Appunfold_Theme_Transparent;
                        /*
                        Update the name and packageName field int the cloned ActivityInfo object
                         */
                    XposedHelpers.setObjectField(newInfo, "name", name);
                    XposedHelpers.setObjectField(newInfo, "packageName", pN);

                        /*
                        Make a new activity object using the cloned and updated ActivityInfo Object and mParseActivityArgs in
                        which we updated the TypeArray
                         */
                    Object newActivity = XposedHelpers.newInstance(activity.getClass(), mParseActivityArgs, newInfo);
                        /*
                        Update the classname field in the newly created activity
                         */
                    XposedHelpers.setObjectField(newActivity, "className", name);

                        /*
                        Add the newly created activity to the list of activities
                         */
                    activities.add(newActivity);


                        /*
                        Recycle the typed array that has been used to create out activity
                         */
                    sa.recycle();
                        /*
                        HACK: For now We have updated the outError[0] i.e "does not specify android:name" that was coming after
                        the creation of our activity to null. If we will not update this then further parsing of apk will stop thus fail.
                        It is not breaking anything for now. But later we have to look into the root cause of this error/
                         */

                    String[] error = Util.isOreoOrGreater()
                            ? (String[]) param.args[4]
                            : (String[]) param.args[5];

                    error[0] = null;
                }

                super.afterHookedMethod(param);
            }
        });

    }

    private void autoAcceptPermissionsDialog(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals("com.android.packageinstaller")) {
            final Class<?> permissionDefaultViewHandler = XposedHelpers.findClass(
                    "com.android.packageinstaller.permission.ui.GrantPermissionsDefaultViewHandler", lpparam.classLoader);
            Method updateUi = XposedHelpers.findMethodBestMatch(permissionDefaultViewHandler, "updateUi", String.class, int.class, int.class, Icon.class, CharSequence.class, boolean.class);
            XposedBridge.hookMethod(updateUi, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object grantPermissionsViewHandler = param.thisObject;
                    final Button mAllowButton = (Button) XposedHelpers.getObjectField(grantPermissionsViewHandler, "mAllowButton");

                    if (mAllowButton.getWindowToken() != null) {
                        mAllowButton.performClick();
                    } else {
                        mAllowButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                mAllowButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                mAllowButton.performClick();
                            }
                        });
                    }

                }
            });
        }
    }

    private void autoDismissAnrDialog(XC_LoadPackage.LoadPackageParam lpparam) {
        final Class<?> appNotRespondingDialogClass = XposedHelpers.findClass(
                "com.android.server.am.AppNotRespondingDialog", lpparam.classLoader);
        final Class<?> appErrorDialogClass = XposedHelpers.findClass(
                "com.android.server.am.AppErrorDialog", lpparam.classLoader);

        Method showANRDialog = XposedHelpers.findMethodBestMatch(appNotRespondingDialogClass, "show");
        //   Method showAEDialog = XposedHelpers.findMethodBestMatch(appErrorDialogClass, "show");
        XposedBridge.hookMethod(showANRDialog, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Button positiveBtn = (Button) XposedHelpers.callMethod(param.thisObject, "getButton", DialogInterface.BUTTON_POSITIVE);
                positiveBtn.performClick();
            }
        });
        XposedHelpers.setStaticLongField(appErrorDialogClass, "DISMISS_TIMEOUT", 0);
    }

    /*
     * If the app already has UserIQ SDK, we will disable the internal SDK by making the variable "mSdkDisabled" true
     * */
    private void disableIntegratedSdk(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.toLowerCase().contains("xposed")) {
            Class appunfoldBuild = XposedHelpers.findClassIfExists("com.useriq.sdk.UserIQSDKInternal", lpparam.classLoader);
            if (appunfoldBuild != null) {
                try {
                    XposedHelpers.setStaticBooleanField(appunfoldBuild, "mSdkDisabled", true);
                    Log.d(TAG, "disabled internal UserIQ SDK");
                } catch (Exception e) {
                    //Doesn't matter
                }
            }
        }
    }

    /*
     * Bypassing the call of SDK's setUser from React native bridge, since the SDK is initialized by Xposed
     * */
    private void disableRNSetUser(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.toLowerCase().contains("xposed")) {
            Class rnClass = XposedHelpers.findClassIfExists("com.useriq.rn.UserIQReactNativeModule", lpparam.classLoader);
            Class rnReadableMapClass = XposedHelpers.findClassIfExists("com.facebook.react.bridge.ReadableMap", lpparam.classLoader);

            if (rnClass != null && rnReadableMapClass != null) {
                try {
                    Method method = XposedHelpers.findMethodExact(rnClass, "setUser", rnReadableMapClass);

                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(null);
                            Log.d(TAG, "ignoring setUser from React Native Module");
                        }
                    });

                } catch (Exception e) {
                    //Doesn't matter
                }
            }
        }
    }

    /*
     * Whenever the showHelpCentre or showCtxHelp api is called, we are bypassing the call and making it from Xposed.
     * The UserIQSDKInternal's instance will be null for the app's internal SDK.
     * */
    private void disableUserIQPublicApis(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.toLowerCase().contains("xposed")) {
            Class userIQClass = XposedHelpers.findClassIfExists("com.useriq.sdk.UserIQSDK", lpparam.classLoader);

            if (userIQClass != null) {
                try {
                    // ShowHelpCentre
                    Method showHelpCentre = XposedHelpers.findMethodExact(userIQClass, "showHelpCentre");
                    XposedBridge.hookMethod(showHelpCentre, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(false);
                            Log.d(TAG, "ignoring showHelpCentre method call from the app's SDK");
                            unfoldSdk.showHelpCentre();
                        }
                    });

                    // ShowCtxHelp
                    Method showCtxHelp = XposedHelpers.findMethodExact(userIQClass, "showCtxHelp");
                    XposedBridge.hookMethod(showCtxHelp, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(false);
                            Log.d(TAG, "ignoring showCtxHelp method call from the app's SDK");
                            unfoldSdk.showCtxHelp();
                        }
                    });
                } catch (Exception e) {
                    Log.d(TAG, "disableUserIQPublicApis: " + e.getMessage());
                }
            }
        }
    }

    private void disableOnReactEvent(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.toLowerCase().contains("xposed")) {
            Class useriqInternalClass = XposedHelpers.findClassIfExists("com.useriq.sdk.UserIQSDKInternal", lpparam.classLoader);

            if (useriqInternalClass != null) {
                try {
                    Method method = XposedHelpers.findMethodExact(useriqInternalClass, "onReactEvent", String.class, int.class);

                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(null);
                            Log.e(TAG, "ignoring onReactEvent in UserIQSDKInternal");
                        }
                    });

                } catch (Exception e) {
                    //Doesn't matter
                }
            }
        }
    }

    /*
     * This was done for supporting the onPaste from dashboard to the emulator. On pasting text from the dashboard,
     * two "dispatchKeyShortcutEvent" getting called for single key, which was creating the crash (reason not known).
     * So, we are consuming the second event and preventing the call to the original method.
     * */
    private void ignoreUnhandledKeyDispatchEvent(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.toLowerCase().contains("xposed")) {
            Class decorViewClass = XposedHelpers.findClassIfExists("com.android.internal.policy.PhoneWindow$DecorView", lpparam.classLoader);
            if (decorViewClass != null) {
                try {
                    Method method = XposedHelpers.findMethodExactIfExists(decorViewClass, "dispatchKeyShortcutEvent", KeyEvent.class);
                    if (method != null) {
                        final int[] i = {0};
                        XposedBridge.hookMethod(method, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                try {
                                    i[0]++;
                                    if (i[0] == 2) {
                                        Log.d(TAG, "consuming the dispatchKeyShortcutEvent event");
                                        param.setResult(true);
                                        i[0] = 0;
                                        Log.d(TAG, "resetting the counter for consuming dispatchKeyShortcutEvent");
                                    }
                                } catch (StackOverflowError e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    //Doesn't matter
                }
            }
        }
    }

    /*
     * Logging the crashes to the web dashboard.
     * */
    private void hookUncaughtException(final Application app) {
        Class<?> c = Thread.getDefaultUncaughtExceptionHandler().getClass();
        final String packageName = app.getPackageName();
        Method method = XposedHelpers.findMethodExactIfExists(c, "uncaughtException", Thread.class, Throwable.class);

        if (method == null) {
            crashLogger.e("Unable to attach crashLogger for '" + packageName + "'", null);
            return;
        }

        XposedBridge.hookMethod(method, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                crashLogger.e("AppCrash: " + packageName, (Throwable) param.args[1]);
            }
        });
    }

    private String argsToString(Object[]... args) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");

        for (Object arg : args) {
            if (arg != null) {
                sb.append("'");
                sb.append(arg.toString());
                sb.append("', ");
            }
        }

        sb.append(")");

        return sb.toString();
    }

}
