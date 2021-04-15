## Xposed Module (cloud)

### Introduction
Cloud xposed module is required to inject the SDK in the new apps and, hooking android and react-native methods.

### Method/Field hooking for Android
1. Disabling the internal SDK.
2. Initializing the SDK in "onCreate" of Activity.
3. Auto accepting permissions from dialog.
4. Auto dismiss of Anr dialog.
5. Bypassing UserIQ's showHelpCentre and showCtxHelp api, making the call from xposed. 
6. Handling UnhandledKeyDispatchEvent for onPaste on Emulator from dashboard. 
7. Carsh logging for UncaughtException.


### Method/Field hooking for React Native
1. Bypassing the setUser from useriq-react-native.
2. Setting up the android resourceId(from R.java) for the "react_test_id" in the "Utils" class of SDK.
3. React native's Lifecycle events, i.e "onHostPause" and "onHostResume".
4. React events(touch events) dispatched by the React-native EventDispatcher.

