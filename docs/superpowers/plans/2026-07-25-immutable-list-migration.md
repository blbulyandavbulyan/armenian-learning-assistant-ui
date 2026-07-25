# Migrate UI States to ImmutableList Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate standard `List<T>` types in Compose Multiplatform UI states to `ImmutableList<T>` from `kotlinx.collections.immutable` to prevent unnecessary recompositions.

**Architecture:** We will add the `kotlinx-collections-immutable` dependency to `libs.versions.toml` and `shared/build.gradle.kts`. Then replace standard `List` with `ImmutableList` in `SearchState.Success` and `DialogueChatViewModel` states, updating their initialization to use `.toImmutableList()` and `persistentListOf()`.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, kotlinx.collections.immutable

## Global Constraints

- Code must compile and tests must pass.
- Use Kotest assertions for tests (e.g., `shouldBe`).
- Immutable collections must be used for UI states to ensure stability for Compose compiler.

---

### Task 1: Add kotlinx-collections-immutable dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `shared/build.gradle.kts`

**Interfaces:**
- Consumes: N/A
- Produces: `kotlinx.collections.immutable` available in `commonMain` source set.

- [ ] **Step 1: Update libs.versions.toml**

Add the version and library declaration for `kotlinx.collections.immutable`.

```toml
# Add to [versions] block
kotlinxCollectionsImmutable = "0.5.1"

# Add to [libraries] block
kotlinx-collections-immutable = { module = "org.jetbrains.kotlinx:kotlinx-collections-immutable", version.ref = "kotlinxCollectionsImmutable" }
```

- [ ] **Step 2: Update shared/build.gradle.kts**

Add the dependency to the `commonMain` source set.

```kotlin
        commonMain.dependencies {
            implementation(libs.kotlinx.collections.immutable)
            // ... existing dependencies
        }
```

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml shared/build.gradle.kts
git commit -m "build: add kotlinx-collections-immutable dependency"
```

---

### Task 2: Migrate SearchState.Success to ImmutableList

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/search/SearchState.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/search/DialogueSearchViewModel.kt`

**Interfaces:**
- Consumes: `kotlinx.collections.immutable.ImmutableList`
- Produces: `SearchState.Success` with an `ImmutableList` instead of `List`

- [ ] **Step 1: Update SearchState.kt**

```kotlin
package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search

import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.DialogueSummaryResponse
import kotlinx.collections.immutable.ImmutableList

sealed class SearchState {
    data object Initial : SearchState()
    data object Loading : SearchState()
    data object Error : SearchState()
    data class Success(val results: ImmutableList<DialogueSummaryResponse>) : SearchState()
}
```

- [ ] **Step 2: Update DialogueSearchViewModel.kt**

In `searchDialogues`, convert the response list to `ImmutableList`.

```kotlin
// Add import
import kotlinx.collections.immutable.toImmutableList

// In searchDialogues method, change:
_searchState.value = SearchState.Success(response.dialogues.toImmutableList())
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/search/SearchState.kt
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/search/DialogueSearchViewModel.kt
git commit -m "refactor: migrate SearchState.Success to ImmutableList"
```

---

### Task 3: Migrate DialogueChatViewModel to ImmutableList

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueChatViewModel.kt`

**Interfaces:**
- Consumes: `kotlinx.collections.immutable.ImmutableList`
- Produces: `DialogueChatViewModel.conversation` exposing `StateFlow<ImmutableList<ConversationItem>>`

- [ ] **Step 1: Update DialogueChatViewModel.kt state types**

```kotlin
// Add imports
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

// Update state declaration
private val _conversation = MutableStateFlow<ImmutableList<ConversationItem>>(persistentListOf())
val conversation: StateFlow<ImmutableList<ConversationItem>> = _conversation.asStateFlow()
```

- [ ] **Step 2: Update DialogueChatViewModel.kt modifications**

Update how `_conversation.value` is updated to ensure it remains an `ImmutableList`.

```kotlin
    @Suppress("TooGenericExceptionCaught")
    fun generateDialogue(prompt: String) {
        if (prompt.isBlank()) return

        val current = _conversation.value.toMutableList()
        current.add(ConversationItem.UserMessage(prompt))
        current.add(ConversationItem.Loading)
        _conversation.value = current.toImmutableList()

        viewModelScope.launch {
            try {
                val response = repository.generateDialogue(prompt, chatId)
                val newConv = _conversation.value.filter { it !is ConversationItem.Loading }.toMutableList()
                newConv.add(ConversationItem.AiResponse(response))
                _conversation.value = newConv.toImmutableList()
            } catch (e: Throwable) {
                val newConv = _conversation.value.filter { it !is ConversationItem.Loading }
                _conversation.value = newConv.toImmutableList()
                println(e)
                globalErrorManager.showError(
                    UiText.from(Res.string.error_failed_to_generate_dialogue),
                    UiText.from(e.message, Res.string.error_unknown)
                )
            }
        }
    }
```

And in `changeConversationState`:

```kotlin
    private fun changeConversationState(
        dialogue: DialogueChatResponse,
        newDialogueConversationItem: (ConversationItem.AiResponse) -> ConversationItem.AiResponse
    ) {
        _conversation.value = _conversation.value.map {
            if (it is ConversationItem.AiResponse && it.response === dialogue) {
                newDialogueConversationItem(it)
            } else {
                it
            }
        }.toImmutableList()
    }
```

- [ ] **Step 3: Run Tests**

Run the test suite to ensure the changes don't break existing tests.

```bash
./gradlew :shared:cleanTest :shared:test --info
```

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueChatViewModel.kt
git commit -m "refactor: migrate DialogueChatViewModel conversation to ImmutableList"
```
