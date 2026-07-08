# Semantic Search & Dialogue Detail Feature Design

## Overview
Implement a new "Search Dialogues" screen with semantic search capabilities, and a "Full Dialogue View" screen that allows users to view and listen to all parts of a saved dialogue. This feature targets Compose Multiplatform (JVM, JS, WasmJS).

## Architecture & State
1. **Screen State**: Update the UI state to support multiple screens without a heavy navigation library. Introduce a `Screen` sealed class:
   - `GeneratorScreen`
   - `SearchScreen`
   - `DetailScreen(dialogue: GetDialogueResponse)`
2. **ViewModel**: `DialogueViewModel` will hold the `searchQuery` and `searchResults` as state flows. Navigating between Search and Detail screens will simply switch the `CurrentScreen` state, preserving the search results and query without refetching data.

## UI Components

### Navigation Structure
- **App Header**: Introduce a top app bar across screens.
- **Generator Screen**: The header will include a "Search" icon button that switches the state to `SearchScreen`.
- **Search & Detail Screens**: The header will include a "Back" button to return to the previous screen.

### Search Dialogues Screen
- **Search Input**: A prominent search bar at the top with the placeholder: *"Type query to find existing dialogue"*.
- **Search Results List**: A simple `Column` with `verticalScroll` (since results are hard-limited to 50, avoiding `LazyColumn` scrollbar issues).
- **Search Result Card**:
  - Displays the Armenian Title, transcription, and translation.
  - An outlined, visually distinct "View Full" button.
  - Native browser audio controls (for JS/Wasm) or equivalent (for JVM) to play the title's audio asset directly from the list.

### Full Dialogue Detail Screen
- **Layout**: Reuses the chat-bubble design from `DialogueGeneratorScreen` to display the full dialogue.
- **Inline Audio Controls**: Instead of native progress bars, small custom "Play" icon buttons will be placed inline next to:
  - The Dialogue Title
  - The Speaker Names (inside the bubbles)
  - The Dialogue Phrases (inside the bubbles)

## Multiplatform Audio Implementation
Since the project targets JVM, JS, and WasmJS, we cannot rely solely on JS interop for audio.
- **Caching & Playback**: To avoid refetching data, audio will be fetched once as a `ByteArray` using Ktor (cached in memory or Ktor cache). 
- We will define a common interface `interface AudioPlayer { fun play(audioBytes: ByteArray) }` or equivalent expect/actual.
- **JS / WasmJS Actual**: Implemented by converting `ByteArray` to a Blob and playing via `HTMLAudioElement` (`URL.createObjectURL(blob)`).
- **JVM Actual**: Implemented using a standard Java audio library (e.g., `javax.sound.sampled` for basic WAV playback) directly from the `ByteArray`.
