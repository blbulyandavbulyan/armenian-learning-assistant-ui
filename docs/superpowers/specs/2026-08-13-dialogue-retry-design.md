# Dialogue Generation Retry Design

## Purpose
Currently, when generating an AI dialogue fails in the Armenian Learning Assistant, the `Loading` item is removed, and the user has no way to retry the failed prompt without retyping it or creating a duplicate message. This feature adds an explicit "Retry" mechanism directly within the chat interface.

## Architecture & Data Flow

1. **State Management (`ConversationItem.kt`)**
   - Add a new state: `data class Error(val failedPrompt: String, val id: String = Uuid.random().toString()) : ConversationItem()`
   - This state encapsulates the failed generation attempt and the prompt required to retry it.

2. **ViewModel Logic (`DialogueChatViewModel.kt`)**
   - **Generation Failure:** In `generateDialogue(prompt: String)`, if `repository.generateDialogue` throws an exception, replace the `ConversationItem.Loading` item with `ConversationItem.Error(prompt)` instead of just removing it.
   - **Retry Action:** Add `fun retryDialogue(id: String)`. This method will:
     - Locate the `Error` item by its `id`.
     - Replace the `Error` item with a `Loading` item.
     - Call the repository to generate the dialogue using the `failedPrompt`.
     - On success, replace `Loading` with `AiResponse`.
     - On failure, replace `Loading` back with the `Error` item.

3. **User Interface (`DialogueGenerator.kt`)**
   - In `ConversationScreen`, add a branch to handle `ConversationItem.Error`.
   - Render the error state using a custom component (e.g., `ErrorItemView`).
   - The view will display a relevant error message and a "Retry" button (using `Res.string.retry_button`).
   - Clicking the "Retry" button invokes the `onRetry` lambda, passing the `Error` item's `id`, which calls `viewModel.retryDialogue(id)`.

## Error Handling
- The global error manager (`globalErrorManager.showError`) remains active and will continue to show transient error toasts/snackbars when a failure occurs. This inline error bubble serves as a persistent anchor for the retry action.

## Testing
- Verify that clicking "Retry" successfully fires a new API request without appending a duplicate user message to the UI.
- Verify that if the retry fails again, the error bubble remains in place.
