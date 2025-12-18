# Contributing to BloodBank

Thank you for your interest in contributing! This guide will help you understand our development workflow and coding standards.

## 🎯 Development Philosophy

This project follows the **"ultrathink" philosophy**: elegant, intuitive, and robust solutions. Every contribution should prioritize:

1. **Code Clarity**: Self-documenting code over comments
2. **Modern Patterns**: Kotlin idioms and Android best practices
3. **Type Safety**: Leverage Kotlin's type system
4. **Testability**: Write testable, modular code

## 🏗️ Architecture Guidelines

### MVVM Pattern

All features must follow MVVM architecture:

```
Activity/Fragment → ViewModel → Repository → Data Source
```

**Rules:**

- Activities should only handle UI logic
- ViewModels contain business logic
- Repositories abstract data sources
- No direct Firebase calls in ViewModels

### Repository Pattern

```kotlin
// ✅ Good: Interface-based repository
interface UserRepository {
    fun getUser(userId: String): Flow<Result<User>>
}

// ❌ Bad: Direct Firebase in ViewModel
class MyViewModel : ViewModel() {
    fun loadUser() {
        FirebaseDatabase.getInstance()... // Don't do this!
    }
}
```

## 📝 Code Style

### Kotlin Conventions

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// ✅ Good: Concise, idiomatic Kotlin
val users = userList.filter { it.isActive }
    .map { it.name }
    .sorted()

// ❌ Bad: Java-style Kotlin
val users = ArrayList<String>()
for (user in userList) {
    if (user.isActive) {
        users.add(user.name)
    }
}
Collections.sort(users)
```

### Naming Conventions

- **Classes**: `PascalCase` (e.g., `UserRepository`)
- **Functions**: `camelCase` (e.g., `getUserById`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_COUNT`)
- **Private fields**: `_camelCase` for MutableStateFlow (e.g., `_users`)

### File Organization

```kotlin
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel() {

    // 1. Companion object (if needed)
    companion object {
        private const val TAG = "MyViewModel"
    }

    // 2. Private mutable state
    private val _state = MutableStateFlow<UiState>(UiState.Loading)

    // 3. Public immutable state
    val state: StateFlow<UiState> = _state.asStateFlow()

    // 4. Init block
    init {
        loadData()
    }

    // 5. Public functions
    fun refresh() { }

    // 6. Private functions
    private fun loadData() { }
}
```

## 🔄 State Management

### Use StateFlow for UI State

```kotlin
// ✅ Good: StateFlow with sealed class
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<User>) : UiState()
    data class Error(val message: String) : UiState()
}

private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

### Collect Flows Safely

```kotlin
// ✅ Good: Lifecycle-aware collection
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            updateUI(state)
        }
    }
}

// ❌ Bad: Unsafe collection
lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        updateUI(state) // Continues in background!
    }
}
```

## 🧪 Testing Guidelines

### ViewModel Tests

```kotlin
@Test
fun `when user loads, should emit success state`() = runTest {
    // Given
    val expectedUser = User(id = "1", name = "Test")
    coEvery { repository.getUser("1") } returns flowOf(Result.Success(expectedUser))

    // When
    viewModel.loadUser("1")

    // Then
    assertEquals(UiState.Success(expectedUser), viewModel.uiState.value)
}
```

### Repository Tests

```kotlin
@Test
fun `getUser should return user from Firebase`() = runTest {
    // Test implementation
}
```

## 🎨 UI Guidelines

### Material Design

- Use Material Components
- Follow Material Design 3 guidelines
- Maintain consistent spacing (8dp grid)

### Accessibility

- Provide content descriptions for images
- Ensure minimum touch target size (48dp)
- Support dynamic text sizing

## 🔧 Dependency Injection

### Use Hilt

```kotlin
// ✅ Good: Constructor injection
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel()

// Module for dependencies
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideMyRepository(
        firebase: FirebaseDatabase
    ): MyRepository = MyRepositoryImpl(firebase)
}
```

## 📦 Pull Request Process

1. **Create a feature branch**: `feature/your-feature-name`
2. **Follow commit conventions**:
   ```
   feat: Add user profile screen
   fix: Resolve chat message duplication
   refactor: Extract validation logic to helper
   docs: Update README with setup instructions
   ```
3. **Write tests** for new features
4. **Update documentation** if needed
5. **Ensure build passes**: `./gradlew build`
6. **Create PR** with clear description

## 🚫 Common Pitfalls to Avoid

### ❌ Don't Use `!!` (Double Bang)

```kotlin
// ❌ Bad: Crashes if null
val name = user!!.name

// ✅ Good: Safe handling
val name = user?.name ?: "Unknown"
```

### ❌ Don't Ignore Coroutine Scope

```kotlin
// ❌ Bad: GlobalScope
GlobalScope.launch { }

// ✅ Good: Proper scope
viewModelScope.launch { }
```

### ❌ Don't Mix Async Patterns

```kotlin
// ❌ Bad: Mixing callbacks with coroutines
fun loadData(callback: (Result) -> Unit) {
    viewModelScope.launch {
        val result = repository.getData()
        callback(result)
    }
}

// ✅ Good: Pure coroutines
suspend fun loadData(): Result {
    return repository.getData()
}
```

## 📚 Resources

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Android Developer Guide](https://developer.android.com/guide)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Hilt Documentation](https://dagger.dev/hilt/)

## 💬 Questions?

Feel free to open an issue for:

- Bug reports
- Feature requests
- Architecture questions
- General discussions

---

**Remember**: Quality over quantity. Take time to write clean, maintainable code that follows our architectural patterns.
