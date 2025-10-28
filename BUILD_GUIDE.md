# SMS Gateway - Build Guide & Development Setup

## 🚀 Quick Start

### Prerequisites

**Development Environment:**
- Android Studio Arctic Fox or newer
- Android SDK API 23+
- Kotlin 1.9+
- Java 11+

**Hardware Requirements:**
- Physical Android device (recommended for SMS testing)
- Minimum 4GB RAM
- 2GB free disk space

---

## 📦 Build Commands

### Basic Build Operations

```bash
# Clean project
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug

# Install release APK to connected device
./gradlew installRelease
```

### Testing Commands

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.example.smsbramkax1.ExampleUnitTest"

# Run specific test method
./gradlew test --tests "com.example.smsbramkax1.ExampleUnitTest.addition_isCorrect"

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run instrumented tests with coverage
./gradlew connectedDebugAndroidTest
```

### Code Quality Commands

```bash
# Run lint checks
./gradlew lint

# Run lint and generate report
./gradlew lintDebug

# Run detekt (if configured)
./gradlew detekt

# Generate test coverage report
./gradlew jacocoTestReport
```

### Advanced Build Commands

```bash
# Build with specific flavor
./gradlew assembleDebugFlavor

# Build and run tests
./gradlew build

# Generate dependency tree
./gradlew dependencies

# Analyze dependencies
./gradlew dependencyInsight --dependency <dependency-name>

# Clean and rebuild
./gradlew clean build
```

---

## 🛠️ Development Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd smsbramkax1
```

### 2. Open in Android Studio

1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the project directory
4. Wait for Gradle sync to complete

### 3. Configure SDK

```bash
# Set ANDROID_HOME environment variable
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### 4. Virtual Device Setup (Optional)

```bash
# List available AVDs
emulator -list-avds

# Create new AVD
avdmanager create avd -n sms_gateway_test -k "system-images;android-30;google_apis;x86_64"

# Launch emulator
emulator -avd sms_gateway_test
```

---

## 📋 Project Structure

### Gradle Configuration

**Root `build.gradle.kts`:**
```kotlin
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
}
```

**App `build.gradle.kts`:**
```kotlin
android {
    namespace = "com.example.smsbramkax1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smsbramkax1"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.0")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.4")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.44")
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("com.google.dagger:hilt-compiler:2.44")
    kapt("androidx.hilt:hilt-compiler:1.1.0")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.6.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

---

## 🧪 Testing Strategy

### Unit Tests

**Location:** `src/test/java/com/example/smsbramkax1/`

**Example Test Structure:**
```kotlin
@RunWith(MockitoJUnitRunner::class)
class SmsManagerTest {
    
    @Mock
    private lateinit var context: Context
    
    @Mock
    private lateinit var smsMessageDao: SmsMessageDao
    
    private lateinit var smsManager: SmsManager
    
    @Before
    fun setup() {
        smsManager = SmsManager(context, smsMessageDao)
    }
    
    @Test
    fun `sendSms with valid number returns success`() {
        // Given
        val phoneNumber = "+1234567890"
        val message = "Test message"
        
        // When
        val result = smsManager.sendSms(phoneNumber, message)
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `sendSms with invalid number returns failure`() {
        // Given
        val phoneNumber = "invalid"
        val message = "Test message"
        
        // When
        val result = smsManager.sendSms(phoneNumber, message)
        
        // Then
        assertTrue(result.isFailure)
    }
}
```

### Instrumented Tests

**Location:** `src/androidTest/java/com/example/smsbramkax1/`

**Example Test Structure:**
```kotlin
@RunWith(AndroidJUnit4::class)
class SmsDaoTest {
    
    private lateinit var database: SmsDatabase
    private lateinit var smsMessageDao: SmsMessageDao
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SmsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        smsMessageDao = database.smsMessageDao()
    }
    
    @After
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun insertAndGetSmsMessage() = runTest {
        // Given
        val smsMessage = SmsMessage(
            phoneNumber = "+1234567890",
            messageBody = "Test message",
            status = "PENDING"
        )
        
        // When
        val id = smsMessageDao.insert(smsMessage)
        val retrieved = smsMessageDao.getById(id)
        
        // Then
        assertNotNull(retrieved)
        assertEquals("+1234567890", retrieved?.phoneNumber)
        assertEquals("Test message", retrieved?.messageBody)
        assertEquals("PENDING", retrieved?.status)
    }
}
```

### UI Tests

**Example Compose Test:**
```kotlin
@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun dashboardScreen_displaysStatistics() {
        composeTestRule.setContent {
            DashboardScreen(navController = rememberNavController())
        }
        
        // Verify statistics cards are displayed
        composeTestRule.onNodeWithText("Queue Count").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daily Sent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error Count").assertIsDisplayed()
        composeTestRule.onNodeWithText("System Status").assertIsDisplayed()
    }
}
```

---

## 🔧 Development Tools

### Code Style & Formatting

**Kotlin Code Style:**
```bash
# Format code
./gradlew ktlintFormat

# Check code style
./gradlew ktlintCheck
```

**Detekt Static Analysis:**
```bash
# Run detekt
./gradlew detekt

# Run detekt with baseline
./gradlew detektBaseline
```

### Debugging Tools

**Database Inspector:**
1. Open Android Studio
2. View → Tool Windows → Database Inspector
3. Select running app
4. Browse database tables

**WorkManager Inspector:**
1. View → Tool Windows → App Inspection
2. Select WorkManager tab
3. Monitor worker status

**Network Inspector:**
1. View → Tool Windows → App Inspection
2. Select Network tab
3. Monitor API calls

---

## 📱 Device Setup

### Physical Device Configuration

1. **Enable Developer Options:**
   - Settings → About phone
   - Tap "Build number" 7 times
   - Go back to Settings → Developer options

2. **Enable USB Debugging:**
   - Developer options → USB debugging
   - Enable "Stay awake"
   - Enable "USB debugging"

3. **Install App:**
   ```bash
   ./gradlew installDebug
   ```

4. **Grant Permissions:**
   - SMS permissions (SEND, RECEIVE, READ)
   - Contacts permission (READ_CONTACTS)
   - Storage permission (optional)
   - Battery optimization (disable for app)

### Emulator Setup

1. **Create AVD:**
   ```bash
   avdmanager create avd -n sms_gateway -k "system-images;android-30;google_apis;x86_64"
   ```

2. **Launch Emulator:**
   ```bash
   emulator -avd sms_gateway
   ```

3. **Install App:**
   ```bash
   ./gradlew installDebug
   ```

---

## 🚀 Release Process

### 1. Prepare Release

```bash
# Clean project
./gradlew clean

# Run tests
./gradlew test connectedAndroidTest

# Run lint
./gradlew lint

# Build release APK
./gradlew assembleRelease
```

### 2. Sign APK

**Generate Signing Key:**
```bash
keytool -genkey -v -keystore sms-gateway-release.keystore -alias sms-gateway -keyalg RSA -keysize 2048 -validity 10000
```

**Configure Signing in `build.gradle.kts`:**
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../sms-gateway-release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 3. Generate Signed APK

```bash
./gradlew assembleRelease
```

### 4. Upload to App Store

1. **Google Play Console:**
   - Create new release
   - Upload signed APK
   - Fill release notes
   - Submit for review

---

## 🔍 Troubleshooting

### Common Build Issues

**Gradle Sync Issues:**
```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies

# Clear Gradle cache
rm -rf ~/.gradle/caches/
./gradlew build --refresh-dependencies
```

**Dependency Conflicts:**
```bash
# Check dependency tree
./gradlew dependencies

# Analyze specific dependency
./gradlew dependencyInsight --dependency <dependency-name>
```

**Compilation Errors:**
```bash
# Clean build
./gradlew clean build

# Check Kotlin compiler version
./gradlew properties | grep kotlin
```

### Runtime Issues

**Permission Issues:**
- Check AndroidManifest.xml permissions
- Grant runtime permissions in app settings
- Check permission request flow

**Database Issues:**
- Check database version and migrations
- Use Database Inspector for debugging
- Check Room annotations

**Background Processing Issues:**
- Check WorkManager constraints
- Monitor worker status in WorkManager Inspector
- Check foreground service permissions

### Performance Issues

**Memory Leaks:**
- Use Memory Profiler
- Check for leaked contexts
- Review coroutine usage

**UI Performance:**
- Use Layout Inspector
- Check Compose recomposition
- Optimize lazy loading

---

## 📊 Build Variants

### Debug Variant

**Purpose:** Development and testing  
**Features:**
- Debug logging enabled
- Debuggable APK
- Application ID suffix: `.debug`
- No code obfuscation

### Release Variant

**Purpose:** Production deployment  
**Features:**
- Code obfuscation with ProGuard
- Optimized bytecode
- Signed APK
- No debug logging

### Custom Variants (Optional)

```kotlin
android {
    flavorDimensions += "version"
    
    productFlavors {
        create("demo") {
            dimension = "version"
            applicationIdSuffix = ".demo"
        }
        create("full") {
            dimension = "version"
        }
    }
}
```

---

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
        
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Run tests
      run: ./gradlew test
      
    - name: Run lint
      run: ./gradlew lint
      
    - name: Build debug APK
      run: ./gradlew assembleDebug
      
    - name: Upload build artifacts
      uses: actions/upload-artifact@v3
      with:
        name: build-artifacts
        path: app/build/outputs/apk/debug/
```

---

## 📚 Development Guidelines

### Code Style

**Kotlin Conventions:**
- Use camelCase for variables and functions
- Use PascalCase for classes and interfaces
- Use UPPER_SNAKE_CASE for constants
- Follow Android Kotlin style guide

**Compose Conventions:**
- Use `@Composable` functions for UI components
- Use `remember` for state management
- Use `@Preview` for component previews
- Follow Material 3 design guidelines

### Git Workflow

**Branch Strategy:**
- `main`: Production code
- `develop`: Integration branch
- `feature/*`: Feature branches
- `hotfix/*`: Critical fixes

**Commit Messages:**
- Use conventional commits
- Format: `type(scope): description`
- Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

### Testing Requirements

**Coverage Requirements:**
- Unit tests: >80% coverage
- Integration tests: Core workflows
- UI tests: Critical user flows

**Test Organization:**
- Unit tests in `src/test/`
- Integration tests in `src/androidTest/`
- UI tests for Compose components

---

**Status:** Build guide provides comprehensive development setup, testing strategies, and deployment procedures for production-ready SMS Gateway application.