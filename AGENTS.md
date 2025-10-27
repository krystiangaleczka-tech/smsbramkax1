# SMS Gateway Android App - Agent Guidelines

## Build Commands
- **Build**: `./gradlew build`
- **Clean**: `./gradlew clean`
- **Install debug**: `./gradlew installDebug`
- **Run tests**: `./gradlew test`
- **Run single test**: `./gradlew test --tests "com.example.smsbramkax1.ExampleUnitTest.addition_isCorrect"`
- **Run instrumented tests**: `./gradlew connectedAndroidTest`
- **Lint**: `./gradlew lint`

## Code Style Guidelines
- **Language**: Kotlin with Jetpack Compose
- **Style**: Follow Kotlin Official Style Guide (configured in IDE)
- **Package structure**: `com.example.smsbramkax1.{data,dto,network,receivers,services,sms,storage,ui,utils,workers}`
- **Naming**: PascalCase for classes, camelCase for functions/variables
- **Imports**: Group imports alphabetically, no wildcards
- **Database**: Room entities with `@Entity`, use `@PrimaryKey(autoGenerate = true)`
- **UI**: Jetpack Compose with Material3, use `@Composable` functions
- **Async**: Use coroutines (`suspend` functions, `withContext`)
- **Logging**: Use Timber for logging
- **Error handling**: Use Result/try-catch, nullable types for optional values
- **Testing**: JUnit for unit tests, Espresso for UI tests