
This is the free and open-source SIP Softphone developed by Viking Electronics and built on Linphone

# What's new

# Building the app

If you have Android Studio, simply open the project, wait for the gradle synchronization and then build/install the app.
It will download the linphone library from our Maven repository as an AAR file so you don't have to build anything yourself.

If you don't have Android Studio, you can build and install the app using gradle:
```
./gradlew assembleDebug
```
will compile the APK file (assembleRelease to instead if you want to build a release package), and then
```
./gradlew installDebug
```
to install the generated APK in the previous step (use installRelease instead if you built a release package).

APK files are stored within ```./app/build/outputs/apk/debug/``` and ```./app/build/outputs/apk/release/``` directories.

## Building the local SDK

1. Clone the linphone-sdk repository from out gitlab:
```
git clone https://gitlab.linphone.org/BC/public/linphone-sdk.git
```

2. Build the SDK.  (Consult the linphone-sdk/README for details and install dependencies.)  In linux, the commands should be something like this:
```
ANDROID_SDK=/path/to/Android/Sdk
ANDROID_NDK=/path/to/Android/Sdk/ndk-bundle
export ANDROID_HOME=$ANDROID_SDK
export PATH="$PATH:$ANDROID_SDK:$ANDROID_NDK"
cd linphone-sdk
mkdir build
cd build
cmake .. -DLINPHONESDK_PLATFORM=Android -DENABLE_NON_FREE_CODECS=ON
```

3. Pull the modified submodules (linphone and mediastreamer2.)  I'm not super confident in my git abilities, so I just use Intellij's git plugin.

4. Build the sdk
```
cmake --build .
```

5. Edit in the linphone-sdk-android folder of this project the symbolic link (debug and/or release) to the generated AAR.
We recommend to at least create the link for the release AAR that can be used for debug APK flavor because it is smaller and will reduce the time required to install the APK.
```
ln -s <path to linphone-sdk>/linphone-sdk/build/linphone-sdk/bin/outputs/aar/linphone-sdk-android-release.aar linphone-sdk-android/linphone-sdk-android-release.aar
ln -s <path to linphone-sdk>/linphone-sdk/build/linphone-sdk/bin/outputs/aar/linphone-sdk-android-debug.aar linphone-sdk-android/linphone-sdk-android-debug.aar
```

5. Rebuild the app in Android Studio.
