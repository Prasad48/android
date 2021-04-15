package com.unfold.xposed;

import android.app.Activity;
import android.util.Log;

import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.capture.Reflect;
import com.useriq.sdk.util.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class ReactNativeSupport {
    private static String TAG = ReactNativeSupport.class.getSimpleName();

    ReactNativeSupport(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.toLowerCase().contains("xposed")) return;
        Log.d(TAG, "packageName: " + lpparam.packageName);

        setupTestId(lpparam);
        addReactNativeLCHooks(lpparam);
        addReactNativeEDHooks(lpparam);
    }

    /*
     * Getting the resourceId for the "react_test_id" and setting the value in the "Utils" class of SDK
     * */
    private void setupTestId(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> nativeResource = XposedHelpers.findClassIfExists("com.facebook.react.R$id", lpparam.classLoader);

        Log.d(TAG, "nativeResource: " + ", " + nativeResource);

        if (nativeResource == null) return;

        int react_test_id = XposedHelpers.getStaticIntField(nativeResource, "react_test_id");
        Log.w(TAG, "react_test_id: " + react_test_id);

        try {
            Field field = Utils.class.getDeclaredField("TEST_ID_KEY");
            field.setAccessible(true);
            field.setInt(null, react_test_id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    /*
     * Hooking the React native's Lifecycle events "onHostPause" and "onHostResume",
     * since the SDK doesn't have the react native dependency.
     * */
    private void addReactNativeLCHooks(XC_LoadPackage.LoadPackageParam lpparam) {
        String className = "com.facebook.react.bridge.ReactContext";
        Class<?> klass = XposedHelpers.findClassIfExists(className, lpparam.classLoader);
        if (klass == null) {
            Log.w(TAG, className + " is null");
            return;
        }

        Method onPause = XposedHelpers.findMethodExactIfExists(klass, "onHostPause");
        Method onResume = XposedHelpers.findMethodExactIfExists(klass, "onHostResume", Activity.class);

        Log.d(TAG, "klass: " + klass + ", onHostPause: " + onPause + ", onHostResume: " + onResume);

        if (onPause != null) XposedBridge.hookMethod(onPause, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.w(TAG, "before: ReactContext.onPause");
                if (UserIQSDKInternal.getInstance() != null) {
                    UserIQSDKInternal.getInstance().onReactNativePause();
                }
            }
        });

        if (onResume != null) XposedBridge.hookMethod(onResume, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.w(TAG, "before: ReactContext.onResume");
                if (UserIQSDKInternal.getInstance() != null) {
                    UserIQSDKInternal.getInstance().onReactNativeResume();
                }
            }
        });
    }

    /*
     * Hooking for the events dispatched by the React native EventDispatcher, only interested
     * in the touch events.
     * */
    private void addReactNativeEDHooks(XC_LoadPackage.LoadPackageParam lpparam) {
        String eventDispatcherClassName = "com.facebook.react.uimanager.events.EventDispatcher";
        Class<?> eventDispatcherKlass = XposedHelpers.findClassIfExists(eventDispatcherClassName, lpparam.classLoader);
        if (eventDispatcherKlass == null) {
            Log.w(TAG, eventDispatcherClassName + " is null");
            return;
        }

        String eventClassName = "com.facebook.react.uimanager.events.Event";
        Class<?> eventKlass = XposedHelpers.findClassIfExists(eventClassName, lpparam.classLoader);
        if (eventKlass == null) {
            Log.w(TAG, eventClassName + " is null");
            return;
        }

        String touchEventClassName = "com.facebook.react.uimanager.events.TouchEvent";
        Class<?> touchEventKlass = XposedHelpers.findClassIfExists(eventClassName, lpparam.classLoader);
        if (touchEventKlass == null) {
            Log.w(TAG, touchEventClassName + " is null");
            return;
        }

        Method dispatchEvent = XposedHelpers.findMethodExactIfExists(eventDispatcherKlass, "dispatchEvent", eventKlass);

        Log.d(TAG, "eventDispatcherKlass: " + eventDispatcherKlass + ", dispatchEvent: " + dispatchEvent);

        if (dispatchEvent != null) XposedBridge.hookMethod(dispatchEvent, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.w(TAG, "before: EventDispatcher.dispatchEvent-> arg1 = " + param.args[0].toString());

                int viewTag = (int) Reflect.getFieldValue("mViewTag", param.args[0]);
                String eventName = Reflect.invoke(param.args[0], "getEventName", null).toString();
                Log.w(TAG, "Event called on viewTag: " + viewTag + " eventName: " + eventName);
                if (UserIQSDKInternal.getInstance() != null) {
                    UserIQSDKInternal.getInstance().onReactEvent(eventName, viewTag);
                }
            }
        });
    }

}
