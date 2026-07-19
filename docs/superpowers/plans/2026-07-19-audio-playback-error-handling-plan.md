# Audio Playback Error Handling Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement non-disruptive audio error handling by introducing a custom exception and a UI banner for audio failures.

**Architecture:** A new `AudioPlayException` will be thrown by `AudioPlayer` implementations on failure. The `AudioPlayer.play` function will become `suspend` so it can properly await asynchronous promise rejections on JS targets. The `DialogueViewModel` will catch this specifically, exposing an `audioError` state flow. The UI will observe this flow and display a localized error banner at the bottom of the screen instead of entering a global error state. Error logging will be improved to omit stacktraces and use proper standard logging (e.g. clean `println` or standard app logger).

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose, Coroutines/StateFlow

## Global Constraints

- No hardcoded UI strings; use localized string resources.
- Always use Kotest assertions (`shouldBe`, etc.) instead of standard JUnit/kotlin.test in tests.
- Do NOT use `printStackTrace()`. Use proper loggers (or clean `println` statements containing the error details) for all caught exceptions.

---

### Task 1: Create AudioPlayException and Update Interface

**Files:**
- Create: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayException.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.kt`

**Interfaces:**
- Produces: `AudioPlayException` class.

- [ ] **Step 1: Create AudioPlayException**

Create `AudioPlayException.kt` with the following content:

```kotlin
package com.blbulyandavbulyan.larm.kmp.audio

class AudioPlayException(message: String, cause: Throwable? = null) : Exception(message, cause)
```

- [ ] **Step 2: Update AudioPlayer Interface**

Update `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.kt` to include KDoc indicating it throws the exception, and make `play` a `suspend` function:

```kotlin
package com.blbulyandavbulyan.larm.kmp.audio

expect class AudioPlayer() {
    /**
     * Plays the given audio bytes.
     * @throws AudioPlayException if an error occurs during audio initialization or playback.
     */
    suspend fun play(audioBytes: ByteArray)
}
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayException.kt shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.kt
git commit --no-gpg-sign -m "feat(audio): create AudioPlayException and update interface to suspend"
```

---

### Task 2: Implement Exception Throwing in AudioPlayers

**Files:**
- Modify: `shared/src/wasmJsMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.wasmJs.kt`
- Modify: `shared/src/jsMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.js.kt`
- Modify: `shared/src/jvmMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.jvm.kt`
- Test: `shared/src/jvmTest/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayerTest.kt`

**Interfaces:**
- Consumes: `AudioPlayException`

- [ ] **Step 1: Update wasmJs AudioPlayer**

In `AudioPlayer.wasmJs.kt`:
1. Make `play` a `suspend` function.
2. Update the outer catch block to remove `e.printStackTrace()`, use `println` (or app logger), and throw `AudioPlayException`.
3. Use `.await()` (from `kotlinx.coroutines.await`) on `audio.play()` so exceptions are properly caught by the outer `try-catch`, and remove the old `.catch {}` block if it's no longer needed, or catch the exception and wrap it in `AudioPlayException`.

```kotlin
        try {
            // ...
            audio.play().await()
        } catch (e: Throwable) {
            println("Audio setup/playback failed: ${e.message}")
            url?.let { URL.revokeObjectURL(it) }
            throw AudioPlayException(e.message ?: "Unknown audio error", e)
        }
```

- [ ] **Step 2: Update js AudioPlayer**

In `AudioPlayer.js.kt`, perform the exact same changes as `wasmJsMain`, making `play` suspend and using `.await()`.

- [ ] **Step 3: Update jvm AudioPlayer**

In `AudioPlayer.jvm.kt`, make `play` suspend, and replace the `e.printStackTrace()` in the catch block with standard logging and a throw:

```kotlin
    actual suspend fun play(audioBytes: ByteArray) {
        // ...
        } catch (e: Exception) {
            println("Audio setup/playback failed: ${e.message}")
            // ensure clip is closed if needed
            throw AudioPlayException(e.message ?: "Unknown audio error", e)
        }
    }
```

- [ ] **Step 4: Add JVM Test**

Add a unit test in `jvmTest` that verifies `play` throws `AudioPlayException` for invalid bytes.

- [ ] **Step 5: Commit**

```bash
git add shared/src/wasmJsMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.wasmJs.kt shared/src/jsMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.js.kt shared/src/jvmMain/kotlin/com/blbulyandavbulyan/larm/kmp/audio/AudioPlayer.jvm.kt shared/src/jvmTest/
git commit --no-gpg-sign -m "feat(audio): implement suspend play and throw AudioPlayException without stacktraces"
```

---

### Task 3: Localized Strings

**Files:**
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`
- Modify: `shared/src/commonMain/composeResources/values-ru/strings.xml`

**Interfaces:**
- Produces: `Res.string.audio_playback_error_title`

- [ ] **Step 1: Add default string**

In `shared/src/commonMain/composeResources/values/strings.xml`, add:
```xml
<string name="audio_playback_error_title">Audio Error</string>
```

- [ ] **Step 2: Add Russian translation**

In `shared/src/commonMain/composeResources/values-ru/strings.xml`:
```xml
<string name="audio_playback_error_title">Ошибка аудио</string>
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/composeResources/values*/strings.xml
git commit --no-gpg-sign -m "feat(ui): add localized string for audio playback error title"
```

---

### Task 4: ViewModel State Management

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueViewModel.kt`
- Test: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueViewModelTest.kt`

**Interfaces:**
- Consumes: `AudioPlayException`
- Produces: `val audioError: StateFlow<String?>` and `fun dismissAudioError()`

- [ ] **Step 1: Write failing test**

In `DialogueViewModelTest.kt`, add a test verifying `audioError` state changes on `AudioPlayException`:

```kotlin
    @Test
    fun playAudio_whenAudioPlayExceptionThrown_updatesAudioErrorStateAndDoesNotChangeSearchState() = runTest {
        // Setup mock AudioPlayer to throw AudioPlayException
        // viewModel.playAudio("url")
        // Advance until idle
        // viewModel.audioError.value shouldBe "error message from exception"
        // viewModel.searchState.value shouldNotBe instanceOf<SearchState.Error>()
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
./gradlew :shared:cleanTestJvm :shared:jvmTest --tests "com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueViewModelTest"
```
Expected: Compilation failure or Test failure.

- [ ] **Step 3: Write implementation**

In `DialogueViewModel.kt`, add the new state:

```kotlin
    private val _audioError = MutableStateFlow<String?>(null)
    val audioError: StateFlow<String?> = _audioError.asStateFlow()
    
    fun dismissAudioError() {
        _audioError.value = null
    }
```

Update `playAudio`:

```kotlin
    fun playAudio(url: String) {
        viewModelScope.launch {
            try {
                val bytes = assetRepository.getAssetBytes(url)
                audioPlayer.play(bytes)
            } catch (e: AudioPlayException) {
                _audioError.value = e.message ?: "Unknown audio error"
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: getString(Res.string.error_unknown))
            }
        }
    }
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :shared:cleanTestJvm :shared:jvmTest --tests "com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueViewModelTest"
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueViewModel.kt shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueViewModelTest.kt
git commit --no-gpg-sign -m "feat(presentation): handle AudioPlayException separately in DialogueViewModel"
```

---

### Task 5: UI Banner implementation

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/dialogue/search/DialogueSearchScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/dialogue/detail/DialogueDetailScreen.kt`

**Interfaces:**
- Consumes: `DialogueViewModel.audioError`, `DialogueViewModel.dismissAudioError()`, `Res.string.audio_playback_error_title`

- [ ] **Step 1: Implement AudioErrorBanner Composables**

At the bottom of `DialogueSearchScreen.kt`, add a new composable for the banner:

```kotlin
@Composable
fun AudioErrorBanner(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    Snackbar(
        action = {
            TextButton(onClick = onDismiss) {
                Text("X")
            }
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(Res.string.audio_playback_error_title),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(text = errorMessage)
        }
    }
}
```

- [ ] **Step 2: Add Banner to DialogueSearchScreen**

In `DialogueSearchScreen.kt`, observe the error and display it inside the `Box` at the bottom:

```kotlin
    val audioError by viewModel.audioError.collectAsState()

    Box(
        // ... existing modifier ...
    ) {
        // ... existing Column ...

        audioError?.let { errorMsg ->
            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                AudioErrorBanner(errorMessage = errorMsg, onDismiss = viewModel::dismissAudioError)
            }
        }
    }
```

- [ ] **Step 3: Add Banner to DialogueDetailScreen**

Repeat the same integration pattern in `DialogueDetailScreen.kt`:

```kotlin
    val audioError by viewModel.audioError.collectAsState()
    
    // inside the main Box or Scaffold:
    audioError?.let { errorMsg ->
        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            AudioErrorBanner(errorMessage = errorMsg, onDismiss = viewModel::dismissAudioError)
        }
    }
```

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/dialogue/search/DialogueSearchScreen.kt shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/dialogue/detail/DialogueDetailScreen.kt
git commit --no-gpg-sign -m "feat(ui): display non-disruptive banner for audio errors"
```
