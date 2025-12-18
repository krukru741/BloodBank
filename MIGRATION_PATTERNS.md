# Kotlin Migration Patterns

This document outlines the patterns and practices used during the Java to Kotlin migration of the BloodBank application.

## 🎯 Migration Overview

**Goal**: Transform Java codebase to modern Kotlin with MVVM architecture, Repository Pattern, and reactive programming.

**Approach**: Systematic phase-by-phase migration maintaining functionality while improving code quality.

## 📋 Migration Phases

### Phase 0: Build Configuration

- Added Kotlin Gradle plugin
- Configured `kotlinOptions` with JVM target 17
- Added Kotlin dependencies (stdlib, coroutines, lifecycle-ktx)

### Phase 1-2: Data Layer

- Migrated models to Kotlin `data class`
- Created Repository interfaces with `Flow` return types
- Implemented Firebase repositories using `callbackFlow`

### Phase 3-5: Presentation Layer

- Created ViewModels with `StateFlow`
- Migrated Activities to observe ViewModels
- Converted Adapters to Kotlin with update methods

### Phase 6-11: Remaining Components

- Completed all activities and adapters
- Migrated utility classes to `object` singletons
- Replaced AlarmManager with WorkManager

## 🔄 Common Migration Patterns

### 1. Data Classes

**Before (Java)**:

```java
public class Message {
    private String id;
    private String senderId;
    private String content;

    public Message() {} // Firebase requires

    public Message(String id, String senderId, String content) {
        this.id = id;
        this.senderId = senderId;
        this.content = content;
    }

    // Getters and setters...
}
```

**After (Kotlin)**:

```kotlin
@Keep
data class Message(
    val id: String = "",
    val senderId: String = "",
    val content: String = ""
)
```

**Benefits**: 90% less code, automatic `equals()`, `hashCode()`, `toString()`

### 2. Repository Pattern with Flow

**Before (Java)**:

```java
public interface MessageRepository {
    void getMessages(String chatId, Callback<List<Message>> callback);
}

public class FirebaseMessageRepository implements MessageRepository {
    @Override
    public void getMessages(String chatId, Callback<List<Message>> callback) {
        database.child("messages").child(chatId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<Message> messages = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        messages.add(child.getValue(Message.class));
                    }
                    callback.onSuccess(messages);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
}
```

**After (Kotlin)**:

```kotlin
interface MessageRepository {
    fun getMessages(chatId: String): Flow<Result<List<Message>>>
}

class FirebaseMessageRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : MessageRepository {

    override fun getMessages(chatId: String): Flow<Result<List<Message>>> = callbackFlow {
        val listener = database.reference.child("messages").child(chatId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.mapNotNull {
                        it.getValue(Message::class.java)
                    }
                    trySend(Result.Success(messages))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Result.Error(error.message))
                }
            })

        awaitClose { database.reference.child("messages").child(chatId).removeEventListener(listener) }
    }
}
```

**Benefits**: Reactive streams, automatic cleanup, type-safe error handling

### 3. ViewModel with StateFlow

**Before (Java)**:

```java
public class ChatViewModel extends ViewModel {
    private MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    private MessageRepository repository;

    public ChatViewModel(MessageRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Message>> getMessages() {
        return messages;
    }

    public void loadMessages(String chatId) {
        repository.getMessages(chatId, new Callback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> result) {
                messages.setValue(result);
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }
}
```

**After (Kotlin)**:

```kotlin
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            messageRepository.getMessages(chatId).collect { result ->
                when (result) {
                    is Result.Success -> _messages.value = result.data
                    is Result.Error -> _error.value = result.message
                }
            }
        }
    }
}
```

**Benefits**: Hilt injection, coroutines, reactive state, proper error handling

### 4. Activity Observation

**Before (Java)**:

```java
public class ChatActivity extends AppCompatActivity {
    private ChatViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        viewModel.getMessages().observe(this, messages -> {
            adapter.updateMessages(messages);
        });
    }
}
```

**After (Kotlin)**:

```kotlin
@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { messages ->
                    adapter.updateMessages(messages)
                }
            }
        }
    }
}
```

**Benefits**: Hilt injection, lifecycle-aware collection, no memory leaks

### 5. Utility Classes

**Before (Java)**:

```java
public class LocationHelper {
    private static final double MAX_DISTANCE_KM = 50.0;

    public static double calculateDistance(double lat1, double lon1,
                                          double lat2, double lon2) {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);

        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);

        return location1.distanceTo(location2) / 1000.0;
    }
}
```

**After (Kotlin)**:

```kotlin
object LocationHelper {
    private const val MAX_DISTANCE_KM = 50.0

    fun calculateDistance(lat1: Double, lon1: Double,
                         lat2: Double, lon2: Double): Double {
        val location1 = Location("").apply {
            latitude = lat1
            longitude = lon1
        }

        val location2 = Location("").apply {
            latitude = lat2
            longitude = lon2
        }

        return location1.distanceTo(location2) / 1000.0
    }
}
```

**Benefits**: Object singleton, `apply` scope function, concise syntax

### 6. Background Tasks

**Before (Java - AlarmManager)**:

```java
public class DonationReminderService {
    public void scheduleReminder(long time) {
        Intent intent = new Intent(context, DonationReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }
}
```

**After (Kotlin - WorkManager)**:

```kotlin
@HiltWorker
class DonationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            sendReminder()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

class DonationReminderScheduler(private val context: Context) {
    fun scheduleReminder(delay: Long) {
        val workRequest = OneTimeWorkRequestBuilder<DonationReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
```

**Benefits**: Modern API, automatic retry, battery-efficient, Hilt integration

## 🎨 Kotlin Idioms Used

### When Expressions

```kotlin
when (status) {
    "SCHEDULED" -> Color.GREEN
    "COMPLETED" -> Color.BLUE
    "CANCELLED" -> Color.RED
    else -> Color.GRAY
}
```

### Scope Functions

```kotlin
user?.let { u ->
    nameText.text = u.name
    emailText.text = u.email
}

Location("").apply {
    latitude = lat
    longitude = lon
}
```

### Extension Functions

```kotlin
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
```

### Sealed Classes

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}
```

## 📊 Migration Statistics

- **Code Reduction**: ~33% (18,000 → 12,000 lines)
- **Files Migrated**: 50+ Java files
- **Null Safety**: 100% null-safe code
- **Async Patterns**: Callbacks → Coroutines
- **Dependency Injection**: Manual → Hilt

## ✅ Best Practices Applied

1. **Immutability**: Prefer `val` over `var`
2. **Null Safety**: Use `?` and `?.` instead of `!!`
3. **Coroutines**: Use `viewModelScope` and `lifecycleScope`
4. **StateFlow**: Prefer over LiveData for new code
5. **Hilt**: Constructor injection everywhere
6. **Sealed Classes**: Type-safe state management
7. **Data Classes**: For models and DTOs
8. **Object Singletons**: For stateless utilities

---

This migration demonstrates how to modernize an Android codebase while maintaining functionality and improving code quality through Kotlin's powerful features.
