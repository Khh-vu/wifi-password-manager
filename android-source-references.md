# Android WiFi Source Code References

This document contains references to Android WiFi-related source code across different Android API levels (30-36), organized by component type.

## Overview

Android WiFi framework reference guide covering API levels 30-36. Starting from Android 12, WiFi components moved to a separate module.

---

## IWifiManager (API 30-36)

AIDL interface for WiFi management operations used by applications to interact with the WiFi subsystem.

### [Android 11 (API 30)](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android11-release/wifi/java/android/net/wifi/IWifiManager.aidl)

### [Android 12 (API 31)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android12-release/framework/java/android/net/wifi/IWifiManager.aidl)

### [Android 12L (API 32)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android12L-release/framework/java/android/net/wifi/IWifiManager.aidl)

### [Android 13 (API 33)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android13-release/framework/java/android/net/wifi/IWifiManager.aidl)

### [Android 14 (API 34)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android14-release/framework/java/android/net/wifi/IWifiManager.aidl)

### [Android 15 (API 35)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android15-release/framework/java/android/net/wifi/IWifiManager.aidl)

### [Android 16 (API 36)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android16-release/framework/java/android/net/wifi/IWifiManager.aidl)

---

## WifiConfiguration (API 30-36)

Data class representing WiFi network configuration including SSID, security settings, and credentials.

### [Android 11 (API 30)](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android11-release/wifi/java/android/net/wifi/WifiConfiguration.java)

### [Android 12 (API 31)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android12-release/framework/java/android/net/wifi/WifiConfiguration.java)

### [Android 12L (API 32)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android12L-release/framework/java/android/net/wifi/WifiConfiguration.java)

### [Android 13 (API 33)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android13-release/framework/java/android/net/wifi/WifiConfiguration.java)

### [Android 14 (API 34)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android14-release/framework/java/android/net/wifi/WifiConfiguration.java)

### [Android 15 (API 35)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android15-release/framework/java/android/net/wifi/WifiConfiguration.java)

### [Android 16 (API 36)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android16-release/framework/java/android/net/wifi/WifiConfiguration.java)

---

## WifiShellCommand (API 30-36)

Shell command interface for WiFi operations via ADB, used for debugging and system-level management.

### [Android 11 (API 30)](https://android.googlesource.com/platform/frameworks/opt/net/wifi/+/refs/heads/android11-release/service/java/com/android/server/wifi/WifiShellCommand.java)

### [Android 12 (API 31)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android12-release/service/java/com/android/server/wifi/WifiShellCommand.java)

### [Android 12L (API 32)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android12L-release/service/java/com/android/server/wifi/WifiShellCommand.java)

### [Android 13 (API 33)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android13-release/service/java/com/android/server/wifi/WifiShellCommand.java)

### [Android 14 (API 34)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android14-release/service/java/com/android/server/wifi/WifiShellCommand.java)

### [Android 15 (API 35)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android15-release/service/java/com/android/server/wifi/WifiShellCommand.java)

### [Android 16 (API 36)](https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android16-release/service/java/com/android/server/wifi/WifiShellCommand.java)

---

## Notes

- **API Evolution**: Each version may introduce new methods, deprecate existing ones, or modify behavior
- **Compatibility**: When working with WiFi APIs, always check the target API level for compatibility
