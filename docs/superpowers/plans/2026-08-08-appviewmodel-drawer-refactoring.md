# AppViewModel and DrawerViewModel Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor `AppViewModel` to purely handle global screen navigation via `Flow<AuthState>`, and extract user profile state and sign out logic into a new `DrawerViewModel`.

**Architecture:** MVVM / Clean Architecture separation in KMP. `AppViewModel` in `presentation.global` manages root navigation (`ScreenState`) without knowledge of `AuthRepository` actions or profile data. `DrawerViewModel` in `presentation.drawer` manages side-drawer profile and sign-out actions. `App.kt` composes both ViewModels and drives pure UI components via UDF.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose Multiplatform, Mokkery (for mocking), Kotest matchers, Kotlinx Coroutines / Turbine.

## Global Constraints

- Never hardcode strings in UI; use Compose Resources.
- Always use Kotest assertions (`shouldBe`, `shouldBeInstanceOf`) instead of JUnit assertions.
- Non-UI presentation logic / ViewModels must reside in `presentation` packages.
- Dumb UI components (`AppDrawerContent`) must remain pure functions receiving primitives/lambdas without direct ViewModel dependencies.
- New tests must use **Mokkery** for mocking instead of manual fakes.

---

### Task 1: Create `DrawerViewModel` and its Tests

**Files:**
- Create: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/drawer/DrawerViewModel.kt`
- Test: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/drawer/DrawerViewModelTest.kt`

**Interfaces:**
- Consumes: `AuthRepository` (`observeUserProfile(): Flow<UserProfile?>`, `signOut(): suspend () -> Unit`)
- Produces: `DrawerViewModel(authRepository: AuthRepository = AppModule.authRepository)` with `val userProfile: StateFlow<UserProfile?>` and `fun signOut()`

- [ ] **Step 1: Write the failing unit test for `DrawerViewModel` using Mokkery**

Create `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/drawer/DrawerViewModelTest.kt`:

```kotlin
package com.blbulyandavbulyan.larm.kmp.presentation.drawer

import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthRepository
import com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DrawerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val authRepository = mock<AuthRepository>()
    private val userProfileFlow = MutableStateFlow<UserProfile?>(null)
    private lateinit var viewModel: DrawerViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.observeUserProfile() } returns userProfileFlow
        everySuspend { authRepository.signOut() } returns Unit
        viewModel = DrawerViewModel(authRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `userProfile updates when repository emits profile`() = runTest {
        viewModel.userProfile.value shouldBe null

        val testProfile = UserProfile(
            id = "test_user_1",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = "https://example.com/avatar.png"
        )
        userProfileFlow.value = testProfile
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.userProfile.value shouldBe testProfile
    }

    @Test
    fun `signOut invokes repository signOut`() = runTest {
        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        verifySuspend { authRepository.signOut() }
    }
}
```

- [ ] **Step 2: Run test to verify it fails compilation**

Run: `./gradlew :shared:wasmJsTest --tests "com.blbulyandavbulyan.larm.kmp.presentation.drawer.DrawerViewModelTest"`
Expected: FAIL due to Unresolved reference `DrawerViewModel`

- [ ] **Step 3: Write `DrawerViewModel` implementation**

Create `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/drawer/DrawerViewModel.kt`:

```kotlin
package com.blbulyandavbulyan.larm.kmp.presentation.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blbulyandavbulyan.larm.kmp.di.AppModule
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthRepository
import com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DrawerViewModel(
    private val authRepository: AuthRepository = AppModule.authRepository
) : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeUserProfile().collect { profile ->
                _userProfile.value = profile
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :shared:wasmJsTest --tests "com.blbulyandavbulyan.larm.kmp.presentation.drawer.DrawerViewModelTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/drawer/DrawerViewModel.kt shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/drawer/DrawerViewModelTest.kt
git commit -m "feat: add DrawerViewModel and tests"
```

---

### Task 2: Refactor `AppViewModel` and its Tests

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/global/AppViewModel.kt`
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/global/AppViewModelTest.kt`

**Interfaces:**
- Consumes: `Flow<AuthState>`
- Produces: `AppViewModel(authStateFlow: Flow<AuthState> = AppModule.authRepository.observeAuthState())` with `currentScreen: StateFlow<ScreenState>` and navigation methods (`navigateToSearch`, `navigateToLoading`, `navigateToGenerator`, `navigateToDetail`, `navigateToLogin`)

- [ ] **Step 1: Update `AppViewModelTest` to test pure navigation and `Flow<AuthState>`**

Update `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/global/AppViewModelTest.kt`:

```kotlin
package com.blbulyandavbulyan.larm.kmp.presentation.global

import app.cash.turbine.test
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthState
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DomainMothers
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val authStateFlow = MutableStateFlow(AuthState.UNAUTHENTICATED)
    private lateinit var viewModel: AppViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AppViewModel(authStateFlow)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `navigation state defaults and updates correctly via manual methods`() = runTest {
        viewModel.currentScreen.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem().shouldBeInstanceOf<ScreenState.Login>()

            viewModel.navigateToSearch()
            awaitItem().shouldBeInstanceOf<ScreenState.Search>()

            viewModel.navigateToLoading()
            awaitItem().shouldBeInstanceOf<ScreenState.Loading>()

            val fakeDialogue = DomainMothers.DIALOGUE_1
            viewModel.navigateToDetail(fakeDialogue)
            awaitItem().shouldBeInstanceOf<ScreenState.Detail>().dialogue shouldBe fakeDialogue

            viewModel.navigateToGenerator()
            awaitItem().shouldBeInstanceOf<ScreenState.Generator>()

            viewModel.navigateToLogin()
            awaitItem().shouldBeInstanceOf<ScreenState.Login>()
        }
    }

    @Test
    fun `auth state UNAUTHENTICATED sets screen to Login`() = runTest {
        authStateFlow.value = AuthState.UNAUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.currentScreen.value shouldBe ScreenState.Login
    }

    @Test
    fun `auth state AUTHENTICATED switches from Login to Generator`() = runTest {
        authStateFlow.value = AuthState.UNAUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.currentScreen.value shouldBe ScreenState.Login

        authStateFlow.value = AuthState.AUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.currentScreen.value shouldBe ScreenState.Generator
    }

    @Test
    fun `auth state AUTHENTICATED does not override Search screen`() = runTest {
        authStateFlow.value = AuthState.UNAUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.navigateToSearch()
        viewModel.currentScreen.value shouldBe ScreenState.Search

        authStateFlow.value = AuthState.AUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.currentScreen.value shouldBe ScreenState.Search
    }

    @Test
    fun `auth state LOADING sets screen to Loading`() = runTest {
        authStateFlow.value = AuthState.LOADING
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.currentScreen.value shouldBe ScreenState.Loading
    }
}
```

- [ ] **Step 2: Run test to verify failure against current `AppViewModel` signature**

Run: `./gradlew :shared:wasmJsTest --tests "com.blbulyandavbulyan.larm.kmp.presentation.global.AppViewModelTest"`
Expected: FAIL due to type mismatch in constructor

- [ ] **Step 3: Refactor `AppViewModel.kt`**

Update `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/global/AppViewModel.kt`:

```kotlin
package com.blbulyandavbulyan.larm.kmp.presentation.global

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blbulyandavbulyan.larm.kmp.di.AppModule
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthState
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Dialogue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val authStateFlow: Flow<AuthState> = AppModule.authRepository.observeAuthState()
) : ViewModel() {
    private val _currentScreen = MutableStateFlow<ScreenState>(ScreenState.Generator)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    init {
        viewModelScope.launch {
            authStateFlow.collect { authState ->
                when (authState) {
                    AuthState.AUTHENTICATED -> {
                        if (_currentScreen.value is ScreenState.Login || _currentScreen.value is ScreenState.Loading) {
                            _currentScreen.value = ScreenState.Generator
                        }
                    }
                    AuthState.UNAUTHENTICATED -> {
                        _currentScreen.value = ScreenState.Login
                    }
                    AuthState.LOADING -> {
                        _currentScreen.value = ScreenState.Loading
                    }
                }
            }
        }
    }

    fun navigateToSearch() {
        _currentScreen.value = ScreenState.Search
    }

    fun navigateToLoading() {
        _currentScreen.value = ScreenState.Loading
    }

    fun navigateToGenerator() {
        _currentScreen.value = ScreenState.Generator
    }

    fun navigateToDetail(dialogue: Dialogue) {
        _currentScreen.value = ScreenState.Detail(dialogue)
    }

    fun navigateToLogin() {
        _currentScreen.value = ScreenState.Login
    }
}
```

- [ ] **Step 4: Run `AppViewModelTest` to verify it passes**

Run: `./gradlew :shared:wasmJsTest --tests "com.blbulyandavbulyan.larm.kmp.presentation.global.AppViewModelTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/global/AppViewModel.kt shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/global/AppViewModelTest.kt
git commit -m "refactor: simplify AppViewModel to pure navigation with Flow<AuthState>"
```

---

### Task 3: Update `App.kt` and `AppTest.kt` Integration

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/App.kt`
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/AppTest.kt`

**Interfaces:**
- Consumes: `AppViewModel`, `DrawerViewModel`, `DialogueSearchViewModel`, `DialogueChatViewModel`, `LoginViewModel`
- Produces: `App(...)` composable rendering drawer, top bar, and active screen

- [ ] **Step 1: Update `App.kt` to inject and observe `DrawerViewModel`**

In `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/App.kt`:
1. Import `com.blbulyandavbulyan.larm.kmp.presentation.drawer.DrawerViewModel`.
2. Add `drawerViewModel: DrawerViewModel = remember { DrawerViewModel(AppModule.authRepository) }` to `App(...)` parameter list.
3. Collect `val userProfile by drawerViewModel.userProfile.collectAsStateWithLifecycle()`.
4. In `MainScaffold`:
   - Bind `onSignOut = { coroutineScope.launch { drawerState.close() }; drawerViewModel.signOut() }`.
   - Update `MainScaffold` and `Content` signatures to accept `drawerViewModel: DrawerViewModel` (or pass `onSignOut`).

- [ ] **Step 2: Update `AppTest.kt` test helpers and assertions**

In `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/AppTest.kt`:
1. Import `com.blbulyandavbulyan.larm.kmp.presentation.drawer.DrawerViewModel`.
2. Update `createAuthenticatedAppViewModel()` to return `AppViewModel(MutableStateFlow(AuthState.AUTHENTICATED))`.
3. In `app_showsLoginScreen_whenUnauthenticated()`, instantiate `AppViewModel(MutableStateFlow(AuthState.UNAUTHENTICATED))` and `DrawerViewModel(authRepository)`.
4. In `app_showsTopBarAndDrawer_whenAuthenticated()`:
   - Provide `AppViewModel(authStateFlow)` and `DrawerViewModel(authRepository)`.
   - Ensure sign out test asserts drawer sign-out properly invokes `drawerViewModel.signOut()`.
5. In `app_hidesTopBar_whenUnauthenticated()`, update to `AppViewModel(MutableStateFlow(AuthState.UNAUTHENTICATED))`.

- [ ] **Step 3: Run full multiplatform test suite and detekt**

Run: `./gradlew check`
Run: `./gradlew detekt`
Expected: All tests pass, detekt passes with 0 issues.

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/App.kt shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/AppTest.kt
git commit -m "refactor: integrate DrawerViewModel into App.kt and update UI tests"
```
