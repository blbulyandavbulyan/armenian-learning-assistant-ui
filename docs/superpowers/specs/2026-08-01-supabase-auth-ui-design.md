# Supabase Auth UI Integration Design

## Overview
Integrate a dedicated Login UI screen to handle Supabase Google Authentication for the Armenian Learning Assistant, and automatically attach the resulting Supabase JWT access token to all outgoing Ktor HTTP requests sent to the backend.

## 1. Architecture & State Management

**Domain / Data Layer**
- **`AuthRepository` (Interface)**: 
  - `fun observeAuthState(): Flow<AuthState>`
  - `suspend fun signInWithGoogle()`
  - `suspend fun signOut()`
  - `fun getCurrentAccessToken(): String?`
- **`SupabaseAuthRepository` (Impl)**: Uses the `SupabaseClient` (configured with the `Auth` plugin in `AppModule.kt`) to initiate the OAuth flow, observe `sessionStatus`, and supply the current access token.
- **`AppModule.kt`**: 
  - Exposes the `AuthRepository` as a manual dependency.
  - Updates `httpClient`'s `defaultRequest` block to dynamically append the `Authorization: Bearer <token>` header on every backend request using `supabaseClient.auth.currentAccessTokenOrNull()`.

**Presentation Layer**
- **`LoginViewModel`**: 
  - Holds localized UI states like loading and error handling specific to the login form.
  - Exposes an action `signInWithGoogle()`.
- **`AppViewModel`**: 
  - Observes the global authentication state via the `AuthRepository`.
  - Maps an unauthenticated state to `ScreenState.Login`.
  - Automatically transitions to `ScreenState.Generator` once a session is established.
- **`ScreenState`**: Add `data object Login : ScreenState`.

## 2. Backend Token Interceptor

- In `AppModule.kt`, the Ktor `HttpClient` includes a default request header or plugin:
  ```kotlin
  defaultRequest {
      val baseUrl = BuildKonfig.API_URL
      if (baseUrl.isNotBlank()) {
          url(baseUrl)
      }
      val token = supabaseClient.auth.currentAccessTokenOrNull()
      if (!token.isNullOrBlank()) {
          header(HttpHeaders.Authorization, "Bearer $token")
      }
  }
  ```
- This ensures all existing repositories (`NetworkDialogueRepository`, `NetworkDialogueChatRepository`, `NetworkAssetRepository`) automatically send the valid Supabase JWT Bearer token on every backend API call.

## 3. UI Layer & Design

**`LoginScreen.kt`**
- Defined in `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/auth/LoginScreen.kt`.
- Features a clean, centered layout superimposed on the app's existing gradient background.
- Contains:
  - App Logo / Title.
  - Subtitle / Welcome Text.
  - A stylized `GoogleSignInButton` following standard Google branding (Google 'G' icon + text).
- Strictly follows Unidirectional Data Flow (UDF), receiving primitive states and emitting callback lambdas.

**Strings & Localization**
- All UI text is abstracted into Compose Multiplatform string resources located at `shared/src/commonMain/composeResources/values/strings.xml` (and `values-ru/strings.xml` for Russian equivalents).
- Keys include: `auth_app_title`, `auth_welcome_subtitle`, `auth_sign_in_with_google`, `auth_error_message`.

**Routing Integration**
- Added into the `Crossfade` navigation block in `App.kt`.
- When `currentScreen` evaluates to `ScreenState.Login`, `LoginScreen` is rendered.
- Error events from login attempts are funneled cleanly into `AppModule.globalErrorManager`.
