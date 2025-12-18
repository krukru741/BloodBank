# BloodBank - Blood Donation Management System

A modern Android application for managing blood donations, connecting donors with recipients, and tracking donation history with gamification features.

## 🏗️ Architecture

This project follows **Clean Architecture** principles with **MVVM (Model-View-ViewModel)** pattern and **Repository Pattern** for data abstraction.

### Architecture Layers

```
┌─────────────────────────────────────────┐
│           Presentation Layer            │
│  (Activities, Adapters, ViewModels)    │
└──────────────┬──────────────────────────┘
               │ StateFlow/LiveData
┌──────────────▼──────────────────────────┐
│           Domain Layer                   │
│      (ViewModels, Use Cases)            │
└──────────────┬──────────────────────────┘
               │ Repository Interface
┌──────────────▼──────────────────────────┐
│            Data Layer                    │
│  (Repositories, Firebase, Local Storage)│
└─────────────────────────────────────────┘
```

### Tech Stack

- **Language**: Kotlin 100% (migrated from Java)
- **Architecture**: MVVM + Repository Pattern
- **Dependency Injection**: Hilt
- **Async**: Kotlin Coroutines & Flow
- **Backend**: Firebase (Realtime Database, Authentication, Firestore)
- **Background Tasks**: WorkManager
- **Image Loading**: Glide
- **UI**: Material Design Components

## 🚀 Features

- **User Authentication**: Email/password with verification
- **Donor Management**: Profile, health tracking, donation history
- **Recipient Management**: Blood request system
- **Emergency Requests**: Priority-based blood requests
- **Chat System**: Real-time messaging between users
- **Notifications**: Push notifications for requests and reminders
- **Achievements**: Gamification with badges and leaderboard
- **Donation Centers**: Location-based center finder
- **Appointment Scheduling**: Book donation appointments

## 📋 Prerequisites

- **Android Studio**: Arctic Fox or newer
- **JDK**: 17 or higher
- **Android SDK**: API 24+ (Android 7.0+)
- **Gradle**: 8.2.2
- **Kotlin**: 1.9.20

## 🛠️ Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd BloodBank
```

### 2. Firebase Configuration

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app with package name: `com.example.bloodbank`
3. Download `google-services.json`
4. Place it in `app/` directory
5. Enable:
   - Firebase Authentication (Email/Password)
   - Firebase Realtime Database
   - Cloud Firestore

### 3. Build and Run

```bash
# Open in Android Studio
# File > Open > Select BloodBank folder

# Or via command line:
./gradlew assembleDebug
./gradlew installDebug
```

## 📁 Project Structure

```
app/src/main/java/com/example/bloodbank/
├── Model/                  # Data models (Kotlin data classes)
├── repository/             # Repository interfaces & implementations
├── di/                     # Hilt dependency injection modules
├── worker/                 # WorkManager background tasks
├── Adapter/                # RecyclerView adapters
├── Util/                   # Utility classes & helpers
├── *Activity.kt            # UI Activities (MVVM pattern)
├── *ViewModel.kt           # ViewModels (business logic)
└── BloodBankApplication.kt # Application class
```

## 🎯 Key Components

### ViewModels

All ViewModels use `StateFlow` for reactive state management:

```kotlin
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
}
```

### Repositories

Repositories use Kotlin `Flow` for async data streams:

```kotlin
interface MessageRepository {
    fun getMessages(chatId: String): Flow<Result<List<Message>>>
    fun sendMessage(message: Message): Flow<Result<Unit>>
}
```

### Activities

Activities observe ViewModels using `lifecycleScope`:

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.messages.collect { messages ->
            adapter.updateMessages(messages)
        }
    }
}
```

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 📱 Minimum Requirements

- **Android Version**: 7.0 (API 24) or higher
- **RAM**: 2GB minimum
- **Storage**: 50MB
- **Permissions**: Location, Internet, Notifications

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.

## 📄 License

[Add your license here]

## 👥 Authors

[Add author information]

## 🙏 Acknowledgments

- Firebase for backend services
- Material Design for UI components
- Kotlin community for excellent tooling

---

**Note**: This project was successfully migrated from Java to Kotlin with modern Android architecture patterns (MVVM, Repository Pattern, Coroutines, Hilt).
