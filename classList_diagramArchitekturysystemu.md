# 📝 KOMPLETNA LISTA KLAS I DIAGRAM ARCHITEKTURY

***

## 📦 WSZYSTKIE KLASY PROJEKTU (Alfabetycznie z pełną ścieżką)

### 📁 **Package: com.smsgateway.beauty** (Root)

```kotlin
// 1. Application Class
com.smsgateway.beauty.GatewayApplication : Application
```

***

### 📁 **Package: com.smsgateway.beauty.api**

```kotlin
// 2. HTTP Server
com.smsgateway.beauty.api.KtorServer
```

***

### 📁 **Package: com.smsgateway.beauty.api.dto**

```kotlin
// 3-6. Data Transfer Objects
com.smsgateway.beauty.api.dto.ApiResponse
com.smsgateway.beauty.api.dto.QueueSmsRequest
com.smsgateway.beauty.api.dto.QueueSmsResponse
com.smsgateway.beauty.api.dto.SmsDetail
com.smsgateway.beauty.api.dto.SmsStatusResponse
```

***

### 📁 **Package: com.smsgateway.beauty.api.middleware**

```kotlin
// 7. Middleware Extension Function (w pliku AuthMiddleware.kt)
// Extension: ApplicationCall.verifyAuth(encryptedStorage: EncryptedStorage): Boolean
```

***

### 📁 **Package: com.smsgateway.beauty.api.routes**

```kotlin
// 8-9. Route Extension Functions
// Extension: Route.setupSmsRoutes(context: Context, encryptedStorage: EncryptedStorage)
// Extension: Route.setupWebRoutes(context: Context)
```

***

### 📁 **Package: com.smsgateway.beauty.data**

```kotlin
// 10. Enum
com.smsgateway.beauty.data.SmsStatus : Enum<SmsStatus>

// 11-13. Data Classes / Entities
com.smsgateway.beauty.data.AppSettings
com.smsgateway.beauty.data.SmsMessage : @Entity
com.smsgateway.beauty.data.SystemLog : @Entity
```

***

### 📁 **Package: com.smsgateway.beauty.service**

```kotlin
// 14. Broadcast Receiver
com.smsgateway.beauty.service.BootReceiver : BroadcastReceiver

// 15. Helper Class
com.smsgateway.beauty.service.CloudflareManager

// 16. Broadcast Receiver
com.smsgateway.beauty.service.DeliveryReceiver : BroadcastReceiver

// 17. Foreground Service
com.smsgateway.beauty.service.SmsSchedulerService : Service

// 18. Service Class
com.smsgateway.beauty.service.SmsSenderService
```

***

### 📁 **Package: com.smsgateway.beauty.storage**

```kotlin
// 19. DAO Interface
com.smsgateway.beauty.storage.LogDao : @Dao

// 20. Room Database
com.smsgateway.beauty.storage.SmsDatabase : RoomDatabase

// 21. DAO Interface
com.smsgateway.beauty.storage.SmsDao : @Dao

// 22. Security Class
com.smsgateway.beauty.storage.EncryptedStorage

// 23. Preferences Manager
com.smsgateway.beauty.storage.PreferencesManager
```

***

### 📁 **Package: com.smsgateway.beauty.ui**

```kotlin
// 24. Main Activity
com.smsgateway.beauty.ui.MainActivity : AppCompatActivity
```

***

### 📁 **Package: com.smsgateway.beauty.utils**

```kotlin
// 25. Object (Singleton)
com.smsgateway.beauty.utils.BatteryOptimizer : object

// 26. Object (Singleton)
com.smsgateway.beauty.utils.DateTimeHelper : object

// 27. Object (Singleton)
com.smsgateway.beauty.utils.LogManager : object

// 28. Object (Singleton)
com.smsgateway.beauty.utils.SecurityUtils : object
```

***

## 📊 PODSUMOWANIE STATYSTYK KLAS

| Kategoria | Liczba | Typy |
|-----------|--------|------|
| **Total Classes** | 28 | - |
| **Kotlin Classes** | 15 | class |
| **Data Classes** | 7 | data class |
| **Entities (Room)** | 2 | @Entity |
| **Interfaces (DAO)** | 2 | @Dao interface |
| **Enums** | 1 | enum class |
| **Singletons (Object)** | 4 | object |
| **Services** | 1 | Service |
| **Receivers** | 2 | BroadcastReceiver |
| **Activities** | 1 | AppCompatActivity |
| **Application** | 1 | Application |
| **Extension Functions** | 3 | (w plikach routes/middleware) |

***

## 📋 LISTA KLAS WEDŁUG KATEGORII FUNKCJONALNEJ

### 🔷 **WARSTWA APLIKACJI** (1 klasa)
```
1. GatewayApplication
```

### 🔷 **WARSTWA UI** (1 klasa)
```
2. MainActivity
```

### 🔷 **WARSTWA API** (9 klas/funkcji)
```
3. KtorServer
4. ApiResponse
5. QueueSmsRequest
6. QueueSmsResponse
7. SmsDetail
8. SmsStatusResponse
9. AuthMiddleware (extension)
10. SmsRoutes (extension)
11. WebRoutes (extension)
```

### 🔷 **WARSTWA DANYCH** (4 klasy)
```
12. SmsStatus (enum)
13. SmsMessage (entity)
14. SystemLog (entity)
15. AppSettings (data class)
```

### 🔷 **WARSTWA STORAGE** (5 klas)
```
16. SmsDatabase
17. SmsDao
18. LogDao
19. EncryptedStorage
20. PreferencesManager
```

### 🔷 **WARSTWA SERWISÓW** (5 klas)
```
21. SmsSchedulerService
22. SmsSenderService
23. DeliveryReceiver
24. BootReceiver
25. CloudflareManager
```

### 🔷 **WARSTWA UTILS** (4 klasy - singletons)
```
26. BatteryOptimizer
27. DateTimeHelper
28. LogManager
29. SecurityUtils
```

***

## 🎨 DIAGRAM ARCHITEKTURY SYSTEMU

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         EXTERNAL WORLD                                      │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐         ┌──────────────────┐      ┌───────────────┐ │
│  │  TypeScript App │◄────────┤ Cloudflare Tunnel├──────┤  Web Browser  │ │
│  │  (Booking Sys)  │  HTTPS  │   (External)     │      │  (Dashboard)  │ │
│  └────────┬────────┘         └────────┬─────────┘      └───────┬───────┘ │
│           │                           │                         │         │
└───────────┼───────────────────────────┼─────────────────────────┼─────────┘
            │ REST API                  │                         │ HTTP
            │ POST /api/sms/queue       │ Tunnel                  │ GET /dashboard
            │ GET  /api/sms/status      │                         │
            │                           │                         │
┌───────────▼───────────────────────────▼─────────────────────────▼─────────┐
│                      ANDROID APPLICATION LAYER                             │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐ │
│  │                      GatewayApplication                               │ │
│  │  • onCreate() - Initialize all components                            │ │
│  │  • Start KtorServer                                                  │ │
│  │  • Start SmsSchedulerService                                         │ │
│  │  • Initialize LogManager                                             │ │
│  └───────────────────────────┬──────────────────────────────────────────┘ │
│                              │                                             │
│  ┌───────────────────────────▼──────────────────────────────────────────┐ │
│  │                          MainActivity                                 │ │
│  │  • Request SMS permissions                                           │ │
│  │  • Request Battery Optimization whitelist                            │ │
│  │  • Show simple status UI                                             │ │
│  └──────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │
┌────────────────────────────────────▼─────────────────────────────────────┐
│                           API LAYER (Ktor Server)                         │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                          KtorServer                                   ││
│  │  Port: 8080                                                           ││
│  │  • ContentNegotiation (Gson)                                          ││
│  │  • CORS                                                               ││
│  │  • Request Logging                                                    ││
│  └───────────────────────────┬───────────────────────────────────────────┘│
│                              │                                             │
│    ┌─────────────────────────┼─────────────────────────┐                 │
│    │                         │                         │                 │
│    │                         │                         │                 │
│  ┌─▼───────────┐  ┌──────────▼───────────┐  ┌────────▼──────────┐      │
│  │ WebRoutes   │  │    SmsRoutes         │  │ AuthMiddleware    │      │
│  │             │  │                      │  │                   │      │
│  │ GET /       │  │ POST /api/sms/queue  │  │ verifyAuth()      │      │
│  │ GET /dash   │  │ GET  /api/sms/status │  │ • Bearer Token    │      │
│  │ GET /logs   │  │ GET  /api/sms/list   │  │ • Validation      │      │
│  │ GET /settings│ │ DELETE /api/sms/:id  │  │                   │      │
│  │             │  │ GET  /api/logs       │  │                   │      │
│  └─────────────┘  └──────────┬───────────┘  └─────────┬─────────┘      │
│                              │                         │                 │
│                              │                         │                 │
│  ┌───────────────────────────┼─────────────────────────┘                 │
│  │                           │                                           │
│  │  DTO Classes:             │                                           │
│  │  • QueueSmsRequest        │                                           │
│  │  • QueueSmsResponse       │                                           │
│  │  • SmsStatusResponse      │                                           │
│  │  • SmsDetail              │                                           │
│  │  • ApiResponse            │                                           │
│  └───────────────────────────┘                                           │
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │
┌────────────────────────────────────▼─────────────────────────────────────┐
│                        SERVICE LAYER (Business Logic)                     │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                     SmsSchedulerService                               ││
│  │  (Foreground Service - DATA_SYNC)                                    ││
│  │                                                                       ││
│  │  LOOP (every 60s):                                                   ││
│  │    1. getPendingMessages(currentTime)                                ││
│  │    2. Check sending hours (8-21)                                     ││
│  │    3. Check maxRetries                                               ││
│  │    4. Call SmsSenderService.sendSms()                                ││
│  │    5. Rate limit: 500ms between SMS                                  ││
│  │    6. Update notification                                            ││
│  │    7. cleanupOldData()                                               ││
│  └───────────────────────────┬───────────────────────────────────────────┘│
│                              │                                             │
│                              │ uses                                        │
│                              ▼                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                      SmsSenderService                                 ││
│  │                                                                       ││
│  │  sendSms(message):                                                   ││
│  │    1. Update status → SENDING                                        ││
│  │    2. Create PendingIntents (sent/delivered)                         ││
│  │    3. SmsManager.sendTextMessage()                                   ││
│  │    4. Catch exceptions → FAILED                                      ││
│  │                                                                       ││
│  │  updateMessageStatus(id, status, error)                              ││
│  └───────────────────────────┬───────────────────────────────────────────┘│
│                              │                                             │
│                              │ triggers                                    │
│                              ▼                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                      DeliveryReceiver                                 ││
│  │  (BroadcastReceiver)                                                 ││
│  │                                                                       ││
│  │  onReceive(intent):                                                  ││
│  │    • SMS_SENT action:                                                ││
│  │      - RESULT_OK → SENT                                              ││
│  │      - RESULT_ERROR_* → FAILED                                       ││
│  │    • SMS_DELIVERED action:                                           ││
│  │      - RESULT_OK → DELIVERED                                         ││
│  │      - RESULT_CANCELED → NOT_DELIVERED                               ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                        BootReceiver                                   ││
│  │  (BroadcastReceiver)                                                 ││
│  │                                                                       ││
│  │  onReceive(ACTION_BOOT_COMPLETED):                                   ││
│  │    • Check settings.schedulerEnabled                                 ││
│  │    • Start SmsSchedulerService                                       ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                      CloudflareManager                                ││
│  │                                                                       ││
│  │  • isConfigured()                                                    ││
│  │  • getPublicUrl()                                                    ││
│  │  • getSetupInstructions()                                            ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │
┌────────────────────────────────────▼─────────────────────────────────────┐
│                      STORAGE LAYER (Persistence)                          │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                        SmsDatabase                                    ││
│  │  (Room Database - Singleton)                                         ││
│  │                                                                       ││
│  │  Entities:                                                           ││
│  │    • SmsMessage (18 fields)                                          ││
│  │    • SystemLog (7 fields)                                            ││
│  │                                                                       ││
│  │  DAOs:                                                               ││
│  │    • SmsDao                                                          ││
│  │    • LogDao                                                          ││
│  └───────────────────────────┬───────────────────────────────────────────┘│
│                              │                                             │
│    ┌─────────────────────────┼─────────────────────────┐                 │
│    │                         │                         │                 │
│  ┌─▼───────────────┐  ┌──────▼──────────────┐  ┌──────▼──────────────┐ │
│  │    SmsDao       │  │     LogDao          │  │ EncryptedStorage    │ │
│  │  (Interface)    │  │  (Interface)        │  │                     │ │
│  │                 │  │                     │  │ • MasterKey         │ │
│  │ • getAllMsg()   │  │ • getRecentLogs()   │  │ • AES256_GCM        │ │
│  │ • getByStatus() │  │ • getLogsByLevel()  │  │                     │ │
│  │ • getById()     │  │ • getLogsSince()    │  │ saveApiToken()      │ │
│  │ • getByExtId()  │  │ • insertLog()       │  │ getApiToken()       │ │
│  │ • getPending()  │  │ • deleteOldLogs()   │  │ generateNewToken()  │ │
│  │ • insert()      │  │ • getErrorCount()   │  │ clearToken()        │ │
│  │ • update()      │  │                     │  │                     │ │
│  │ • delete()      │  │                     │  │                     │ │
│  │ • getCount()    │  │                     │  │                     │ │
│  │ • deleteOld()   │  │                     │  │                     │ │
│  └─────────────────┘  └─────────────────────┘  └─────────────────────┘ │
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                      PreferencesManager                               ││
│  │  (SharedPreferences Wrapper)                                         ││
│  │                                                                       ││
│  │  • saveSettings(AppSettings)                                         ││
│  │  • getSettings(): AppSettings                                        ││
│  │                                                                       ││
│  │  Settings (14 params):                                               ││
│  │    - apiEnabled, apiPort                                             ││
│  │    - cloudflareEnabled, tunnelId, hostname                           ││
│  │    - schedulerEnabled, checkIntervalSeconds                          ││
│  │    - autoSend, sendHour, sendHourEnd, maxRetries                     ││
│  │    - logLevel, logRetentionDays                                      ││
│  │    - batteryOptimizationDisabled                                     ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │
┌────────────────────────────────────▼─────────────────────────────────────┐
│                      DATA LAYER (Models & Enums)                          │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                          SmsMessage                                   ││
│  │  @Entity(tableName = "sms_messages")                                 ││
│  │                                                                       ││
│  │  Fields (18):                                                        ││
│  │    • id: Long (PK, auto)                                             ││
│  │    • phoneNumber: String                                             ││
│  │    • message: String                                                 ││
│  │    • status: String                                                  ││
│  │    • queuedAt: Long                                                  ││
│  │    • scheduledFor: Long                                              ││
│  │    • sentAt: Long?                                                   ││
│  │    • deliveredAt: Long?                                              ││
│  │    • failedAt: Long?                                                 ││
│  │    • externalId: String?                                             ││
│  │    • clientName: String?                                             ││
│  │    • serviceType: String?                                            ││
│  │    • errorMessage: String?                                           ││
│  │    • retryCount: Int                                                 ││
│  │    • maxRetries: Int                                                 ││
│  │    • deliveryStatus: String?                                         ││
│  │    • operatorStatus: String?                                         ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                          SystemLog                                    ││
│  │  @Entity(tableName = "system_logs")                                  ││
│  │                                                                       ││
│  │  Fields (7):                                                         ││
│  │    • id: Long (PK, auto)                                             ││
│  │    • timestamp: Long                                                 ││
│  │    • level: String (DEBUG/INFO/WARNING/ERROR)                        ││
│  │    • category: String (SMS/API/SCHEDULER/SYSTEM)                     ││
│  │    • message: String                                                 ││
│  │    • smsId: Long?                                                    ││
│  │    • stackTrace: String?                                             ││
│  │    • meta String? (JSON)                                        ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                          SmsStatus                                    ││
│  │  enum class                                                          ││
│  │                                                                       ││
│  │  Values (7):                                                         ││
│  │    • QUEUED                                                          ││
│  │    • SENDING                                                         ││
│  │    • SENT                                                            ││
│  │    • DELIVERED                                                       ││
│  │    • NOT_DELIVERED                                                   ││
│  │    • FAILED                                                          ││
│  │    • DELETED                                                         ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                        AppSettings                                    ││
│  │  data class                                                          ││
│  │                                                                       ││
│  │  Fields (14): apiEnabled, apiPort, apiToken, cloudflareEnabled, ...  ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │
┌────────────────────────────────────▼─────────────────────────────────────┐
│                      UTILS LAYER (Helpers & Tools)                        │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                        LogManager                                     ││
│  │  object (Singleton)                                                  ││
│  │                                                                       ││
│  │  • init(context)                                                     ││
│  │  • d(category, message, smsId?) - DEBUG                              ││
│  │  • i(category, message, smsId?) - INFO                               ││
│  │  • w(category, message, smsId?) - WARNING                            ││
│  │  • e(category, message, throwable?, smsId?) - ERROR                  ││
│  │  • cleanOldLogs(retentionDays)                                       ││
│  │                                                                       ││
│  │  Integration:                                                        ││
│  │    • Timber (console)                                                ││
│  │    • Room Database (persistent)                                      ││
│  │    • Coroutines (async)                                              ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                      DateTimeHelper                                   ││
│  │  object (Singleton)                                                  ││
│  │                                                                       ││
│  │  • parseIso8601(dateString): Long                                    ││
│  │  • toIso8601(timestamp): String                                      ││
│  │  • formatDisplay(timestamp): String                                  ││
│  │  • calculateSendTime(appointmentTime): Long  // -24h                 ││
│  │  • isWithinSendingHours(timestamp, start, end): Boolean              ││
│  │  • getStartOfDay(): Long                                             ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                      SecurityUtils                                    ││
│  │  object (Singleton)                                                  ││
│  │                                                                       ││
│  │  • generateApiToken(): String  // sk_live_ + 256-bit random          ││
│  │  • validatePhoneNumber(phone): Boolean                               ││
│  │  • sanitizePhoneNumber(phone): String                                ││
│  │  • validateToken(provided, stored): Boolean  // constant-time        ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐│
│  │                     BatteryOptimizer                                  ││
│  │  object (Singleton)                                                  ││
│  │                                                                       ││
│  │  • isBatteryOptimizationDisabled(context): Boolean                   ││
│  │  • requestDisableBatteryOptimization(activity)                       ││
│  │  • openBatterySettings(context)                                      ││
│  │  • getBatteryOptimizationTips(): List<String>                        ││
│  └──────────────────────────────────────────────────────────────────────┘│
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │
┌────────────────────────────────────▼─────────────────────────────────────┐
│                   ANDROID SYSTEM LAYER (SDK)                              │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  • SmsManager - SMS wysyłanie                                             │
│  • PowerManager - Wake locks, battery optimization                        │
│  • NotificationManager - Foreground service notification                  │
│  • SharedPreferences - Settings storage                                   │
│  • Room Database - SQLite wrapper                                         │
│  • Android Keystore - Encryption keys                                     │
│  • BroadcastReceiver - SMS delivery reports, Boot events                  │
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
```

***

## 🔗 FLOW DIAGRAM - Wysyłanie SMS (End-to-End)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    FLOW: Queue SMS → Delivery Report                     │
└─────────────────────────────────────────────────────────────────────────┘

1. EXTERNAL APP (TypeScript)
   │
   │ POST /api/sms/queue
   │ {
   │   phone: "+48123456789",
   │   message: "Przypominamy...",
   │   scheduledTime: "2025-10-28T14:00:00Z"
   │ }
   │
   ▼

2. KtorServer (Port 8080)
   │
   │ AuthMiddleware.verifyAuth()
   │ ├─ Check Authorization header
   │ ├─ Validate Bearer Token
   │ └─ Return 401 if invalid
   │
   ▼

3. SmsRoutes.setupSmsRoutes()
   │
   │ POST /api/sms/queue handler:
   │ ├─ Validate phone number (SecurityUtils)
   │ ├─ Sanitize phone number
   │ ├─ Parse scheduledTime (DateTimeHelper)
   │ ├─ Calculate sendTime = scheduledTime - 24h
   │ └─ If sendTime < now → sendTime = now
   │
   ▼

4. SmsDao.insertMessage()
   │
   │ Insert into database:
   │ ├─ status: QUEUED
   │ ├─ queuedAt: now
   │ ├─ scheduledFor: sendTime
   │ └─ Save externalId, clientName, etc.
   │
   ▼

5. Response to External App
   │
   │ 201 Created
   │ {
   │   success: true,
   │   smsId: "sms_123",
   │   status: "QUEUED",
   │   estimatedSendTime: "2025-10-27T14:00:00Z"
   │ }
   │
   └─────────────────────────────────────────────────────────────────────────

   ... WAIT (Scheduler checks every 60s) ...

┌─────────────────────────────────────────────────────────────────────────┐
│                    SCHEDULER LOOP (Every 60s)                            │
└─────────────────────────────────────────────────────────────────────────┘

6. SmsSchedulerService (Background)
   │
   │ Loop iteration:
   │ ├─ Call SmsDao.getPendingMessages(now)
   │ ├─ Found: SMS with scheduledFor <= now
   │ ├─ Check: isWithinSendingHours(8-21) → YES
   │ ├─ Check: retryCount < maxRetries → YES
   │ └─ Call SmsSenderService.sendSms(message)
   │
   ▼

7. SmsSenderService.sendSms()
   │
   │ Step 1: Update status → SENDING
   │ ├─ SmsDao.updateMessage(status = SENDING)
   │ └─ LogManager.i("SMS", "Attempting to send...")
   │
   │ Step 2: Create PendingIntents
   │ ├─ sentIntent (action: SMS_SENT, smsId)
   │ └─ deliveredIntent (action: SMS_DELIVERED, smsId)
   │
   │ Step 3: Send SMS
   │ ├─ SmsManager.sendTextMessage(
   │ │     phoneNumber,
   │ │     null,
   │ │     message,
   │ │     sentIntent,
   │ │     deliveredIntent
   │ │  )
   │ └─ Return Result.success()
   │
   ▼

8. Android SMS System
   │
   │ SMS sent to operator...
   │ └─ Broadcast: SMS_SENT
   │
   ▼

9. DeliveryReceiver.onReceive()
   │
   │ Action: SMS_SENT
   │ ├─ Extract smsId from Intent
   │ ├─ Check resultCode:
   │ │   • RESULT_OK → status = SENT
   │ │   • RESULT_ERROR_* → status = FAILED
   │ ├─ SmsSenderService.updateMessageStatus(smsId, status)
   │ └─ LogManager.i("SMS", "SMS sent successfully")
   │
   ▼

10. SmsDao.updateMessage()
    │
    │ Update database:
    │ ├─ status: SENT
    │ ├─ sentAt: now
    │ └─ Log entry created
    │
    ▼

    ... WAIT (Operator confirms delivery) ...

11. Android SMS System
    │
    │ Delivery report received...
    │ └─ Broadcast: SMS_DELIVERED
    │
    ▼

12. DeliveryReceiver.onReceive()
    │
    │ Action: SMS_DELIVERED
    │ ├─ Check resultCode:
    │ │   • RESULT_OK → status = DELIVERED
    │ │   • RESULT_CANCELED → status = NOT_DELIVERED
    │ ├─ SmsSenderService.updateMessageStatus(smsId, DELIVERED)
    │ └─ LogManager.i("SMS", "SMS delivered")
    │
    ▼

13. SmsDao.updateMessage()
    │
    │ Update database:
    │ ├─ status: DELIVERED
    │ ├─ deliveredAt: now
    │ └─ Log entry created
    │
    └─────────────────────────────────────────────────────────────────────────

14. EXTERNAL APP (TypeScript) - Check Status
    │
    │ GET /api/sms/status/sms_123
    │
    ▼

15. KtorServer → SmsRoutes
    │
    │ GET /api/sms/status/:id handler:
    │ ├─ Verify auth
    │ ├─ SmsDao.getMessageById(123)
    │ └─ Return status
    │
    ▼

16. Response to External App
    │
    │ 200 OK
    │ {
    │   success: true,
    │   sms: {
    │     id: "sms_123",
    │     status: "DELIVERED",
    │     sentAt: "2025-10-27T14:00:05Z",
    │     deliveredAt: "2025-10-27T14:00:12Z"
    │   }
    │ }
    │
    └─────────────────────────────────────────────────────────────────────────

END FLOW
```

***

## 📌 NAMING CONVENTIONS - WAŻNE UWAGI

### ⚠️ Aby uniknąć konfliktów nazw:

1. **Package name MUSI być dokładnie**:
   ```
   com.smsgateway.beauty
   ```

2. **Nazwy klas są case-sensitive**:
   - ✅ `SmsMessage` (poprawne)
   - ❌ `SMSMessage` (błędne)
   - ❌ `smsMessage` (błędne)

3. **Extension functions są w osobnych plikach**:
   - Plik: `AuthMiddleware.kt` → Function: `ApplicationCall.verifyAuth()`
   - Plik: `SmsRoutes.kt` → Function: `Route.setupSmsRoutes()`
   - Plik: `WebRoutes.kt` → Function: `Route.setupWebRoutes()`

4. **Singletons używają `object`**:
   ```kotlin
   object LogManager { ... }  // NIE class LogManager
   ```

5. **Entities MUSZĄ mieć @Entity annotation**:
   ```kotlin
   @Entity(tableName = "sms_messages")
   data class SmsMessage(...)
   ```

6. **DAOs MUSZĄ być interface z @Dao**:
   ```kotlin
   @Dao
   interface SmsDao { ... }  // NIE class SmsDao
   ```

7. **Enums używają enum class**:
   ```kotlin
   enum class SmsStatus { ... }
   ```

***

To kompletna lista wszystkich klas i szczegółowy diagram architektury systemu! 🎉

Źródła
