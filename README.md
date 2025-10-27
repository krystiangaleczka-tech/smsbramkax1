# SMS Gateway Android App

A comprehensive Android application for SMS gateway functionality with queue management, real-time monitoring, and advanced features.

## 📱 Features

### Core Functionality
- **SMS Queue Management**: Send, receive, and manage SMS messages with priority-based queuing
- **Real-time Monitoring**: Live dashboard with SMS status tracking and statistics
- **Background Processing**: WorkManager-based workers for reliable SMS processing
- **Data Persistence**: Encrypted local storage with Room database

### Advanced Features
- **Settings & Configuration**: API key management, server URL configuration, notification preferences
- **Export/Import**: CSV export for SMS data, JSON export for logs, bulk SMS import
- **Permission Management**: Runtime permission handling with user-friendly dialogs
- **Error Handling**: Comprehensive error management with retry mechanisms
- **Diagnostics**: System health monitoring and troubleshooting tools

### User Interface
- **Material 3 Design**: Modern, responsive UI following Android design guidelines
- **Multi-device Support**: Optimized for phones, tablets, and desktop layouts
- **Dark/Light Theme**: Automatic theme switching based on system preferences

## 🚀 Installation

### Prerequisites
- Android 6.0 (API level 23) or higher
- Kotlin 1.9+
- Android Studio Arctic Fox or later

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd smsbramkax1
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

### Required Permissions

The app requires the following permissions:

| Permission | Purpose | Required |
|------------|---------|----------|
| `SEND_SMS` | Send SMS messages | ✅ |
| `RECEIVE_SMS` | Receive SMS messages | ✅ |
| `READ_SMS` | Read SMS messages | ✅ |
| `INTERNET` | Network communication | ✅ |
| `ACCESS_NETWORK_STATE` | Check network status | ✅ |
| `FOREGROUND_SERVICE` | Background processing | ✅ |
| `POST_NOTIFICATIONS` | Show notifications | ✅ (Android 13+) |
| `RECEIVE_BOOT_COMPLETED` | Auto-start on boot | ✅ |
| `WRITE_EXTERNAL_STORAGE` | Export data | ✅ |

## 📖 User Manual

### Getting Started

1. **First Launch**
   - Grant required SMS permissions when prompted
   - Allow notifications for status updates
   - Disable battery optimization for reliable background operation

2. **Dashboard**
   - View real-time SMS statistics
   - Monitor pending, sent, and failed messages
   - Access quick actions for common tasks

3. **Sending SMS**
   - Use "Wyślij testowy SMS" for quick testing
   - Messages are queued and processed automatically
   - Failed messages can be retried manually

### Settings Configuration

#### API Configuration
- **API Key**: Enter your gateway API key
- **Server URL**: Configure the backend server URL
- **Device ID**: Automatically generated unique identifier

#### Notification Settings
- Enable/disable notifications
- Configure notification channels
- Access system notification settings

#### Auto-Retry Settings
- Enable automatic retry for failed messages
- Set maximum retry attempts
- Configure retry intervals

#### Data Management
- **Export SMS**: Export message history to CSV
- **Export Logs**: Export system logs to JSON
- **Clear Data**: Remove all stored data (destructive action)

### Troubleshooting

#### Common Issues

**SMS Not Sending**
1. Check SMS permissions in Settings → Apps → SMS Gateway → Permissions
2. Verify network connectivity
3. Check if airplane mode is enabled
4. Ensure sufficient signal strength

**Background Issues**
1. Disable battery optimization in Settings → Battery → App optimization
2. Verify background app refresh is enabled
3. Check if app is in "Doze" mode

**Export Problems**
1. Ensure storage permissions are granted
2. Check available storage space
3. Verify file system permissions

#### Diagnostics Screen

Access the diagnostics screen to:
- View system health status
- Check database integrity
- Monitor worker status
- Review recent error logs

## 🏗️ Architecture Overview

### Project Structure

```
app/src/main/java/com/example/smsbramkax1/
├── data/              # Data models
├── dto/               # Data Transfer Objects
├── network/           # Network layer
├── receivers/         # Broadcast receivers
├── services/          # Foreground services
├── sms/               # SMS management
├── storage/           # Database layer
├── ui/                # User interface
│   ├── components/    # Reusable UI components
│   ├── screens/       # Screen implementations
│   ├── theme/         # Theme and styling
│   └── utils/         # UI utilities
├── utils/             # Utility classes
└── workers/           # Background workers
```

### Key Components

#### Data Layer
- **Room Database**: Local SQLite database with encrypted storage
- **Entities**: `SmsQueue`, `SystemLog`
- **DAOs**: Data access objects for database operations

#### Business Logic
- **SmsManager**: SMS sending and receiving
- **NetworkManager**: API communication
- **PermissionsManager**: Runtime permission handling
- **ExportManager**: Data export/import functionality

#### UI Layer
- **Jetpack Compose**: Modern declarative UI framework
- **Material 3**: Design system components
- **Navigation**: Single-activity architecture with Compose navigation

#### Background Processing
- **WorkManager**: Reliable background task scheduling
- **Foreground Service**: Persistent background operation
- **Broadcast Receivers**: System event handling

### Data Flow

1. **SMS Reception**
   ```
   SMS Received → SmsManager → Database → Queue → Worker → API → Notification
   ```

2. **SMS Sending**
   ```
   User Action → Queue → Worker → SmsManager → Network → Status Update
   ```

3. **Background Processing**
   ```
   WorkManager → Periodic Tasks → Database Operations → Status Updates
   ```

## 🔧 API Documentation

### SMS Queue API

#### Send SMS
```kotlin
val smsQueue = SmsQueue(
    phoneNumber = "+48123456789",
    message = "Test message",
    status = SmsStatus.PENDING,
    priority = 5,
    createdAt = System.currentTimeMillis()
)
val id = database.smsQueueDao().insertSms(smsQueue)
```

#### Get SMS Status
```kotlin
val sms = database.smsQueueDao().getSmsById(id)
val status = sms?.status
```

#### Update Status
```kotlin
database.smsQueueDao().updateSmsStatus(
    id, 
    SmsStatus.SENT, 
    System.currentTimeMillis()
)
```

### Export Manager API

#### Export SMS to CSV
```kotlin
exportManager.exportSmsToCsv()
    .onSuccess { filePath -> 
        // Handle success
    }
    .onFailure { error ->
        // Handle error
    }
```

#### Export Logs to JSON
```kotlin
exportManager.exportLogsToJson()
    .onSuccess { filePath -> 
        // Handle success
    }
    .onFailure { error ->
        // Handle error
    }
```

### Permissions Manager API

#### Check Permissions
```kotlin
val hasPermissions = permissionsManager.hasAllRequiredPermissions()
val hasSmsPermissions = permissionsManager.hasSmsPermissions()
```

#### Request Permissions
```kotlin
// In Composable
PermissionsManagerComposable(
    permissionsManager = permissionsManager,
    onPermissionsGranted = { /* Handle granted */ },
    onPermissionsDenied = { /* Handle denied */ }
)
```

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Lint Check
```bash
./gradlew lint
```

### Build Variants
- **Debug**: Development build with debugging enabled
- **Release**: Production build with optimizations

## 📊 Performance Considerations

### Battery Optimization
- Minimal background processing
- Efficient WorkManager scheduling
- Smart retry mechanisms
- Battery optimization exclusion recommended

### Memory Management
- Room database with proper indexing
- Lazy loading for large datasets
- Efficient Compose recomposition
- Memory leak prevention

### Network Usage
- Batch API requests
- Offline-first architecture
- Smart retry with exponential backoff
- Minimal data transfer

## 🔒 Security

### Data Protection
- EncryptedSharedPreferences for sensitive data
- Room database encryption
- Secure API key storage
- No sensitive data in logs

### Permissions
- Minimal required permissions
- Runtime permission requests
- Permission rationale dialogs
- Graceful degradation without permissions

## 🐛 Troubleshooting

### Build Issues
- Ensure Android SDK is up to date
- Check Kotlin version compatibility
- Clear Gradle cache: `./gradlew clean`
- Rebuild project: `./gradlew build`

### Runtime Issues
- Check logcat with: `adb logcat -s SmsGateway`
- Verify permissions in device settings
- Test on physical device for SMS functionality
- Check network connectivity

### Performance Issues
- Monitor memory usage with Android Profiler
- Check battery usage in device settings
- Review database query performance
- Optimize background task scheduling

### Export Issues
- Export files are saved to app's external files directory
- Files are accessible via FileProvider for sharing
- Check storage permissions if export fails
- Verify FileProvider configuration in AndroidManifest.xml

## 📝 Changelog

### Phase 6 - Complete ✅
- Settings Screen with full configuration options
- Enhanced Permissions Manager with runtime handling
- Comprehensive Error Handling with retry mechanisms
- Export/Import Manager with CSV and JSON support
- Full testing and bug fixes
- Complete documentation

### Phase 5 - Complete ✅
- Real-time monitoring system
- Notification framework
- Diagnostics capabilities
- UI improvements
- Auto-refresh system

### Phase 4 - Complete ✅
- WorkManager integration
- Background services
- System event handling
- Build configuration fixes

### Phase 3 - Complete ✅
- Complete UI/UX implementation
- Material 3 design system
- Responsive layouts
- Navigation system

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the diagnostics screen in-app

---

**SMS Gateway Android App** - Built with ❤️ using Kotlin and Jetpack Compose