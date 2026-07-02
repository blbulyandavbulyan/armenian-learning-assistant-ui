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