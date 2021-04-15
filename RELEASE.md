## Update cloud apk

1. Update sdk version in the `build.gradle` file.
1. Update cloud.apk version in `string.xml` and `build.gradle` of cloud project
2. Run "assembleDebug"
3. Put the cloud.apk in `mobile-iq-builds` folder.
4. Update the emulators

## Build Release SDK

1. Run "generateRelease" from Tasks/other.
2. Go to release folder, copy content of new version version number (ex. 1.0.3) to mobile-iq-builds/android-sdk-release
3. Delete .md5, .sha files and all .sources files
4. Copy sources folder and javadoc.jar file from mobile-iq-builds/android-sdk-release to release version.
5. Copy sdk-prod-release.aar from build `aar` folder to release version folder as sdk-1.0.3.aar
6. Copy mapping folder from output folder to current release folder
7. Inside the release version folder zip "com" folder with zip -r com.zip com -x "*.DS_Store"
8. Release to maven
9. Update SDK in android-sdk

## Update UserIQ React Native library

1. Change the useriq dependency version in the `build.gradle` file.
2. Update the library version in `package.json`.
3. Push to npm.

## Update Xamarin module

1. Update the `.aar` file in the Xamarin SDK module.
2. Build the .dll file.
3. Push to the useriq-xamarin-sdk

## Update the SDK version in UserIQ dashboard
1. Update sdk version in `AndroidIntegration.tsx` file.

## Miscellaneous
1. Update CHANGELOG.md in android-sdk repository.
2. Update README.md (if required) in android-sdk repository.

