package com.unfold.xposed;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class FakeHardwareInfo {

    public FakeHardwareInfo(LoadPackageParam sharePkgParam) {
        FakeBluetooth(sharePkgParam);
        FakeWifi(sharePkgParam);
        // FakeCPUFile(sharePkgParam);
        FakeTelephony(sharePkgParam);

    }

    public static boolean CreatDataCpu(Context context) {
        String str = "/data/data/" + context.getPackageName() + "/cpuinfo";
        String str2 = "/data/data/" + context.getPackageName() + "/version";
        try {
            AssetManager assets = context.getAssets();
            InputStream open = assets.open("cpuinfo");
            OutputStream fileOutputStream = new FileOutputStream(str);
            writeValue(open, fileOutputStream);
            open.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            InputStream open2 = assets.open("version");
            OutputStream fileOutputStream2 = new FileOutputStream(str2);
            writeValue(open2, fileOutputStream2);
            open2.close();
            fileOutputStream2.flush();
            fileOutputStream2.close();
            Sendfile(str, str2);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void writeValue(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] bArr = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
            while (true) {
                int read = inputStream.read(bArr);
                if (read != -1) {
                    outputStream.write(bArr, 0, read);
                } else {
                    return;
                }
            }
        } catch (Exception e) {
        }
    }

    private static void Sendfile(String str, String str2) {
        IOException e;
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(Runtime.getRuntime().exec("su").getOutputStream());
            try {
                dataOutputStream.writeBytes("mkdir /data/misc/sys/\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("chmod 777 /data/misc/sys/\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("cp " + str + " /data/misc/sys\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("chmod 444 /data/misc/sys/cpuinfo\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("rm " + str + "\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("cp " + str2 + " /data/misc/sys\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("chmod 444 /data/misc/sys/version\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("rm " + str2 + "\n");
                dataOutputStream.flush();
                dataOutputStream.close();
                if (new File("/data/misc/sys/cpuinfo").exists()) {
                    return;
                }
                throw new IOException();
            } catch (IOException e2) {
                e = e2;
                DataOutputStream dataOutputStream2 = dataOutputStream;
                e.printStackTrace();
            }
        } catch (IOException e3) {
            e = e3;
            e.printStackTrace();
        }
    }


    public void FakeCPUFile(LoadPackageParam loadPkgParam) {
        try {

            XposedBridge.hookAllConstructors(File.class, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    if (param.args.length == 1) {
                        if (param.args[0].equals("/proc/cpuinfo")) {
                            param.args[0] = "/data/misc/sys/cpuinfo";
                        }
                        if (param.args[0].equals("/proc/version")) {
                            param.args[0] = "/data/misc/sys/version";
                        }
                    } else if (param.args.length == 2 && !File.class.isInstance(param.args[0])) {
                        int i = 0;
                        String str = "";
                        while (i < 2) {
                            String stringBuilder;
                            if (param.args[i] != null) {
                                if (param.args[i].equals("/proc/cpuinfo")) {
                                    param.args[i] = "/data/misc/sys/cpuinfo";
                                }
                                if (param.args[i].equals("/proc/version")) {
                                    param.args[i] = "/data/misc/sys/version";
                                }
                                stringBuilder = new StringBuilder(String.valueOf(str)).append(param.args[i]).append(":").toString();
                            } else {
                                stringBuilder = str;
                            }
                            i++;
                            str = stringBuilder;
                        }
                    }
                }

            });

            XposedHelpers.findAndHookMethod("java.lang.Runtime", loadPkgParam.classLoader, "exec", String[].class, String[].class, File.class, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    if (param.args.length == 1) {
                        if (param.args[0].equals("/proc/cpuinfo")) {
                            param.args[0] = "/data/misc/sys/cpuinfo";
                        }
                        if (param.args[0].equals("/proc/version")) {
                            param.args[0] = "/data/misc/sys/version";
                        }
                    } else if (param.args.length == 2 && !File.class.isInstance(param.args[0])) {
                        int i = 0;
                        String str = "";
                        while (i < 2) {
                            String stringBuilder;
                            if (param.args[i] != null) {
                                if (param.args[i].equals("/proc/cpuinfo")) {
                                    param.args[i] = "/data/misc/sys/cpuinfo";
                                }
                                if (param.args[i].equals("/proc/version")) {
                                    param.args[i] = "/data/misc/sys/version";
                                }
                                stringBuilder = new StringBuilder(String.valueOf(str)).append(param.args[i]).append(":").toString();
                            } else {
                                stringBuilder = str;
                            }
                            i++;
                            str = stringBuilder;
                        }
                    }
                }

            });
        } catch (Exception e) {
            XposedBridge.log("Fake CPUFile - 1 ERROR: " + e.getMessage());
        }


        try {
            XposedBridge.hookMethod(XposedHelpers.findConstructorExact(ProcessBuilder.class, new Class[]{String[].class}), new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    if (param.args[0] != null) {
                        String[] strArr = (String[]) param.args[0];
                        String str = "";
                        for (String str2 : strArr) {
                            str = new StringBuilder(String.valueOf(str)).append(str2).append(":").toString();
                            if (str2 == "/proc/cpuinfo") {
                                strArr[1] = "/data/misc/sys/cpuinfo";
                            }
                            if (str2 == "/proc/version") {
                                strArr[1] = "/data/misc/sys/version";
                            }
                        }
                        param.args[0] = strArr;
                    }
                }

            });
        } catch (Exception e) {
            XposedBridge.log("Fake CPUFile - 2 ERROR: " + e.getMessage());
        }

        try {
            //Pattern.compile("").matcher("");

            XposedHelpers.findAndHookMethod("java.util.regex.Pattern", loadPkgParam.classLoader, "matcher", CharSequence.class, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.beforeHookedMethod(param);
                    if (param.args.length == 1) {
                        if (param.args[0].equals("/proc/cpuinfo")) {
                            param.args[0] = "/data/misc/sys/cpuinfo";
                        }
                        if (param.args[0].equals("/proc/version")) {
                            param.args[0] = "/data/misc/sys/version";
                        }
                    }
                }

            });

        } catch (Exception e) {
            XposedBridge.log("Fake CPU(Pattern) ERROR: " + e.getMessage());
        }
    }


    public void FakeBluetooth(LoadPackageParam loadPkgParam) {
        try {
            XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothAdapter", loadPkgParam.classLoader, "getAddress", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.afterHookedMethod(param);
                    param.setResult(FakeConfigurationConstants.WIFI_MAC);
                }

            });
            XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothDevice", loadPkgParam.classLoader, "getAddress", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    //super.afterHookedMethod(param);
                    param.setResult(FakeConfigurationConstants.WIFI_MAC);
                }

            });
        } catch (Exception e) {
            XposedBridge.log("Fake Bluetooth ERROR: " + e.getMessage());
        }
    }

    public void FakeWifi(LoadPackageParam loadPkgParam) {
        try {
            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", loadPkgParam.classLoader, "getMacAddress", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.afterHookedMethod(param);
                    param.setResult(FakeConfigurationConstants.WIFI_MAC);
                }

            });
            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", loadPkgParam.classLoader, "getSSID", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.afterHookedMethod(param);
                    param.setResult(FakeConfigurationConstants.WIFI_NAME);
                }

            });
            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", loadPkgParam.classLoader, "getBSSID", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.afterHookedMethod(param);
                    param.setResult(FakeConfigurationConstants.BSSID);
                }

            });
        } catch (Exception e) {
            XposedBridge.log("Fake Wifi ERROR: " + e.getMessage());
        }
    }

    public void FakeTelephony(LoadPackageParam loadPkgParam) {
        String TelePhone = "android.telephony.TelephonyManager";
        HookTelephony(TelePhone, loadPkgParam, "getDeviceId", FakeConfigurationConstants.IMEI);
        //HookTelephony(TelePhone, loadPkgParam, "getSubscriberId", FakeConfigurationConstants.IMS);
        HookTelephony(TelePhone, loadPkgParam, "getLine1Number", FakeConfigurationConstants.PHONE_NUMBER);
        HookTelephony(TelePhone, loadPkgParam, "getSimSerialNumber", FakeConfigurationConstants.SIM_SERIAL);
        HookTelephony(TelePhone, loadPkgParam, "getNetworkOperator", FakeConfigurationConstants.CARRIER);
        HookTelephony(TelePhone, loadPkgParam, "getNetworkOperatorName", FakeConfigurationConstants.CARRIER);
        HookTelephony(TelePhone, loadPkgParam, "getSimOperator", FakeConfigurationConstants.CARRIER_CODE);
        HookTelephony(TelePhone, loadPkgParam, "getSimOperatorName", FakeConfigurationConstants.CARRIER);
        HookTelephony(TelePhone, loadPkgParam, "getNetworkCountryIso", FakeConfigurationConstants.COUNTRY_CODE);
        HookTelephony(TelePhone, loadPkgParam, "getSimCountryIso", FakeConfigurationConstants.COUNTRY_CODE);
        ;
        //HookTelephony(TelePhone, loadPkgParam, "getDeviceId", SharedPref.getXValue("IMEI"));
        try {
            XposedHelpers.findAndHookMethod(System.class, "getProperty", String.class, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.afterHookedMethod(param);
                    if (param.args[0] == "os.version") {
                        param.setResult(FakeConfigurationConstants.OS_VERSION);
                    }
                    if (param.args[0] == "os.arch") {
                        param.setResult(FakeConfigurationConstants.OS_ARCH);
                    }
                    if (param.args[0] == "os.name") {
                        param.setResult(FakeConfigurationConstants.OS_NAME);
                    }
                }

            });
        } catch (Exception e) {
            XposedBridge.log("Fake OS ERROR: " + e.getMessage());
        }
    }

    private void HookTelephony(String hookClass, LoadPackageParam loadPkgParam, String funcName, final String value) {
        try {
            XposedHelpers.findAndHookMethod(hookClass, loadPkgParam.classLoader, funcName, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    // TODO Auto-generated method stub
                    super.afterHookedMethod(param);
                    param.setResult(value);
                }

            });
        } catch (Exception e) {
            XposedBridge.log("Fake " + funcName + " ERROR: " + e.getMessage());
        }
    }
}
