# Audio MP3 Support Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable KMP `AudioPlayer` to play MP3 files by passing the MIME type dynamically and adding JVM MP3 SPI support.

**Architecture:** We introduce a new `Audio` data class containing the raw bytes and MIME type. We update `AudioPlayer` and platform implementations to use this class, update network clients to parse `Content-Type`, and connect it all together in the ViewModels.

**Tech Stack:** Kotlin Multiplatform, Compose, Ktor, Java Sound SPI.

## Global Constraints

- Avoid breaking compilation between tasks. Implement interface changes and temporarily mock usages in the same task if needed.
- No wildcards in permissions, absolute paths only.

---

### Task 1: Core Audio Data Class and Player Implementation

We will define the `Audio` class, update the KMP `AudioPlayer` contract and all its platform-specific implementations, and add the JVM MP3 support dependency. To keep compilation passing, we will temporarily mock the usage in the view model.

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `shared/build.gradle.kts`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.kt`
- Modify: `shared/src/jvmMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.jvm.kt`
- Modify: `shared/src/jsMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.js.kt`
- Modify: `shared/src/wasmJsMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.wasmJs.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/search/DialogueSearchViewModel.kt`

- [ ] **Step 1: Add MP3 SPI Dependency to JVM**
Add `mp3spi = "1.9.5.4"` to the `[versions]` block and `mp3spi = { module = "com.googlecode.soundlibs:mp3spi", version.ref = "mp3spi" }` to the `[libraries]` block in `gradle/libs.versions.toml`. Then add `implementation(libs.mp3spi)` inside the `jvmMain.dependencies` block in `shared/build.gradle.kts`.

```kotlin
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.mp3spi)
        }
```

- [ ] **Step 2: Define `Audio` Data Class and Update Expected `AudioPlayer`**
Update `AudioPlayer.kt` to define `Audio` and modify the `play` method.

```kotlin
package com.blbulyandavbulyan.larm.kmp.audio

data class Audio(
    val data: ByteArray,
    val mimeType: String
)

expect class AudioPlayer() {
    suspend fun play(audio: Audio)
}
```

- [ ] **Step 3: Update JVM `AudioPlayer` Implementation**
Update `AudioPlayer.jvm.kt` to accept `audio: Audio` and use `audio.data`.

```kotlin
// ... inside actual class AudioPlayer ...
    @Suppress("TooGenericExceptionCaught", "kotlin:S6310")
    actual suspend fun play(audio: Audio) {
        var clip: Clip? = null
        try {
            withContext(Dispatchers.IO) {
                val audioInputStream = AudioSystem.getAudioInputStream(ByteArrayInputStream(audio.data))
// ... rest remains exactly the same ...
```

- [ ] **Step 4: Update JS `AudioPlayer` Implementation**
Update `AudioPlayer.js.kt` to accept `audio: Audio` and use `audio.mimeType` and `audio.data`.

```kotlin
// ... inside actual class AudioPlayer ...
    actual suspend fun play(audio: Audio) {
        var url: String? = null
        var audioEl: HTMLAudioElement? = null
        try {
            val blob = Blob(arrayOf(audio.data), BlobPropertyBag(type = audio.mimeType))
            url = URL.createObjectURL(blob)
            audioEl = document.createElement("audio") as HTMLAudioElement
            audioEl.src = url
            document.body?.append(audioEl)
            audioEl.addEventListener("ended") {
                url.let { URL.revokeObjectURL(it) }
                audioEl.remove()
            }
// ... rename existing `audio` variable to `audioEl` to avoid conflict with the parameter ...
            audioEl.addEventListener("error") {
                println("Audio playback error event")
                url.let { URL.revokeObjectURL(it) }
                audioEl.remove()
            }
            audioEl.play().await()
        } catch (e: Throwable) {
            println("Audio setup failed: ${e.message}")
            url?.let { URL.revokeObjectURL(it) }
            audioEl?.remove()
            throw AudioPlayException(e)
        }
    }
```

- [ ] **Step 5: Update WasmJS `AudioPlayer` Implementation**
Update `AudioPlayer.wasmJs.kt` to accept `audio: Audio` and use `audio.mimeType`. Make sure to rename the `audio` local variable to `audioEl` as well to prevent shadowing.

```kotlin
// ... inside actual class AudioPlayer ...
    actual suspend fun play(audio: Audio) {
        var url: String? = null
        var audioEl: HTMLAudioElement? = null
        try {
            val uint8Array = createUint8Array(audio.data.size)
            for (i in audio.data.indices) {
                setUint8Array(uint8Array, i, audio.data[i])
            }

            val jsArray = wrapInArray(uint8Array)
            val blobPropertyBag = BlobPropertyBag(type = audio.mimeType)
            val blob = Blob(jsArray, blobPropertyBag)

            url = URL.createObjectURL(blob)
            audioEl = document.createElement("audio") as HTMLAudioElement
            audioEl.src = url
// ... update rest to use audioEl, and ensure exceptions are thrown as `AudioPlayException(message = it)` or `AudioPlayException(e)` ...
```

- [ ] **Step 6: Temporarily Fix Compilation in ViewModel**
In `DialogueSearchViewModel.kt`, change `audioPlayer.play(bytes)` to `audioPlayer.play(Audio(bytes, "audio/wav"))`.

```kotlin
// inside playAudio
        viewModelScope.launch {
            try {
                val bytes = assetRepository.getAssetBytes(url)
                audioPlayer.play(Audio(bytes, "audio/wav"))
// ...
```

- [ ] **Step 7: Run Gradle to Verify Compilation**
Run: `./gradlew :shared:compileKotlinJvm :shared:compileKotlinJs :shared:compileKotlinWasmJs`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**
```bash
git add shared/
git commit -m "feat: define Audio data class and update AudioPlayer interfaces across platforms"
```

---

### Task 2: Network Client and Repository Updates

We will update Ktor networking layer to parse the MIME type from `Content-Type` header and return a new `AssetData` object. We will also introduce an exception for missing content types.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/AssetData.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/ApiClient.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/AssetRepository.kt`
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/network/FakeAssetRepository.kt`
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/network/NetworkAssetRepositoryTest.kt`

- [ ] **Step 1: Define `AssetData`**
Create `AssetData.kt` in `com.blbulyandavbulyan.larm.kmp.network` to hold any asset fetched from the network.

```kotlin
package com.blbulyandavbulyan.larm.kmp.network

data class AssetData(
    val data: ByteArray,
    val mimeType: String
)
```

- [ ] **Step 2: Update AssetRepository exceptions and interface**
In `AssetRepository.kt`, add the new exception and update the interface to return `AssetData`.

```kotlin
package com.blbulyandavbulyan.larm.kmp.network

fun interface AssetRepository {
    suspend fun getAsset(url: String): AssetData
}

class NetworkAssetRepository(private val apiClient: ApiClient) : AssetRepository {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun getAsset(url: String): AssetData {
        return try {
            apiClient.getAsset(url)
        } catch (e: Throwable) {
            throw AssetFetchException(e)
        }
    }
}

open class AssetFetchException(cause: Throwable? = null, message: String? = cause?.message) : Exception(message, cause)
class AssetHasNoContentTypeException : AssetFetchException(message = "Asset response is missing Content-Type header")
```

- [ ] **Step 3: Update ApiClient to fetch AssetData**
In `ApiClient.kt`, modify `getAssetBytes` to `getAsset`. Throw `AssetHasNoContentTypeException` if the header is missing.

```kotlin
import io.ktor.http.HttpHeaders
// ...
    suspend fun getAsset(url: String): AssetData {
        val response = client.get(url)
        val mimeType = response.headers[HttpHeaders.ContentType] ?: throw AssetHasNoContentTypeException()
        return AssetData(response.readRawBytes(), mimeType)
    }
```

- [ ] **Step 4: Update Test Fakes**
In `FakeAssetRepository.kt`, change the mocked function.

```kotlin
// ...
    override suspend fun getAsset(url: String): AssetData {
        requestedUrls.add(url)
        if (shouldFailWithAudioException) throw AudioPlayException(message = "Fake Audio Error")
        if (shouldFailWithAssetFetchException) throw AssetFetchException(message = "Fake Network Error")
        return AssetData(ByteArray(0), "audio/wav")
    }
```

- [ ] **Step 5: Update NetworkAssetRepositoryTest**
In `NetworkAssetRepositoryTest.kt`, change the test cases to verify the `AssetData` class is returned correctly along with `mimeType`.

```kotlin
// ... inside testGetAudioBytes ...
        val mockEngine = MockEngine { _ ->
            respond(
                content = byteArrayOf(1, 2, 3),
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.CacheControl to listOf("public, max-age=60"),
                    HttpHeaders.ContentType to listOf("audio/mpeg")
                )
            )
        }
// ...
        val result1 = repository.getAsset("http://example.com/audio.mp3")
        result1.data.size shouldBe 3
        result1.data[0] shouldBe 1.toByte()
        result1.mimeType shouldBe "audio/mpeg"

        val result2 = repository.getAsset("http://example.com/audio.mp3")
        result2.data.size shouldBe 3
        result2.data[0] shouldBe 1.toByte()
        result2.mimeType shouldBe "audio/mpeg"
// ...
```
Also update `testGetAudioBytesThrowsAudioFetchExceptionOn500` to call `repository.getAsset`.

- [ ] **Step 6: Add test for missing Content-Type**
In `NetworkAssetRepositoryTest.kt`, add a new test to verify `AssetHasNoContentTypeException`.

```kotlin
    @Test
    fun testGetAssetThrowsAssetHasNoContentTypeExceptionWhenNoContentType() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = byteArrayOf(1, 2, 3),
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.CacheControl to listOf("public, max-age=60")
                ) // No Content-Type
            )
        }
        val mockClient = HttpClient(mockEngine) {
            install(HttpCache)
            expectSuccess = true
        }
        val apiClient = ApiClient(client = mockClient)
        val repository = NetworkAssetRepository(apiClient)

        shouldThrow<AssetFetchException> {
            repository.getAsset("http://example.com/audio.mp3")
        }
    }
```

- [ ] **Step 7: Run Tests**
Run: `./gradlew :shared:jvmTest --tests "com.blbulyandavbulyan.larm.kmp.network.NetworkAssetRepositoryTest"`
Expected: PASS

- [ ] **Step 8: Commit**
```bash
git add shared/
git commit -m "feat: network clients now parse Content-Type header and return AssetData"
```

---

### Task 3: Hook Network Up to Player

We update the ViewModel to consume the `AssetData` object from the network, map it to an `Audio` object, and pass it to the audio player without any temporary mockings.

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/search/DialogueSearchViewModel.kt`

- [ ] **Step 1: Update ViewModel Integration**
In `DialogueSearchViewModel.kt`, map `assetRepository.getAsset(url)` to `Audio` before passing into `audioPlayer.play()`.

```kotlin
import com.blbulyandavbulyan.larm.kmp.audio.Audio
// ...
    fun playAudio(url: String) {
        viewModelScope.launch {
            try {
                val asset = assetRepository.getAsset(url)
                val audio = Audio(asset.data, asset.mimeType)
                audioPlayer.play(audio)
            } catch (e: AudioPlayException) {
// ...
```

- [ ] **Step 2: Run Gradle checks to verify codebase is clean**
Run: `./gradlew :shared:jvmTest`
Expected: BUILD SUCCESSFUL and all tests PASS.

- [ ] **Step 3: Commit**
```bash
git add shared/
git commit -m "feat: wire networking audio fetching directly to KMP audio player"
```
