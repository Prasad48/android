# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/smylsamy/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

##-dontshrink
##-dontoptimize
#-optimizationpasses 10
##-dontusemixedcaseclassnames
##-repackageclasses ''
##-flattenpackagehierarchy ''
#-allowaccessmodification
#-verbose
#-renamesourcefileattribute unfold
-keepattribute SourceFile
-keepattribute LineNumberTable
#-keepattribute Exceptions
#-keepattribute InnerClasses
#-keepattribute Signature
##-keepattributes *Annotation*
#-keepattributes EnclosingMethod
-keepparameternames
-keepattributes Exceptions, InnerClasses

-keep public class com.useriq.sdk.UserIQSDK {
    public *;
}

-keep public class com.useriq.sdk.UserIQSDK$* {
    *;
}

# Required for ReactNative
# mSdkDisabled required by Xposed to disable SDK

-keep public class com.useriq.sdk.UserIQSDKInternal {
    private boolean mSdkDisabled;
    public static *** getInstance();
    public void onReactNativePause();
    public void onReactNativeResume();
    public void onReactEvent(java.lang.String,int);
}

-keep public class com.useriq.sdk.SDKService {
    *;
}

-keep public class com.useriq.SimpleRPC {
    *;
}
-keep public class com.useriq.SimpleRPC$* {
    *;
}
-keepclassmembers,allowoptimization enum * {
    public static **[] values(); public static ** valueOf(java.lang.String);
}



