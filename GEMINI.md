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

## Architecture & Packaging
We follow a Clean Architecture / MVVM separation style. Code should be separated into two primary packages to ensure proper test filtering and maintainability:
- **`...presentation`**: This package contains ViewModels, UI state models, and presentation logic. These are purely logical components without Compose dependencies.
- **`...ui`**: This package contains all Composable functions (screens, components, layouts) and Compose-specific resources. 

*Rule of thumb:* If it draws pixels on the screen (Composables), it goes in `ui`. If it manages state and talks to repositories (ViewModels), it goes in `presentation`.

## UI Guidelines
- **No Hardcoded Strings**: Never hardcode UI display strings directly in Kotlin/Compose code. Always use Compose Multiplatform resources defined in `shared/src/commonMain/composeResources/values/strings.xml` (and appropriate localized files like `values-ru/strings.xml`) and reference them via `stringResource(Res.string.your_string_name)`.