# WiFi Password Manager

An Android application for managing saved WiFi network passwords using privileged system access through [Shizuku](https://shizuku.rikka.app/).

## Key Features

- **View Saved Networks**: Display all configured WiFi networks with their passwords
- **Search & Copy**: Find networks with real-time search and securely copy passwords
- **Import/Export**: Export/import WiFi configurations to/from JSON files
- **Forget All Networks**: Remove all saved WiFi networks at once
- **Auto-Persist Ephemeral Networks**: Automatically save temporary WiFi networks to make them permanent
- **Quick Settings Tile**: Toggle auto-persist feature directly from Quick Settings panel
- **Material Design 3**: Modern UI with dynamic theming and dark/light mode support
- **Privileged Access**: Uses Shizuku for system-level WiFi management

## Requirements

- **Android 11 (API 30) or higher**
- **[Shizuku](https://shizuku.rikka.app/)**: Required for privileged system access

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
- **HiddenApiBypass**: Bypasses hidden API restrictions

## Security & Privacy

- **Local Storage**: All data remains on your device
- **No Network Access**: App doesn't connect to the internet
- **Sensitive Data Protection**: Clipboard operations marked as sensitive

## Acknowledgments

- **Shizuku Team**: For providing the framework for privileged API access
- **Android Open Source Project**: For the underlying WiFi management APIs

## Disclaimer

Use at your own risk. I'm not responsible for any misuse or damage caused by this application.