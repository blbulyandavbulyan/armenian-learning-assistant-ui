# Dialogue Generation Retry Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a persistent inline "Retry" button for dialogue generation failures.

**Architecture:** We are updating the MVI state flow. A new `ConversationItem.Error` state will replace `Loading` when generation fails. The UI will render this state as a failure bubble with a retry button. The view model will expose a `retryDialogue` function to handle the retry action, which swaps the error back to loading.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Kotest.

## Global Constraints

- Always use Kotest assertions (`import io.kotest.matchers.shouldBe`) instead of standard assertions.
- Do not mutate pure UI components for asynchronous state. Dumb UI components must remain pure.
- UI tests (using `runComposeUiTest`) require the Skiko WebAssembly binary, so testing must use Kotest or Compose UI test APIs correctly.
- No Hardcoded Strings. Use `Res.string.*` for any UI display strings.

---

### Task 1: State & ViewModel Updates

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/ConversationItem.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueChatViewModel.kt`
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueChatViewModelTest.kt`

**Interfaces:**
- Consumes: `DialogueChatRepository`
- Produces: `ConversationItem.Error` class, `retryDialogue(id: String)` method on `DialogueChatViewModel`.

- [ ] **Step 1: Write failing tests for generation failure and retry in `DialogueChatViewModelTest.kt`**

```kotlin
    @Test
    fun `generateDialogue - when repository throws exception - emits Error item instead of removing Loading`() = runTest {
        // Arrange
        val prompt = "test prompt"
        val exception = RuntimeException("Network Error")
        everySuspend { repository.generateDialogue(any(), any()) } throws exception

        // Act
        viewModel.generateDialogue(prompt)
        runCurrent()

        // Assert
        val items = viewModel.conversation.value
        items.size shouldBe 2 // UserMessage and Error
        items[0] shouldBe ConversationItem.UserMessage(prompt)
        items[1].shouldBeInstanceOf<ConversationItem.Error>()
        val errorItem = items[1] as ConversationItem.Error
        errorItem.failedPrompt shouldBe prompt
    }

    @Test
    fun `retryDialogue - when called with valid error id - replaces Error with Loading and retries`() = runTest {
        // Arrange
        val prompt = "test prompt"
        val exception = RuntimeException("Network Error")
        everySuspend { repository.generateDialogue(any(), any()) } throws exception

        viewModel.generateDialogue(prompt)
        runCurrent()
        
        val errorItem = viewModel.conversation.value.last() as ConversationItem.Error
        
        val successfulResponse = GeneratedDialogue(emptyList())
        everySuspend { repository.generateDialogue(prompt, any()) } returns successfulResponse

        // Act
        viewModel.retryDialogue(errorItem.id)
        runCurrent()

        // Assert
        val items = viewModel.conversation.value
        items.last() shouldBe ConversationItem.AiResponse(successfulResponse)
    }
```

- [ ] **Step 2: Update `ConversationItem.kt` to include `Error` state**

```kotlin
package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat

import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.GeneratedDialogue
import kotlin.uuid.Uuid

sealed class ConversationItem {
    data class UserMessage(val text: String) : ConversationItem()
    data class AiResponse(
        val response: GeneratedDialogue,
        val isSaving: Boolean = false,
        val isSaved: Boolean = false
    ) : ConversationItem()
    data object Loading : ConversationItem()
    data class Error(val failedPrompt: String, val id: String = Uuid.random().toString()) : ConversationItem()
}
```

- [ ] **Step 3: Implement ViewModel logic in `DialogueChatViewModel.kt`**

```kotlin
// In DialogueChatViewModel.kt
// Update generateDialogue catch block:
            } catch (e: Throwable) {
                val errorItem = ConversationItem.Error(prompt)
                _conversation.update { current ->
                    current.map { if (it is ConversationItem.Loading) errorItem else it }.toPersistentList()
                }
                println(e)
                globalErrorManager.showError(
                    UiText.from(Res.string.error_failed_to_generate_dialogue),
                    UiText.from(e.message, Res.string.error_unknown)
                )
            }

// Add retryDialogue function:
    @Suppress("TooGenericExceptionCaught")
    fun retryDialogue(id: String) {
        val errorItem = _conversation.value.filterIsInstance<ConversationItem.Error>().find { it.id == id } ?: return
        
        _conversation.update { current ->
            current.map {
                if (it is ConversationItem.Error && it.id == id) {
                    ConversationItem.Loading
                } else it
            }.toPersistentList()
        }
        
        viewModelScope.launch {
            try {
                val response = repository.generateDialogue(errorItem.failedPrompt, chatId)
                _conversation.update { current ->
                    current.removingAll { it is ConversationItem.Loading }
                        .adding(ConversationItem.AiResponse(response))
                }
            } catch (e: Throwable) {
                _conversation.update { current ->
                    current.map {
                        if (it is ConversationItem.Loading) {
                            errorItem
                        } else it
                    }.toPersistentList()
                }
                println(e)
                globalErrorManager.showError(
                    UiText.from(Res.string.error_failed_to_generate_dialogue),
                    UiText.from(e.message, Res.string.error_unknown)
                )
            }
        }
    }
```

- [ ] **Step 4: Run tests to verify they pass**
Run: `./gradlew :shared:cleanTest :shared:testDebugUnitTest --tests "com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueChatViewModelTest"`
Expected: PASS

- [ ] **Step 5: Commit**
Run `git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/* shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueChatViewModelTest.kt`
Run `git commit --no-gpg-sign -m "feat: add error state and retry logic to DialogueChatViewModel"`

---

### Task 2: UI Updates

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/dialogue/chat/DialogueGenerator.kt`
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/ui/dialogue/chat/DialogueGeneratorScreenTest.kt`

**Interfaces:**
- Consumes: `ConversationItem.Error`, `viewModel.retryDialogue`
- Produces: Visual error state in `ConversationScreen` with a retry button.

- [ ] **Step 1: Write failing UI test in `DialogueGeneratorScreenTest.kt`**
Add a test ensuring `ConversationItem.Error` displays the error text and a clickable retry button.

```kotlin
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun shouldDisplayErrorStateAndRetryButton() = runComposeUiTest {
        var retryId: String? = null
        val errorItem = ConversationItem.Error("failed prompt", id = "error-123")
        
        setContent {
            AppTheme {
                DialogueGeneratorScreen(
                    conversation = listOf(errorItem),
                    onGenerateDialogue = {},
                    onSaveDialogue = {},
                    onRetryDialogue = { retryId = it }
                )
            }
        }

        onNodeWithTag("errorItemView").assertIsDisplayed()
        onNodeWithTag("retryButton").assertIsDisplayed().performClick()
        
        retryId shouldBe "error-123"
    }
```

- [ ] **Step 2: Update `DialogueGeneratorScreen` signature in `DialogueGenerator.kt`**
Update the stateful wrapper:
```kotlin
@Composable
fun DialogueGeneratorScreen(
    viewModel: DialogueChatViewModel,
    modifier: Modifier = Modifier
) {
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    DialogueGeneratorScreen(
        conversation = conversation,
        modifier = modifier,
        onGenerateDialogue = viewModel::generateDialogue,
        onSaveDialogue = viewModel::saveDialogue,
        onRetryDialogue = viewModel::retryDialogue
    )
}
```
Update the stateless overload to accept `onRetryDialogue: (String) -> Unit = {}`.

- [ ] **Step 3: Update `ConversationScreen` in `DialogueGenerator.kt`**

Add `onRetryDialogue: (String) -> Unit` to `ConversationScreen` parameters.
Pass `onRetryDialogue` from `DialogueGeneratorScreen` down to `ConversationScreen`.

In the `when (item)` block inside `ConversationScreen`, add the `Error` branch:
```kotlin
                        is ConversationItem.Error -> {
                            ErrorItemView(
                                errorItem = item,
                                onRetry = { onRetryDialogue(item.id) }
                            )
                        }
```

- [ ] **Step 4: Implement `ErrorItemView` in `DialogueGenerator.kt`**

```kotlin
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_generate_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.retry_button
// ...
@Composable
fun ErrorItemView(
    errorItem: ConversationItem.Error,
    onRetry: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().testTag("errorItemView"), horizontalArrangement = Arrangement.Start) {
        Card(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 16.dp,
                bottomEnd = 16.dp,
                bottomStart = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth(fraction = 0.85f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.error_failed_to_generate_dialogue),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("retryButton")
                ) {
                    Text(stringResource(Res.string.retry_button))
                }
            }
        }
    }
}
```

- [ ] **Step 5: Run UI tests to verify they pass**
Run: `./gradlew :shared:cleanTest :shared:testDebugUnitTest --tests "com.blbulyandavbulyan.larm.kmp.ui.dialogue.chat.DialogueGeneratorScreenTest"`
Expected: PASS

- [ ] **Step 6: Commit**
Run `git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/dialogue/chat/DialogueGenerator.kt shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/ui/dialogue/chat/DialogueGeneratorScreenTest.kt`
Run `git commit --no-gpg-sign -m "feat: display error state and retry button in DialogueGeneratorScreen"`
