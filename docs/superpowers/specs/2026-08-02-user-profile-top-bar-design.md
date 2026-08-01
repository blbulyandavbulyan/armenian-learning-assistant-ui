# Universal User Profile & App Top Bar Design

## 1. Overview & Goal
Add an application-wide Top Bar with centered title and a universal user profile avatar in the top right. Clicking the avatar expands a dropdown menu displaying the logged-in user's profile picture, name, and email, alongside a "Sign Out" button. The solution relies entirely on standard Supabase user metadata and works universally across all authentication providers.

## 2. Global Constraints
- Target Kotlin: 2.4.0
- Web-First (WASM & JS) + JVM compatibility
- Pure MVVM / UDF separation:
  - Domain models & repositories in `com.blbulyandavbulyan.larm.kmp.domain.auth`
  - Infrastructure repository implementations in `com.blbulyandavbulyan.larm.kmp.infrastructure.auth.suppabase`
  - ViewModels in `com.blbulyandavbulyan.larm.kmp.presentation`
  - Composables in `com.blbulyandavbulyan.larm.kmp.ui`
- No hardcoded strings: all strings in `strings.xml` and `values-ru/strings.xml`
- Strict Kotest assertions for tests

## 3. Data & Domain Architecture

### 3.1 Domain Model
`com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile`:
```kotlin
data class UserProfile(
    val id: String,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?
)
```

### 3.2 Repository Interface
`com.blbulyandavbulyan.larm.kmp.domain.auth.AuthRepository`:
```kotlin
interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    fun observeUserProfile(): Flow<UserProfile?>
    suspend fun signInWithGoogle()
    suspend fun signOut()
    fun getCurrentAccessToken(): String?
}
```

### 3.3 Supabase Implementation
In `com.blbulyandavbulyan.larm.kmp.infrastructure.auth.suppabase.SupabaseAuthRepository`:
- Map `supabaseClient.auth.sessionStatus`:
  - `SessionStatus.Authenticated(session)` -> extracts `session.user` to `UserProfile`:
    - `displayName`: `userMetadata["full_name"]?.jsonPrimitive?.contentOrNull ?: userMetadata["name"]?.jsonPrimitive?.contentOrNull ?: userMetadata["preferred_username"]?.jsonPrimitive?.contentOrNull ?: user.email`
    - `avatarUrl`: `userMetadata["avatar_url"]?.jsonPrimitive?.contentOrNull ?: userMetadata["picture"]?.jsonPrimitive?.contentOrNull`
    - `email`: `user.email`
    - `id`: `user.id`
  - Any non-authenticated status -> `null`

## 4. Presentation Architecture

### 4.1 AppViewModel
In `com.blbulyandavbulyan.larm.kmp.presentation.global.AppViewModel`:
- `val userProfile: StateFlow<UserProfile?>`: collects `authRepository.observeUserProfile()`.
- `fun signOut()`: launches coroutine in `viewModelScope`, invokes `authRepository.signOut()`, and navigates to `ScreenState.Login`.

## 5. UI Architecture

### 5.1 Async Image Loader / Initials Fallback
`com.blbulyandavbulyan.larm.kmp.ui.common.AvatarImage`:
- If `avatarUrl` is non-blank, attempts to fetch image bytes and render with Skiko `ImageBitmap`.
- If loading, empty, or on error: renders a stylized circular avatar with initials (e.g. first letters of display name or email) with theme primary color.

### 5.2 UserProfileMenu Composable
`com.blbulyandavbulyan.larm.kmp.ui.auth.UserProfileMenu`:
- Pure UI composable:
  ```kotlin
  @Composable
  fun UserProfileMenu(
      userProfile: UserProfile?,
      onSignOut: () -> Unit,
      modifier: Modifier = Modifier
  )
  ```
- Renders 36dp circular avatar button.
- Clicking opens a Material3 `DropdownMenu` with:
  - User avatar & display name (headline/bold).
  - Email address (muted secondary text).
  - Horizontal divider.
  - "Sign Out" item with icon and click handler `onSignOut`.

### 5.3 AppTopBar Composable
`com.blbulyandavbulyan.larm.kmp.ui.common.AppTopBar`:
- Rendered in `App.kt` when `currentScreen` is an authenticated screen (`ScreenState.Generator`, `ScreenState.Search`, `ScreenState.Detail`).
- Center: App title (localized).
- Right: `UserProfileMenu`.

## 6. Localization Strings
`shared/src/commonMain/composeResources/values/strings.xml`:
- `app_title` = "Armenian Learning Assistant"
- `action_sign_out` = "Sign Out"
- `profile_anonymous_user` = "User"
- `profile_no_email` = "No email"

`shared/src/commonMain/composeResources/values-ru/strings.xml`:
- `app_title` = "Помощник изучения армянского"
- `action_sign_out` = "Выйти"
- `profile_anonymous_user` = "Пользователь"
- `profile_no_email` = "Без email"

## 7. Testing & Verification
1. **Repository Unit Tests (`SessionStatusMappersTest.kt`)**:
   - Verify `observeUserProfile` extracts standard Supabase `UserInfo` and metadata fields correctly.
   - Verify fallback logic when name/avatar are null or missing.
2. **ViewModel Unit Tests (`AppViewModelTest.kt`)**:
   - Verify `userProfile` StateFlow updates when auth state / session changes.
   - Verify `signOut()` invokes repository signOut and navigates to `ScreenState.Login`.
3. **UI Compose Tests (`UserProfileMenuTest.kt`, `AppTopBarTest.kt`)**:
   - Verify avatar displays initials/fallback.
   - Verify clicking avatar expands menu with name, email, and sign-out button.
   - Verify clicking sign-out calls callback.
