# Semantic Search & Dialogue Detail Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a semantic search screen and full dialogue view with inline multiplatform audio playback.

**Architecture:** We use an MVI-like state in `DialogueViewModel` to handle navigation between `GeneratorScreen`, `SearchScreen`, and `DetailScreen`. Audio playback is handled by an `expect/actual` `AudioPlayer` that plays cached `ByteArray` data fetched via Ktor.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Ktor Client, JS/WasmJS, JVM (`javax.sound.sampled`).

## Global Constraints

- Must target JVM, JS, and WasmJS platforms.
- Avoid `LazyColumn` for search results; use `Column` with `verticalScroll`.
- Cache audio byte arrays so they are not refetched over the network.
- UI tests are excluded from the JS target in `build.gradle.kts`. ViewModels should be tested in `commonTest` under the `.presentation` package.
- No hardcoded strings; use Compose Multiplatform resources. (Assuming strings already exist or we will add them to string resources).

---

### Task 1: Update Models and API Client

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/data/DialogueModels.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/ApiClient.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/DialogueRepository.kt`

**Interfaces:**
- Consumes: Backend API
- Produces: Data models and repository functions for Search, Get Dialogue, and Get Asset.

- [ ] **Step 1: Write the failing tests for repository functions**

```kotlin
// In shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/network/DialogueRepositoryTest.kt
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class DialogueRepositoryTest {
    // Basic test to ensure compilation and basic wiring
    @Test
    fun testSearchDialogues() = runTest {
        // test code that calls repository.searchDialogues("query") and expects a result
    }
}
```

- [ ] **Step 2: Add missing models to DialogueModels.kt**

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class AssetResponse(val contentType: String, val url: String)

@Serializable
data class PhraseTranslation(val id: String, val isoLanguageCode: String, val translationText: String)

@Serializable
data class PhraseResponse(val id: String, val phrase: String, val isoLanguageCode: String, val transcription: String, val translations: List<PhraseTranslation>, val assets: List<AssetResponse>)

@Serializable
data class DialogueSummaryResponse(val id: String, val title: PhraseResponse)

@Serializable
data class SearchDialoguesResponse(val dialogues: List<DialogueSummaryResponse>)

@Serializable
data class GetDialogueSpeakerResponse(val id: String, val name: PhraseResponse)

@Serializable
data class GetDialoguePhraseResponse(val speakerId: String, val phrase: PhraseResponse)

@Serializable
data class GetDialogueResponse(val id: String, val title: PhraseResponse, val speakers: List<GetDialogueSpeakerResponse>, val dialoguePhrases: List<GetDialoguePhraseResponse>)
```

- [ ] **Step 3: Update ApiClient and DialogueRepository**
Add `searchDialogues(query: String)`, `getDialogue(id: String)`, and `getAssetBytes(url: String)` to ApiClient.
Add memory cache `val audioCache = mutableMapOf<String, ByteArray>()` to DialogueRepository, and a `getAudioBytes(url: String): ByteArray` that caches the result. Add repository methods for search and getDialogue.

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/data/DialogueModels.kt
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/ApiClient.kt
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/DialogueRepository.kt
git commit -m "feat: add search and get dialogue api endpoints and models"
```

---

### Task 2: Multiplatform AudioPlayer

**Files:**
- Create: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.kt`
- Create: `shared/src/jvmMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.jvm.kt`
- Create: `shared/src/jsMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.js.kt`
- Create: `shared/src/wasmJsMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.wasmJs.kt`

**Interfaces:**
- Produces: `expect class AudioPlayer() { fun play(audioBytes: ByteArray) }`

- [ ] **Step 1: Write common expect class**

```kotlin
package com.blbulyandavbulyan.larm.kmp.audio

expect class AudioPlayer() {
    fun play(audioBytes: ByteArray)
}
```

- [ ] **Step 2: Write JVM actual using javax.sound.sampled**

```kotlin
package com.blbulyandavbulyan.larm.kmp.audio

import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem

actual class AudioPlayer actual constructor() {
    actual fun play(audioBytes: ByteArray) {
        try {
            val audioInputStream = AudioSystem.getAudioInputStream(ByteArrayInputStream(audioBytes))
            val clip = AudioSystem.getClip()
            clip.open(audioInputStream)
            clip.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
```

- [ ] **Step 3: Write JS and WasmJs actuals**

```kotlin
// In shared/src/jsMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.js.kt
// and similarly in wasmJsMain adapting for Wasm
package com.blbulyandavbulyan.larm.kmp.audio

import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.dom.url.URL

actual class AudioPlayer actual constructor() {
    actual fun play(audioBytes: ByteArray) {
        try {
            val blob = Blob(arrayOf(audioBytes), BlobPropertyBag(type = "audio/wav"))
            val url = URL.createObjectURL(blob)
            val audio = document.createElement("audio") as HTMLAudioElement
            audio.src = url
            audio.play()
        } catch (e: Exception) {
            console.error("Audio playback failed", e)
        }
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.kt
git add shared/src/jvmMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.jvm.kt
git add shared/src/jsMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.js.kt
git add shared/src/wasmJsMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.wasmJs.kt
git commit -m "feat: implement multiplatform audio player for byte array"
```

---

### Task 3: Navigation State in ViewModel

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/DialogueViewModel.kt`

**Interfaces:**
- Consumes: Repository models
- Produces: `Screen` sealed class, `searchQuery`, `searchResults`, `currentScreen` state flows.

- [ ] **Step 1: Write state classes and tests**
Test that `navigateToSearch`, `navigateToGenerator`, `navigateToDetail(id)` update `currentScreen`.
Test `searchDialogues(query)` updates `searchResults`.

- [ ] **Step 2: Implement State and functions in ViewModel**

```kotlin
sealed class Screen {
    object Generator : Screen()
    object Search : Screen()
    data class Detail(val dialogue: GetDialogueResponse) : Screen()
}

// In ViewModel
private val _currentScreen = MutableStateFlow<Screen>(Screen.Generator)
val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

private val _searchQuery = MutableStateFlow("")
val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

private val _searchResults = MutableStateFlow<List<DialogueSummaryResponse>>(emptyList())
val searchResults: StateFlow<List<DialogueSummaryResponse>> = _searchResults.asStateFlow()

// functions to update these and fetch data
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/DialogueViewModel.kt
git commit -m "feat: add navigation state and search flow to viewmodel"
```

---

### Task 4: UI - Search Dialogues Screen

**Files:**
- Create: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueSearchScreen.kt`

**Interfaces:**
- Consumes: `DialogueSummaryResponse`, `AudioPlayer`

- [ ] **Step 1: Implement DialogueSearchScreen**
Must use `Column` with `verticalScroll` for results.
Top bar with back button (returns to Generator).
Search input field.
Card for each result: Armenian title, transcription, translation. A "Play" button next to title that fetches bytes and uses `AudioPlayer`. A distinct "View Full" button that triggers `onViewFull(id)`.

- [ ] **Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueSearchScreen.kt
git commit -m "feat: implement search dialogues ui screen"
```

---

### Task 5: UI - Full Dialogue Detail Screen

**Files:**
- Create: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueDetailScreen.kt`

**Interfaces:**
- Consumes: `GetDialogueResponse`, `AudioPlayer`

- [ ] **Step 1: Implement DialogueDetailScreen**
Re-use chat bubble design aesthetic from `DialogueView.kt` but for the saved dialogue.
Add inline "Play" icons (▶) next to Title, Speaker name, and Phrase text. Each button calls repository to fetch bytes and then `audioPlayer.play()`.
Add back button in header to return to Search screen.

- [ ] **Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueDetailScreen.kt
git commit -m "feat: implement full dialogue detail screen with inline audio"
```

---

### Task 6: Wire Up Navigation in App

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/App.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGenerator.kt`

**Interfaces:**
- Consumes: `DialogueViewModel.currentScreen`

- [ ] **Step 1: Update App.kt to observe currentScreen**
Use `when(currentScreen)` to render `DialogueGeneratorScreen`, `DialogueSearchScreen`, or `DialogueDetailScreen`.

- [ ] **Step 2: Update Generator Screen Header**
Add a "Search" icon button in the header of `DialogueGeneratorScreen` to trigger navigation to the Search screen.

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/App.kt shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGenerator.kt
git commit -m "feat: wire up navigation between screens in app"
```
