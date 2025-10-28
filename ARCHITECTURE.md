# SMS Gateway - Architecture Documentation

## 🏗️ System Architecture Overview

### 3-Layer Clean Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   MainActivity  │  │   MainApp.kt    │  │  UI Screens  │ │
│  │                 │  │                 │  │              │ │
│  │ - Permissions   │  │ - Navigation    │  │ - Dashboard  │ │
│  │ - Theme setup   │  │ - State Mgmt    │  │ - History    │ │
│  │ - Entry Point   │  │ - Responsive    │  │ - Bulk SMS   │ │
│  └─────────────────┘  └─────────────────┘  │ - Templates │ │
│                                            │ - Settings   │ │
│                                            └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   BUSINESS LOGIC LAYER                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   SmsManager    │  │ScheduledSmsMgr  │  │BulkSmsManager│ │
│  │                 │  │                 │  │              │ │
│  │ - Send SMS      │  │ - Schedule SMS  │  │ - Bulk send  │ │
│  │ - Receive SMS   │  │ - Time calc     │  │ - Progress   │ │
│  │ - Validation    │  │ - Status Mgmt   │  │ - Batching   │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │TemplateManager  │  │TemplateEngine   │  │Utility Classes│ │
│  │                 │  │                 │  │              │ │
│  │ - CRUD templates│  │ - Variable sub  │  │ - Permissions│ │
│  │ - Categories    │  │ - Validation    │  │ - Contacts   │ │
│  │ - Search        │  │ - JSON serial   │  │ - Logging    │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATA PROCESSING LAYER                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Room Database │  │  WorkManager    │  │Network Mgmt  │ │
│  │                 │  │                 │  │              │ │
│  │ - SMS Messages  │  │ - Background    │  │ - HTTP calls │ │
│  │ - Templates     │  │ - Scheduling    │  │ - API sync   │ │
│  │ - Contacts      │  │ - Constraints   │  │ - Status     │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │EncryptedStorage│  │  System Utils   │  │Notifications│ │
│  │                 │  │                 │  │              │ │
│  │ - API keys      │  │ - DateUtils     │  │ - Status     │ │
│  │ - Settings      │  │ - Health check  │  │ - Errors     │ │
│  │ - Tokens        │  │ - Permissions   │  │ - Progress   │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 📱 Presentation Layer

### Core Components

#### MainActivity.kt
**Purpose:** Application entry point and permission initialization
**Key Features:**
- Initializes notification channels via `Notify.ensureChannels()`
- Sets up Jetpack Compose with `Smsbramkax1Theme`
- Clean, minimal implementation following Android best practices
- Single responsibility principle

#### MainApp.kt
**Purpose:** Main navigation and UI orchestration
**Key Features:**
- Responsive design with sidebar for tablets (≥840dp) and drawer for mobile
- Comprehensive navigation between all major screens
- Proper state management with `remember` and `mutableStateOf`
- Lazy initialization of managers (SmsManager, TemplateManager, etc.)

**UI Architecture:**
- Material 3 design system
- Conditional layout based on screen size
- Proper navigation state management
- Efficient recomposition patterns

#### SmsGatewayApplication.kt
**Purpose:** Application-level configuration and WorkManager setup
**Key Features:**
- Implements `Configuration.Provider` for custom WorkManager setup
- Initializes WorkManager with DEBUG logging level
- Proper error handling for initialization failures
- Dependency injection pattern for WorkManager configuration

---

## 🔄 Business Logic Layer

### SMS Management System

#### SmsManager.kt
**Purpose:** Core SMS sending and receiving functionality
**Key Features:**
- Device SMS capability checking
- Phone number validation with regex patterns
- Multipart SMS support for long messages
- Network operator detection
- Comprehensive error handling with Result pattern

**Technical Implementation:**
- Uses Android's `SmsManager` API
- Proper status tracking in database
- Coroutine-based async operations
- Input validation and sanitization

#### BulkSmsManager.kt
**Purpose:** Bulk SMS campaign processing
**Architecture:** Singleton pattern with thread safety
**Key Features:**
- Batch processing with configurable delays and batch sizes
- Real-time progress tracking via StateFlow
- Campaign cancellation support
- Rate limiting and retry mechanisms
- Comprehensive error collection

**Advanced Features:**
- Automatic batch ID generation
- Progress estimation algorithms
- Memory-efficient processing
- Background job management
- Resource cleanup on completion

#### ScheduledSmsManager.kt
**Purpose:** Scheduled SMS lifecycle management
**Key Features:**
- Flexible scheduling with immediate processing option
- Full CRUD operations for scheduled messages
- Automatic cleanup of old messages
- Status transition management
- Time-based processing logic

**Business Logic:**
- Preserves user-selected times
- Processes messages within 24-hour window
- Proper status transitions (SCHEDULED → QUEUED → SENT)
- Cleanup of deleted messages
- Room Flow for reactive updates

### Template System

#### TemplateManager.kt
**Purpose:** SMS template management and rendering
**Features:**
- Template CRUD with validation
- Variable extraction and management
- Category-based organization
- Search functionality
- Default template initialization

**Template Features:**
- Variable substitution ({{name}}, {{date}}, etc.)
- JSON serialization for variables
- Template validation
- Default templates for beauty salon use cases

**Default Templates:**
- Appointment reminders
- Confirmation messages
- Cancellation notices
- Promotional messages
- Information updates

#### TemplateEngine.kt
**Purpose:** Template variable processing and validation
**Features:**
- Regex-based variable extraction
- Safe variable substitution
- Template validation
- JSON serialization support
- Common variable definitions

**Variable Support:**
- `{{name}}` - Customer name
- `{{time}}` - Appointment time
- `{{date}}` - Appointment date
- `{{service}}` - Service type
- `{{price}}` - Service price
- `{{address}}` - Location
- `{{phone}}` - Contact phone
- `{{company}}` - Business name

---

## 🔧 Utility Classes

#### PermissionsManager.kt
**Purpose:** Comprehensive permission management
**Permissions Handled:**
- SMS permissions (SEND, RECEIVE, READ)
- Notification permissions (POST_NOTIFICATIONS for API 33+)
- Contacts permissions (READ_CONTACTS)
- Storage permissions (optional)
- Battery optimization permissions

**Key Features:**
- Runtime permission checking
- Permission rationale dialogs
- Battery optimization handling
- Settings navigation
- Compose integration

**Dialog Components:**
- PermissionRequestDialog for initial requests
- PermissionRationaleDialog for denied permissions
- BatteryOptimizationDialog for power management

#### ContactManager.kt
**Purpose:** Device contact synchronization and management
**Features:**
- Contact synchronization from Android Contacts API
- Phone number cleaning and validation
- Search functionality
- Contact caching

**Phone Number Processing:**
- Cleaning: removes non-digit characters
- Formatting: international format conversion
- Validation: regex-based validation
- Deduplication: contactId-based removal

#### LogManager.kt
**Purpose:** Centralized logging system
**Features:**
- Database logging with structured data
- Android Log integration
- Log level filtering
- SMS-specific log tracking
- Stack trace capture

**Log Levels:**
- INFO: General information
- WARNING: Warning messages
- ERROR: Error conditions with stack traces

**Usage Pattern:** `LogManager.log("INFO", "Category", "Message", smsId)`

#### NetworkState.kt
**Purpose:** Network connectivity monitoring
**Features:**
- Internet connectivity checking
- Network type detection (WiFi/Mobile)
- Modern NetworkCapabilities API usage
- Thread-safe operations

**Methods:**
- `isOnline()`: Internet connectivity
- `isWifi()`: WiFi connection detection
- `isMobile()`: Cellular connection detection

#### HealthChecker.kt
**Purpose:** System health monitoring and diagnostics
**Features:**
- Comprehensive health status tracking
- Database statistics
- Network status monitoring
- Error rate calculation
- Health status determination

**Health Metrics:**
- Pending message count
- Failed messages (last hour/day)
- Last send timestamp
- Network connectivity status
- Total sent/failed counts

---

## 📊 Data Flow Architecture

### SMS Processing Pipeline

```
UI Input
    │
    ▼
Validation → Database Storage (PENDING)
    │
    ▼
WorkManager Enqueue
    │
    ▼
Background Worker Processing
    │
    ▼
Android SMS API
    │
    ▼
Status Updates → Database
    │
    ▼
Real-time UI Updates + Notifications
```

### State Management

**UI State:**
- Compose `remember` and `mutableStateOf`
- `collectAsStateWithLifecycle` for efficient state collection
- Proper coroutine scope management

**Business State:**
- StateFlow for reactive updates in managers
- Room Flow for database changes
- SharedFlow for events

### Error Handling

**Result Pattern:**
- `Result<T>` for operations
- User-friendly error messages
- Retry mechanisms with exponential backoff
- Comprehensive logging for debugging

---

## 🔒 Security Architecture

### Input Validation
**Phone Numbers:**
- Regex-based validation
- International format support
- Length restrictions
- Character filtering

**Message Content:**
- Length validation
- Character encoding checks
- Content sanitization

### Permission Security
**Runtime Checks:**
- Permission status validation
- Graceful degradation
- User education
- Settings navigation

### Data Protection
**Encrypted Storage:**
- API key encryption
- Settings protection
- Token management
- Secure data persistence

---

## 🎯 Architecture Strengths

### Design Principles
1. **Clean Architecture:** Proper separation of concerns
2. **Modern Android:** Jetpack Compose, Material 3, Coroutines
3. **Reactive Programming:** Flow and StateFlow for real-time updates
4. **Error Handling:** Comprehensive Result pattern usage
5. **State Management:** Proper Compose state patterns
6. **Background Processing:** WorkManager integration
7. **Security:** Input validation and encrypted storage

### Code Quality Indicators
- **Consistent Naming:** Follows Kotlin conventions
- **Documentation:** Comprehensive comments and documentation
- **Error Handling:** Proper exception handling throughout
- **Testing:** Structure supports unit testing
- **Performance:** Efficient data loading and state management
- **Maintainability:** Clear separation of concerns

### Scalability Considerations
- **Database Design:** Unified table approach for efficiency
- **Background Processing:** WorkManager benefits (retry, constraints, battery optimization)
- **Memory Management:** Lazy loading, proper cleanup, resource pooling

---

## 📚 Key Architectural Decisions

### Unified SMS Table (Version 3)
**Problem:** Separate tables for scheduled and immediate SMS caused data consistency issues
**Solution:** Single `sms_messages` table with `isScheduled` boolean field
**Benefits:**
- Single source of truth
- Simplified queries
- Better data consistency
- Easier maintenance

### WorkManager Integration
**Choice:** WorkManager for background processing
**Reasons:**
- Automatic retry with exponential backoff
- Constraint-based execution (network, battery)
- System integration and battery optimization
- Guaranteed execution across app restarts

### Jetpack Compose + Material 3
**Choice:** Modern declarative UI
**Benefits:**
- Less code, fewer bugs
- Reactive UI with state management
- Material 3 design system out of the box
- Better performance with intelligent recomposition

---

**Status:** Architecture demonstrates enterprise-level design with clean separation of concerns, modern Android practices, and production-ready quality.