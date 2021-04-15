package com.unfold.xposed;

import android.accounts.Account;
import android.text.TextUtils;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class FakeEmail {

    public static boolean getPackage(String lisPkg, String pkg) {
        if (TextUtils.isEmpty(lisPkg)) {
            return false;
        }
        return Arrays.asList(TextUtils.split(lisPkg.replace(" ", ""), ",")).contains(pkg);
    }

    public void fakeGmail(final LoadPackageParam loadPkgParam) {
        try {
            XposedHelpers.findAndHookMethod("android.accounts.AccountManager", loadPkgParam.classLoader, "getAccounts", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.afterHookedMethod(param);
                    if (getPackage(FakeConfigurationConstants.FAKE_EMAIL_PACKAGE, loadPkgParam.packageName)) {
                        param.setResult(new Account[]{new Account(FakeConfigurationConstants.EMAIL, "com.google")});

                    }
                }

            });
            XposedHelpers.findAndHookMethod("android.accounts.AccountManager", loadPkgParam.classLoader, "getAccountsByType", String.class, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.afterHookedMethod(param);
                    if (getPackage(FakeConfigurationConstants.FAKE_EMAIL_PACKAGE, loadPkgParam.packageName)) {
                        param.setResult(new Account[]{new Account(FakeConfigurationConstants.EMAIL, "com.google")});
                    }
                }

            });
        } catch (Exception e) {
            XposedBridge.log("Fake Email ERROR: " + e.getMessage());
        }

    }

}
