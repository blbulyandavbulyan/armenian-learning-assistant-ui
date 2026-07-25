# Domain Models Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Decouple API response models from the presentation layer by introducing dedicated domain models mapped inside the repository.

**Architecture:** We will create two distinct domain packages (one for Search, one for Chat) using pure Kotlin data classes and `ImmutableList`. We will then refactor the repositories to return these domain models, and update the ViewModels and UI to consume them.

**Tech Stack:** Kotlin Multiplatform, kotlinx.collections.immutable

## Global Constraints

- API models (`*Response`) must not leak out of the repository layer.
- All domain models must use `ImmutableList` from `kotlinx.collections.immutable` instead of standard Kotlin `List`.
- Mapping functions must be written as `private` extension functions scoped within the respective Repository files.
- Code must compile and tests must pass at the end of each task.

---

### Task 1: Define Domain Models

**Files:**
- Create: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/domain/model/dialogue/search/SearchDomainModels.kt`
- Create: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/domain/model/dialogue/chat/ChatDomainModels.kt`

**Interfaces:**
- Produces: `Asset`, `PhraseTranslation`, `Phrase`, `DialogueSummary`, `Speaker`, `DialoguePhrase`, `Dialogue` (Search domain)
- Produces: `ChatTranslation`, `DialogueTitle`, `DraftSpeaker`, `DraftPhrase`, `DraftDialoguePhrase`, `GeneratedDialogue` (Chat domain)

- [ ] **Step 1: Create Search Domain Models**
Write the following code into `SearchDomainModels.kt`:
```kotlin
package com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.search

import kotlinx.collections.immutable.ImmutableList

data class Asset(val contentType: String, val url: String)
data class PhraseTranslation(val id: String, val isoLanguageCode: String, val translationText: String)

data class Phrase(
    val id: String, 
    val text: String,
    val isoLanguageCode: String,
    val transcription: String,
    val translations: ImmutableList<PhraseTranslation>,
    val assets: ImmutableList<Asset>
) {
    val audioAssetUrl: String? get() = assets.firstOrNull { it.contentType.startsWith("audio/") }?.url
}

data class DialogueSummary(val id: String, val title: Phrase)
data class Speaker(val id: String, val name: Phrase)
data class DialoguePhrase(val speakerId: String, val phrase: Phrase)

data class Dialogue(
    val id: String,
    val title: Phrase,
    val speakers: ImmutableList<Speaker>,
    val phrases: ImmutableList<DialoguePhrase>
)
```

- [ ] **Step 2: Create Chat Domain Models**
Write the following code into `ChatDomainModels.kt`:
```kotlin
package com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.chat

import kotlinx.collections.immutable.ImmutableList

data class ChatTranslation(val translationText: String, val isoLanguageCode: String)
data class DialogueTitle(val text: String, val transcription: String, val translations: ImmutableList<ChatTranslation>)
data class DraftSpeaker(val id: String, val name: String, val transcription: String, val translations: ImmutableList<ChatTranslation>)
data class DraftPhrase(val text: String, val isoLanguageCode: String, val transcription: String, val translations: ImmutableList<ChatTranslation>)
data class DraftDialoguePhrase(val speakerId: String, val phrase: DraftPhrase)
data class GeneratedDialogue(val message: String, val info: DialogueTitle, val speakers: ImmutableList<DraftSpeaker>, val phrases: ImmutableList<DraftDialoguePhrase>)
```

- [ ] **Step 3: Verify compilation**
Run `./gradlew :shared:compileKotlinJvm` to ensure everything compiles.

- [ ] **Step 4: Commit**
```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/domain/model/dialogue/search/SearchDomainModels.kt
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/domain/model/dialogue/chat/ChatDomainModels.kt
git commit -m "feat: define dialogue domain models"
```

---

### Task 2: Refactor Search Feature to Domain Models

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/DialogueRepository.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/search/SearchState.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/search/DialogueSearchViewModel.kt`
- Modify: Search UI consumers (e.g. `DialogueSearchScreen.kt`, `DialogueScreen.kt`, test files)

**Interfaces:**
- Consumes: Search Domain Models from Task 1.

- [ ] **Step 1: Update DialogueRepository interface & Implementation**
In `DialogueRepository.kt`:
1. Change return types of `searchDialogues` to `ImmutableList<DialogueSummary>` and `getDialogue` to `Dialogue`.
2. Write private extension mappers to map `SearchDialoguesResponse` -> `ImmutableList<DialogueSummary>` and `GetDialogueResponse` -> `Dialogue`, utilizing `toImmutableList()` on collections.
3. Update `NetworkDialogueRepository` implementation to call `return apiClient.searchDialogues(query).dialogues.map { it.toDomain() }.toImmutableList()` etc.

- [ ] **Step 2: Update SearchState and ViewModel**
In `SearchState.kt`: Change `Success` to hold `ImmutableList<DialogueSummary>`.
In `DialogueSearchViewModel.kt`: 
1. `searchDialogues` should now assign `_searchState.value = SearchState.Success(response)` directly, as it's already an `ImmutableList<DialogueSummary>`.
2. `displayDialogue` callback `onDialogueReady` should accept `Dialogue` instead of `GetDialogueResponse`.

- [ ] **Step 3: Update UI Consumers & Tests**
Run the Kotlin compiler `./gradlew :shared:compileKotlinJvm` to find all UI files and test files that are broken due to signature changes. You will likely need to update:
- `DialogueSearchScreen.kt` (using `phrase.text` instead of `phrase.phrase`)
- `DialogueScreen.kt` (using `phrase.text` instead of `phrase.phrase`, mapping fixes)
- `DialogueSearchViewModelTest.kt`

- [ ] **Step 4: Verify tests**
Run `./gradlew :shared:allTests`

- [ ] **Step 5: Commit**
```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/DialogueRepository.kt
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/search/
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/
git add shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/
git commit -m "refactor: migrate Search feature to Domain models"
```

---

### Task 3: Refactor Chat Feature to Domain Models

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/network/DialogueChatRepository.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueChatViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/ConversationItem.kt`
- Modify: Chat UI consumers and Tests

**Interfaces:**
- Consumes: Chat Domain Models from Task 1.

- [ ] **Step 1: Update DialogueChatRepository interface & Implementation**
In `DialogueChatRepository.kt`:
1. Change return type of `generateDialogue` to `GeneratedDialogue`.
2. Change parameter of `saveDialogue` to `GeneratedDialogue`.
3. Add private extension mappers to map `DialogueChatResponse.toDomain()` -> `GeneratedDialogue`.
4. Update `saveDialogue` implementation to map `GeneratedDialogue` to `SaveDialogueRequest`.

- [ ] **Step 2: Update ConversationItem and ViewModel**
In `ConversationItem.kt`: Update `AiResponse` to hold `GeneratedDialogue`.
In `DialogueChatViewModel.kt`:
1. Update `saveDialogue` to accept `GeneratedDialogue`.
2. Fix any type mismatches caused by the new domain objects.

- [ ] **Step 3: Update UI Consumers & Tests**
Run `./gradlew :shared:compileKotlinJvm` to find all UI files and test files that are broken due to signature changes. You will likely need to update:
- `DialogueGenerator.kt` (and any related composables rendering the generated chat).
- `DialogueChatViewModelTest.kt`

- [ ] **Step 4: Verify tests**
Run `./gradlew :shared:allTests`

- [ ] **Step 5: Commit**
```bash
git add .
git commit -m "refactor: migrate Chat feature to Domain models"
```
