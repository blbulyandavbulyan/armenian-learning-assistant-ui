# Audio Enhancements Design Spec

## Overview
This design outlines the architecture for handling audio assets across the dialogue screens. The scope is strictly limited to audio handling (no translations or transcriptions for speakers). We are encapsulating the audio asset resolution logic within our data models and extracting the visual button into a reusable, "dumb" UI component.

## Architecture

### 1. Data Model (DialogueModels.kt)
We will add a computed property to `PhraseResponse` to encapsulate the logic for finding an audio asset.
Because it's a computed getter without a backing field, `kotlinx.serialization` will natively ignore it, fulfilling the requirement that it shouldn't be serialized.

```kotlin
val audioAssetUrl: String?
    get() = assets.firstOrNull { it.contentType.startsWith("audio/") }?.url
```

### 2. Networking & HTTP Caching
We will remove the custom `Map`-based naive caching in `NetworkAssetRepository` to prevent memory leaks and respect server expiration correctly. 
We will let the Ktor client handle caching natively by relying on standard HTTP `Cache-Control` headers from the backend.
- **AppModule.kt:** Install the `HttpCache` plugin in our `HttpClient` configuration.
- **NetworkAssetRepository.kt:** Remove the `audioCache` map and `Mutex`, and simply delegate to `apiClient.getAssetBytes(url)`.

### 3. UI Component (ListenButton.kt)
We will create a pure UI component named `ListenButton` in the `ui` package.
It will internally resolve its own text using `stringResource(Res.string.listen_phrase_button)` (which reads "Listen") so that the text logic isn't leaked to the call sites.

```kotlin
@Composable
fun ListenButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOutlined: Boolean = false
) {
    val text = stringResource(Res.string.listen_phrase_button)
    if (isOutlined) {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Text(text)
        }
    } else {
        Button(onClick = onClick, modifier = modifier) {
            Text(text)
        }
    }
}
```

### 4. Screen Refactoring
We will update the usage sites to use the new `audioAssetUrl` field and the `ListenButton` component.

#### DialogueSearchScreen.kt
*   **Search Results List:** Replace the hardcoded asset index with `audioAssetUrl`.

```kotlin
dialogue.title.audioAssetUrl?.let { url ->
    ListenButton(
        onClick = { viewModel.playAudio(url) },
        isOutlined = true
    )
}
```

#### DialogueDetailScreen.kt
*   **Dialogue Title:** Add the `ListenButton` below the translations.
*   **Speaker Name:** Add the `ListenButton` inline next to the speaker name.
*   **Dialogue Phrases:** Refactor the existing listen button to use `audioAssetUrl` and `ListenButton`.

```kotlin
dialoguePhrase.phrase.audioAssetUrl?.let { url ->
    ListenButton(
        onClick = { viewModel.playAudio(url) },
        modifier = Modifier.align(Alignment.End)
    )
}
```

### 5. Automated UI Tests (DialogueSearchScreenTest.kt)
We must implement the audio-related test cases described in the TODO comments at the bottom of `DialogueSearchScreenTest.kt`, specifically updating the mock data to include valid audio assets, and verifying the new audio buttons:

*   **Test Mock Data Updates:** Replace `assets = emptyList() // TODO, not valid, there must be assets` with a valid mock audio asset `AssetResponse("audio/mpeg", "http://test.audio/1")`.
*   **Test Case 2:** When 'Details' are shown, pressing the listen buttons for the Title, Speaker, and Phrases invokes the `fakeAudioRepository` with the right URL.
*   **Test Case 3 (Audio Cache):** When 'Details' are shown, pressing the listen button multiple times ensures the caching works and the backend is invoked only once.
*   **Test Case 4:** When the 'Dialogues search screen' is shown, pressing the 'listen' button near the dialogue invokes the correct backend endpoint with the right URL.

## Self-Review Checklist
- [x] Placeholder scan: No TBDs or TODOs left in the spec.
- [x] Internal consistency: The UI uses the newly proposed `audioAssetUrl` and `ListenButton`.
- [x] Scope check: Only audio-related functionality is addressed. Speaker transcriptions/translations are explicitly excluded.
- [x] Ambiguity check: The text string `listen_phrase_button` is used internally in `ListenButton` to standardize the text as requested.
