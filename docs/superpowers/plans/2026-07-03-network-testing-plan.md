# Network Package Testing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement unit tests for the `network` package (`ApiClient` and `NetworkDialogueRepository`) by injecting Ktor's `MockEngine`.

**Architecture:** 
1. `ApiClient` will be refactored to accept a pre-configured `HttpClient` parameter to enable pure dependency injection, avoiding internal configurations.
2. We will add `ktor-client-mock` as a `commonTest` dependency.
3. Tests will define `MockEngine` responses in-memory to verify JSON serialization/deserialization and endpoint accuracy across all KMP platforms.

**Tech Stack:** Kotlin Multiplatform, Ktor Client, Kotest, `ktor-client-mock`

## Global Constraints
- Must remain compatible with Wasm and other KMP targets (no JVM-specific dependencies or servers like WireMock).
- Use Kotest assertions (e.g., `shouldBe`) instead of standard JUnit.
- Tests should be written in `shared/src/commonTest/kotlin/...`.

---

### Task 1: Add MockEngine Dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `shared/build.gradle.kts`

**Interfaces:**
- Consumes: Existing Ktor configurations
- Produces: Access to `ktor-client-mock` in the test environments

- [ ] **Step 1: Add to Version Catalog**

In `gradle/libs.versions.toml`, under `[libraries]`, add the mock dependency.

```toml
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }
```

- [ ] **Step 2: Add to build.gradle.kts**

In `shared/build.gradle.kts`, under `commonTest.dependencies`, implement the new library.

```kotlin
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotest.assertions.core)
            implementation(libs.turbine)
            implementation(libs.compose.uiTest)
            implementation(libs.ktor.client.mock)
        }
```

- [ ] **Step 3: Sync project**
Run `./gradlew :shared:dependencies --configuration commonTestCompileClasspath` to verify the dependency resolves correctly.
Expected: PASS

- [ ] **Step 4: Commit**
```bash
git add gradle/libs.versions.toml shared/build.gradle.kts
git commit -m "test: add ktor-client-mock dependency"
```

---

### Task 2: Refactor ApiClient for Dependency Injection

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/ApiClient.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/App.kt`

**Interfaces:**
- Produces: `ApiClient` now accepts a pre-configured `HttpClient` directly via its constructor.

- [ ] **Step 1: Update ApiClient Constructor and logic**

```kotlin
// In ApiClient.kt
package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.ChatRequest
import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiClient(private val client: HttpClient) {

    suspend fun generateDialogue(message: String, chatId: String): DialogueChatResponse {
        val requestBody = ChatRequest(message, chatId)
        val response = client.post("/chat/dialogue") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        return response.body()
    }
}
```

- [ ] **Step 2: Configure Client in App.kt**

```kotlin
// In shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/App.kt

// Add these imports at the top
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Update the apiClient instantiation to build and inject the HttpClient
        val httpClient = remember {
            HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    })
                }
                defaultRequest {
                    val baseUrl = BuildKonfig.BASE_URL
                    if (baseUrl.isNotBlank()) {
                        url(baseUrl)
                    }
                }
            }
        }
        val apiClient = remember { ApiClient(httpClient) }
```

- [ ] **Step 3: Verify Compilation**
Run `./gradlew :shared:compileCommonMainKotlinMetadata`
Expected: PASS

- [ ] **Step 4: Commit**
```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/ApiClient.kt shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/App.kt
git commit -m "refactor: use dependency injection for ApiClient HttpClient"
```

---

### Task 3: Test ApiClient

**Files:**
- Create: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/network/ApiClientTest.kt`

**Interfaces:**
- Consumes: `ApiClient` and `ktor-client-mock`.

- [ ] **Step 1: Write ApiClient test**

Create the file and write the test.

```kotlin
package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ApiClientTest {

    @Test
    fun `generateDialogue sends correct request and parses response`() = runTest {
        val mockEngine = MockEngine { request ->
            request.url.encodedPath shouldBe "/chat/dialogue"
            request.method shouldBe HttpMethod.Post
            request.headers[HttpHeaders.ContentType] shouldBe "application/json"
            
            respond(
                content = """{"response": "Barev!"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val apiClient = ApiClient(client = mockClient)
        
        val response = apiClient.generateDialogue(message = "Hello", chatId = "123")
        
        response shouldBe DialogueChatResponse(response = "Barev!")
    }
}
```

- [ ] **Step 2: Run the test**
Run `./gradlew :shared:jvmTest --tests "com.blbulyandavbulyan.larm.kmp.network.ApiClientTest"`
Expected: PASS

- [ ] **Step 3: Commit**
```bash
git add shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/network/ApiClientTest.kt
git commit -m "test: add ApiClient unit tests"
```

---

### Task 4: Test NetworkDialogueRepository

**Files:**
- Create: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/network/NetworkDialogueRepositoryTest.kt`

**Interfaces:**
- Consumes: `NetworkDialogueRepository`, `ApiClient`, and `ktor-client-mock`.

- [ ] **Step 1: Write NetworkDialogueRepository test**

Create the file and write the test.

```kotlin
package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class NetworkDialogueRepositoryTest {

    @Test
    fun `generateDialogue delegates to ApiClient correctly`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"response": "Mocked response"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val apiClient = ApiClient(client = mockClient)
        val repository = NetworkDialogueRepository(apiClient)
        
        val response = repository.generateDialogue(prompt = "Test prompt", chatId = "chat456")
        
        response shouldBe DialogueChatResponse(response = "Mocked response")
    }
}
```

- [ ] **Step 2: Run the test**
Run `./gradlew :shared:jvmTest --tests "com.blbulyandavbulyan.larm.kmp.network.NetworkDialogueRepositoryTest"`
Expected: PASS

- [ ] **Step 3: Commit**
```bash
git add shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/network/NetworkDialogueRepositoryTest.kt
git commit -m "test: add NetworkDialogueRepository unit tests"
```
