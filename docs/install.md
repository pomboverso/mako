# Commands

## Building

### Debug build
```
./gradlew assembleDebug
```

### Release APK (unsigned)
```
./gradlew assembleRelease
```

### Signed release APK (after configuring signing in build.gradle)
```
./gradlew bundleRelease
```

## Installing / Running

### Install debug APK directly to connected device
```
./gradlew installDebug
```

### Or manually with adb
```
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Launch the app after install
```
adb shell am start -n com.yourpackage/.MainActivity
```

## Gradle

### Sync dependencies (equivalent to "Sync Project" in Android Studio)
```
./gradlew dependencies
```

### Clean build cache
```
./gradlew clean
```

### See all available tasks
```
./gradlew tasks
```

## ADB useful ones

### See connected devices
```
adb devices
```

### Watch logcat (filtered to your app)
```
adb logcat --pid=$(adb shell pidof -s com.rama.mako)
```

### Uninstall
```
adb uninstall com.rama.mako
```
