# Refactoring AppViewModel and Introducing DrawerViewModel Design Specification

## Overview

Refactor `AppRouterViewModel` to adhere strictly to the Single Responsibility Principle (SRP) by scoping it purely to root route navigation (`ScreenState`). Extract user profile state and drawer/account operations (such as sign out) into a dedicated `DrawerViewModel` in the `presentation.drawer` package.

---

## 1. Architectural Goals & Motivations

- **Single Responsibility for `AppRouterViewModel`**: `AppRouterViewModel` is solely responsible for application-level navigation (`currentScreen: StateFlow<ScreenState>`) and reacting to authentication state changes to switch screens.
- **Decoupling from `AuthRepository`**: `AppRouterViewModel` no longer depends on the full `AuthRepository` (which contains mutating auth actions like `signInWithGoogle` and `signOut`), but instead receives a lightweight `Flow<AuthState>`.
- **Dedicated Drawer & Account State Holder**: `DrawerViewModel` manages the user profile state (`userProfile: StateFlow<UserProfile?>`) and account actions (`signOut()`, and in the future, account deletion).
- **Strict Unidirectional Data Flow (UDF)**:
  - `App.kt` remains the route orchestrator collecting states from both `AppRouterViewModel` and `DrawerViewModel`.
  - `AppDrawerContent` remains a pure UI composable that receives immutable state and callback lambdas without direct ViewModel dependencies.
- **Testing Standard**: New tests will use Mokkery for mocking, while existing tests maintain their existing structure.

---

## 2. Component Specifications

### 2.1 `AppRouterViewModel` (`com.blbulyandavbulyan.larm.kmp.presentation.global.AppViewModel.kt`)

- **Constructor**:
  ```kotlin
  class AppViewModel(
      private val authStateFlow: Flow<AuthState> = AppModule.authRepository.observeAuthState()
  ) : ViewModel()
  ```
- **State**:
  - `val currentScreen: StateFlow<ScreenState>` (initialized to `ScreenState.Generator`)
- **Lifecycle / Observation**:
  - Collects `authStateFlow`:
    - `AuthState.AUTHENTICATED`: If current screen is `ScreenState.Login` or `ScreenState.Loading`, sets `_currentScreen.value = ScreenState.Generator`.
    - `AuthState.UNAUTHENTICATED`: Sets `_currentScreen.value = ScreenState.Login`.
    - `AuthState.LOADING`: Sets `_currentScreen.value = ScreenState.Loading`.
- **Navigation Methods**:
  - `fun navigateToSearch()`: Transitions to `ScreenState.Search`.
  - `fun navigateToLoading()`: Transitions to `ScreenState.Loading`.
  - `fun navigateToGenerator()`: Transitions to `ScreenState.Generator`.
  - `fun navigateToDetail(dialogue: Dialogue)`: Transitions to `ScreenState.Detail(dialogue)`.
  - `fun navigateToLogin()`: Transitions to `ScreenState.Login`.
- **Removed**:
  - `_userProfile` / `userProfile` StateFlow.
  - `signOut()` method.
  - Direct dependency on `AuthRepository`.

---

### 2.2 `DrawerViewModel` (`com.blbulyandavbulyan.larm.kmp.presentation.drawer.DrawerViewModel.kt`)

- **Constructor**:
  ```kotlin
  class DrawerViewModel(
      private val authRepository: AuthRepository = AppModule.authRepository
  ) : ViewModel()
  ```
- **State**:
  - `val userProfile: StateFlow<UserProfile?>` (initialized to `null`, populated by collecting `authRepository.observeUserProfile()`).
- **Methods**:
  - `fun signOut()`: Launches a coroutine in `viewModelScope` to call `authRepository.signOut()`.

---

### 2.3 `App.kt` Orchestration (`com.blbulyandavbulyan.larm.kmp.App.kt`)

- **Composable Signature**:
  ```kotlin
  @Composable
  fun App(
      appViewModel: AppViewModel = remember { AppViewModel() },
      drawerViewModel: DrawerViewModel = remember { DrawerViewModel(AppModule.authRepository) },
      searchViewModel: DialogueSearchViewModel = remember { ... },
      chatViewModel: DialogueChatViewModel = remember { ... },
      loginViewModel: LoginViewModel = remember { ... }
  )
  ```
- **State Collection**:
  - `val currentScreen by appViewModel.currentScreen.collectAsStateWithLifecycle()`
  - `val userProfile by drawerViewModel.userProfile.collectAsStateWithLifecycle()`
  - `val appError by AppModule.globalErrorManager.currentError.collectAsStateWithLifecycle()`
- **Drawer Binding**:
  - `AppDrawerContent` is passed:
    - `userProfile = userProfile`
    - `currentScreen = currentScreen`
    - `onNavigateToGenerator = { coroutineScope.launch { drawerState.close() }; appViewModel.navigateToGenerator() }`
    - `onSignOut = { coroutineScope.launch { drawerState.close() }; drawerViewModel.signOut() }`

---

## 3. Testing Plan

### 3.1 `AppRouterViewModelTest` (`com.blbulyandavbulyan.larm.kmp.presentation.global.AppViewModelTest.kt`)
- Updated to pass `MutableStateFlow<AuthState>` into `AppRouterViewModel`.
- Tests:
  1. `navigation state defaults and updates correctly via manual methods`
  2. `auth state UNAUTHENTICATED sets screen to Login`
  3. `auth state AUTHENTICATED switches from Login to Generator`
  4. `auth state AUTHENTICATED does not override Search screen`
  5. `auth state LOADING sets screen to Loading`

### 3.2 `DrawerViewModelTest` (`com.blbulyandavbulyan.larm.kmp.presentation.drawer.DrawerViewModelTest.kt`)
- New test class using **Mokkery** for mocking `AuthRepository`.
- Tests:
  1. `userProfile updates when repository emits profile`
  2. `signOut invokes repository signOut`

### 3.3 `AppTest` (`com.blbulyandavbulyan.larm.kmp.AppTest.kt`)
- Update `AppTest` composable invocation to provide `drawerViewModel`.
- Maintain all existing Compose UI navigation and drawer integration tests.

---

## 4. Verification

1. Multiplatform Unit Tests: `./gradlew check` / `./gradlew wasmJsTest` / `./gradlew jvmTest`
2. Static Analysis: `./gradlew detekt`
