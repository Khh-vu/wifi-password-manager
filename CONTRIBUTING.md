# Contributing to WiFi Password Manager

## Testing with root

No rooted device? Create an emulator in Android Studio and root it with [rootAVD](https://gitlab.com/newbit/rootAVD).

## Wifi internals (only for core Wifi or compatibility work)

Android's Wifi code changes between API levels. AOSP sources per release `<N>` (10–17):

- Android 10–11: `https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android<N>-release/wifi/java/android/net/wifi/`
- Android 12+: `https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android<N>-release/framework/java/android/net/wifi/`

Android 12L uses `android12L-release`.

## Bugs and feature requests

Open a [GitHub issue](https://github.com/Khh-vu/wifi-password-manager/issues). For bugs, please include:

- Device or emulator (model, Android version)
- Steps to reproduce
- Expected vs actual behavior
- Logs or screenshots, if available
