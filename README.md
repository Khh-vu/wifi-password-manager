# WiFi Password Manager

An Android application for managing saved WiFi network passwords using privileged system access through [Shizuku](https://shizuku.rikka.app/) or root access.

## Key Features

- **View Saved Networks**: Display all configured WiFi networks with their passwords
- **Search & Copy**: Find networks with real-time search and securely copy passwords
- **Network Notes**: Add custom notes to WiFi networks
- **Import/Export**: Export/import WiFi configurations to/from JSON files
- **Forget All Networks**: Remove all saved WiFi networks at once
- **Auto-Persist Ephemeral Networks**: Automatically save temporary WiFi networks to make them permanent
- **Quick Settings Tile**: Toggle auto-persist feature directly from Quick Settings panel
- **Material Design 3**: Modern UI with dynamic theming and dark/light mode support
- **Privileged Access**: Uses Shizuku or root access for system-level WiFi management

## Screenshots

<img src="fastlane/android/metadata/en-US/images/phoneScreenshots/1.png" width="250" alt="List screen" /><img src="fastlane/android/metadata/en-US/images/phoneScreenshots/2.png" width="250" alt="Search screen" /><img src="fastlane/android/metadata/en-US/images/phoneScreenshots/3.png" width="250" alt="Settings screen" />

## Requirements

- **Android 11 (API 30) or higher**
- **For system-level WiFi management**: [Shizuku](https://shizuku.rikka.app/) or root access

## Technical Stack

### Architecture

- **MVVM Pattern**: Clean separation of concerns
- **Jetpack Compose**: Modern declarative UI framework
- **Kotlin Coroutines**: Asynchronous programming
- **Koin**: Dependency injection

### Key Libraries

- **Jetpack Compose**: UI framework
- **Material 3**: Design system
- **Navigation3**: App navigation
- **WorkManager**: Background task scheduling
- **DataStore**: Settings persistence
- **Kotlinx Serialization**: JSON handling
- **FileKit**: File operations
- **Shizuku**: Privileged API access
- **libsu**: Root access support
- **HiddenApiBypass**: Bypasses hidden API restrictions

## Security & Privacy

- **App Lock**: Protect access with biometric authentication or device credentials
- **Secure Screen**: Prevents screenshots and screen recording when enabled
- **Local Storage**: All data remains on your device
- **No Network Access**: App doesn't connect to the internet
- **Sensitive Data Protection**: Clipboard operations marked as sensitive

## Verification

To verify the authenticity of downloaded APK files, you can check the SHA-256 certificate fingerprint using [apksigner](https://developer.android.com/tools/apksigner#usage-verify):

```
34:FF:A2:EE:65:8F:E0:36:B6:C5:D2:92:AE:B2:51:45:DE:69:57:ED:52:ED:D9:9F:4B:EB:8D:6B:36:31:C1:FF
```

## Acknowledgments

- **Shizuku Team**: For providing the framework for privileged API access
- **Android Open Source Project**: For the underlying WiFi management APIs

## Disclaimer

Use at your own risk. I'm not responsible for any misuse or damage caused by this application.
