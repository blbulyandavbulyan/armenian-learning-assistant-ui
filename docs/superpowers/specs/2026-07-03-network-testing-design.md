# Design Specification: Network Package Testing

## Objective
Introduce tests for the `network` package (`ApiClient.kt` and `DialogueRepository.kt`) to ensure that API requests are properly constructed and responses are accurately parsed. The solution must remain "Kotlionic" and Kotlin Multiplatform-compatible.

## Architecture & Design
We will use Ktor's native `MockEngine` (`ktor-client-mock`) to intercept HTTP requests in memory. This avoids running an actual HTTP server, ensuring tests remain fast and compatible with non-JVM targets like Wasm.

To achieve clean testability, we will use **Inversion of Control** instead of `ApiClient` constructing its own Ktor client.

1. **Dependency Injection Refactor**:
   Update `ApiClient` to accept a pre-configured `HttpClient`. Move the client configuration (ContentNegotiation, defaultRequest) to the call site in `App.kt`.
   ```kotlin
   class ApiClient(private val client: HttpClient) {
       // Only handles endpoints and serialization/deserialization logic
   }
   ```
2. **Mock Dependency**:
   Add `ktor-client-mock` to `gradle/libs.versions.toml` and configure it in `shared/build.gradle.kts` under `commonTest`.
3. **`ApiClientTest`**:
   Verify that `ApiClient.generateDialogue()` sends the correct JSON payload (containing `message` and `chatId`) to `/chat/dialogue` and correctly parses a mock `DialogueChatResponse`. This test will instantiate its own `HttpClient` using `MockEngine`.
4. **`NetworkDialogueRepositoryTest`**:
   Verify that the repository accurately delegates calls to `ApiClient` and propagates the response without unintended alterations.

## Scope
- Modify `gradle/libs.versions.toml`
- Modify `shared/build.gradle.kts`
- Modify `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/ApiClient.kt`
- Modify `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/App.kt`
- Create `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/network/ApiClientTest.kt`
- Create `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/network/NetworkDialogueRepositoryTest.kt`
