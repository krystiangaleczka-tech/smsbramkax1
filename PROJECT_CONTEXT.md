# SMS Gateway Android App - Project Context

## 📋 Overview

SMS Gateway for beauty salons - production-ready Android application for SMS management.

**Package:** `com.example.smsbramkax1`  
**Platform:** Android (API 23+)  
**Tech Stack:** Kotlin, Jetpack Compose, Room, WorkManager, Coroutines  
**Status:** Production Ready ✅

---

## 🏗️ Quick Architecture

**3-Layer Architecture:**
- **Presentation:** Jetpack Compose UI with Material 3
- **Business Logic:** SMS managers, template engine, contact management  
- **Data Processing:** Room database, WorkManager, encrypted storage

**Key Components:**
- `MainActivity.kt` - Entry point & permissions
- `MainApp.kt` - Navigation & UI orchestration
- `SmsManager.kt` - Core SMS functionality
- `BulkSmsManager.kt` - Campaign processing
- `ScheduledSmsManager.kt` - Time-based messaging
- `TemplateManager.kt` - Template management

---

## 📱 Core Features

### SMS Management
- **Individual SMS** with real-time status tracking
- **Bulk Campaigns** with progress monitoring
- **Scheduled SMS** with template engine
- **Template System** with variable substitution
- **Contact Management** with device sync
- **Health Monitoring** with diagnostics

### Business Value
- Beauty salon focused templates
- Appointment management integration
- Customer communication automation
- Marketing campaign support

---

## 🗄️ Data Model

**Unified SMS Table (Version 3):**
```kotlin
@Entity(tableName = "sms_messages")
data class SmsMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val messageBody: String,
    val status: String, // PENDING, QUEUED, SENT, DELIVERED, FAILED, SCHEDULED
    val isScheduled: Boolean = false,    // Key field - unified table
    val scheduledFor: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    // ... other fields
)
```

**Status Flow:** PENDING → QUEUED → SCHEDULED → SENDING → SENT/DELIVERED/FAILED

---

## 📊 Documentation Structure

### 📁 Detailed Documentation Files

| File | Purpose | Size |
|------|---------|------|
| [`ARCHITECTURE.md`](./ARCHITECTURE.md) | System architecture & components | ~8K |
| [`DATA_MODEL.md`](./DATA_MODEL.md) | Database schema & entities | ~6K |
| [`UI_SCREENS.md`](./UI_SCREENS.md) | Screen details & navigation | ~10K |
| [`BACKGROUND_PROCESSING.md`](./BACKGROUND_PROCESSING.md) | Workers & background tasks | ~5K |
| [`BUILD_GUIDE.md`](./BUILD_GUIDE.md) | Build commands & setup | ~3K |

### 🔍 Quick Access by Topic

**For UI Development:** → [`UI_SCREENS.md`](./UI_SCREENS.md)
- Dashboard, History, Bulk SMS, Scheduled, Templates screens
- Component details and navigation patterns

**For Backend Development:** → [`ARCHITECTURE.md`](./ARCHITECTURE.md)
- Manager classes, business logic, utility classes
- SMS processing pipeline and template system

**For Database Work:** → [`DATA_MODEL.md`](./DATA_MODEL.md)
- Room entities, DAOs, queries, migrations
- Unified table implementation details

**For Background Tasks:** → [`BACKGROUND_PROCESSING.md`](./BACKGROUND_PROCESSING.md)
- WorkManager workers, services, receivers
- Processing flows and error handling

**For Build/Deploy:** → [`BUILD_GUIDE.md`](./BUILD_GUIDE.md)
- Gradle commands, testing, deployment
- Development setup and guidelines

---

## 🚀 Quick Start

### Build Commands
```bash
./gradlew build              # Build project
./gradlew installDebug       # Install debug APK
./gradlew test              # Run tests
./gradlew lint              # Lint check
```

### Key Files for Development
- `SmsMessage.kt` - Unified SMS entity
- `SmsMessageDao.kt` - Main data access
- `SmsManager.kt` - Core SMS logic
- `DashboardScreen.kt` - UI example

### Development Guidelines
- **Language:** Kotlin + Jetpack Compose
- **Architecture:** Clean Architecture with MVVM
- **Async:** Coroutines with Flow
- **UI:** Material 3 design system
- **Testing:** JUnit + Compose testing

---

## 📈 Current Status

**✅ Completed Features:**
- Unified SMS table (v3) - solved data consistency
- Complete UI with Material 3
- Background processing with WorkManager
- Template engine with variables
- Comprehensive error handling
- Health monitoring system

**🔧 Technical Quality:**
- Clean architecture with proper separation
- Modern Android development practices
- Production-ready security measures
- Efficient database queries
- Responsive UI design

**📊 Project Metrics:**
- Build: ✅ Working
- Database: Version 3 (unified table)
- Testing: Basic unit tests
- Git: All changes committed

---

## 🎯 Next Steps

**For New Developers:**
1. Read this overview first
2. Check specific topic files above
3. Review `DashboardScreen.kt` for UI patterns
4. Study `SmsManager.kt` for business logic

**For Feature Development:**
- UI changes → [`UI_SCREENS.md`](./UI_SCREENS.md)
- Business logic → [`ARCHITECTURE.md`](./ARCHITECTURE.md)
- Database changes → [`DATA_MODEL.md`](./DATA_MODEL.md)
- Background tasks → [`BACKGROUND_PROCESSING.md`](./BACKGROUND_PROCESSING.md)

---

**Status:** Production ready with comprehensive modular documentation. Use topic-specific files for detailed information.