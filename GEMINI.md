# Gemini Context & Project Rules

This is the frontend UI project for the **Armenian Learning Assistant** application. 

## Current Project Scope
- It is a **Kotlin Multiplatform** (KMP) project.
- **Web-Only (for now)**: The Android and iOS targets have been temporarily deleted to avoid friction since the Android SDK is not currently installed, and the focus is solely on Web development (WASM and JS).
- **Future Targets**: When we are ready to target Android and iOS, the original generated template files and configurations can be restored from the git history. 

## Backend and API Specs
- The OpenAPI specs for the backend can be found in the root directory: `backend-api-docs.json`. This should be used to understand the available API endpoints and data models.
- The backend repository is located at: [armenian-learning-assistant-be](https://github.com/blbulyandavbulyan/armenian-learning-assistant-be).
 
 ## Testing Guidelines
- **Assertions**: Always use Kotest assertions (e.g., `import io.kotest.matchers.shouldBe`, `variable shouldBe expected`) instead of standard JUnit or kotlin.test assertions (`assertTrue`, `assertEquals`) for better readability and consistent style.
- **Compose UI Tests on JS Target**: Compose UI tests (using `runComposeUiTest`) require the Skiko WebAssembly binary to be loaded. The legacy `jsBrowserTest` environment does not handle this automatically. Therefore, UI tests in the `.ui` package are globally excluded from the JS target in `build.gradle.kts` (`excludeTestsMatching("*.ui.*")`). All non-UI tests (like ViewModels) must be placed in the `.presentation` package so they run across all platforms.
- **Mocking**: Should be done using **Mokkery** library 

## Architecture & Packaging
We follow a Clean Architecture / MVVM separation style. Code should be separated into two primary packages to ensure proper test filtering and maintainability:
- **`...presentation`**: This package contains ViewModels, UI state models, and presentation logic. These are purely logical components without Compose dependencies.
- **`...ui`**: This package contains all Composable functions (screens, components, layouts) and Compose-specific resources. 

*Rule of thumb:* If it draws pixels on the screen (Composables), it goes in `ui`. If it manages state and talks to repositories (ViewModels), it goes in `presentation`.

## Strict Unidirectional Data Flow (UDF) & Two-Tier Screen Architecture
- **CRITICAL: Never mutate pure UI components for asynchronous state**: Dumb, generic UI components (like a `SearchField`, custom buttons, etc.) must NEVER contain their own `isLoading` state or manage network feedback. They must remain pure inputs/outputs.
- **Global Loading & Navigation State**: Global screen-level navigation events and app-wide loading (like waiting for a dialogue to fetch before navigating to Detail) are managed at the top-level route/navigation layer (`AppViewModel` and `App.kt`).
- **Two-Tier Screen Architecture (Stateful Wrapper vs Stateless Screen)**:
  - **Stateful Route/Screen Wrapper**: Each screen provides a stateful Composable taking its `ViewModel` (and route navigation lambdas) — e.g. `DialogueSearchScreen(viewModel, onGetDialogueDetails)`. It is responsible for collecting the ViewModel's state and wiring ViewModel callbacks, delegating directly to the stateless overload. The wiring between screen and ViewModel MUST be tested in that screen's test suite (e.g., `DialogueSearchScreenTest`).
  - **Stateless Screen (Pure UI)**: An overloaded Composable taking only immutable state models and callback lambdas — e.g. `DialogueSearchScreen(searchState, onSearch, ...)`. This is used for rendering previews and testing every UI state branch without ViewModels.
  - **Top-Level `App.kt` Shell**: `App.kt` coordinates top-level screens (`ScreenState`), TopBar, and Drawer. It delegates screen rendering to each screen's stateful wrapper (passing the screen's ViewModel and external navigation callbacks) rather than manually unwrapping and inlining granular screen state and action closures in `App.kt`.

## UI Guidelines
- **No Hardcoded Strings**: Never hardcode UI display strings directly in Kotlin/Compose code. Always use Compose Multiplatform resources defined in `shared/src/commonMain/composeResources/values/strings.xml` (and appropriate localized files like `values-ru/strings.xml`) and reference them via `stringResource(Res.string.your_string_name)`.