# Audio Playback Error Handling Design

## Purpose
Currently, audio playback exceptions in `AudioPlayer` are swallowed and simply printed to the console via `printStackTrace()`. Because of this, the UI is unaware of specific audio failures. Even when the `DialogueViewModel` manually catches a generic exception, it updates the main `_searchState` to `Error`, which completely breaks the UI by replacing the entire screen with an error state. This design outlines how to properly propagate audio playback errors and display them non-disruptively in the UI, complete with localized user-facing titles.

## 1. Exception Definition and Contract
- **`AudioPlayException`**: Create a custom exception `class AudioPlayException(message: String, cause: Throwable? = null) : Exception(message, cause)` inside the `com.blbulyandavbulyan.larm.kmp.audio` package.
- **Interface Contract**: Update `AudioPlayer.play(audioBytes: ByteArray)` with KDoc clearly specifying that it `@throws AudioPlayException` when an error occurs during audio initialization or playback.

## 2. AudioPlayer Implementations
In all platform-specific `AudioPlayer` implementations (`wasmJs`, `js`, `jvm`):
- Wrap internal playback logic with a `try/catch` block catching generic `Throwable` or `Exception`.
- Inside the `catch` block, properly log the original exception (using standard `println` or any existing Logger utility).
- Wrap the original exception and throw the newly created `AudioPlayException(e.message, e)`.

## 3. ViewModel State Management
In `DialogueViewModel`:
- **State Segregation**: Remove the logic that transitions the entire screen to `SearchState.Error` when `playAudio` fails.
- **New State Flow**: Introduce a new `MutableStateFlow<String?>(null)` named `_audioError` (with a public read-only `audioError` state).
- **Catch Logic**: Modify the `try/catch` inside `playAudio` to catch `AudioPlayException`. When caught, it will update `_audioError.value = e.message ?: "Unknown audio error"`.
- **Dismiss Action**: Add a public `fun dismissAudioError()` that resets `_audioError.value = null`.

## 4. Jetpack Compose UI Updates & Localization
- **New Resource**: Add a localized string `audio_playback_error_title` (e.g., "Audio Error") to `shared/src/commonMain/composeResources/values/strings.xml` (and corresponding translations if applicable).
In the UI screens that handle audio playback (e.g., `DialogueSearchScreen` and `DialogueDetailScreen`):
- Collect the `audioError` state from the ViewModel.
- When `audioError` is not null, display a dismissible/closable UI element (like a `Snackbar` or a stylized `Card` anchored to the bottom of the screen).
- **Content Display**: The UI element MUST display the localized title (`stringResource(Res.string.audio_playback_error_title)`) so the user knows what failed in their native language, followed by the raw `audioError` message string underneath it for technical context.
- Provide an 'X' or 'Close' button that invokes `viewModel.dismissAudioError()`.
- This ensures that if an audio file fails to play, the user can dismiss the error without losing the search results or dialogue content they are currently viewing.
