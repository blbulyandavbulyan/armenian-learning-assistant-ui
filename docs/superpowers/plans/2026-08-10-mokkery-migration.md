# Mokkery Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the migration of all remaining tests to use Mokkery mocks and delete leftover manual `Fake` implementations.

**Architecture:** We will replace `FakeAssetRepository`, `FakeDialogueChatRepository`, `FakeDialogueRepository`, and `FakeAudioPlayer` with Mokkery's `mock<T>()` in the remaining three test files. Finally, we will delete the fake implementations entirely.

**Tech Stack:** Kotlin Multiplatform, Mokkery

## Global Constraints

- **Kotlin Version:** Kotlin Multiplatform (as configured in `build.gradle.kts`)
- **Testing Standard:** Kotest assertions (`shouldBe`) and Mokkery for mocking
- **No manual fakes:** All mocking should be handled via `dev.mokkery.mock` and related functions.

---

### Task 1: Refactor DialogueChatViewModelTest

**Files:**
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueChatViewModelTest.kt`

**Interfaces:**
- Consumes: `DialogueChatRepository` interface
- Produces: A fully Mokkery-based `DialogueChatViewModelTest`

- [ ] **Step 1: Replace Fake with Mokkery Mock in DialogueChatViewModelTest**

Remove the usage of `FakeDialogueChatRepository` and replace it with Mokkery's `mock<DialogueChatRepository>()`. Add necessary Mokkery imports (like `import dev.mokkery.mock`, `import dev.mokkery.everySuspend`, `import dev.mokkery.answering.returns`).

```kotlin
// Remove this import:
// import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.chat.FakeDialogueChatRepository

// Add Mokkery imports
import dev.mokkery.mock
import dev.mokkery.everySuspend
import dev.mokkery.answering.returns

// ...

    // Change field type
    private lateinit var mockRepository: DialogueChatRepository

// ...
    @BeforeTest
    fun setUp() {
        mockRepository = mock<DialogueChatRepository>()
        
        // Ensure any setup that was happening with fakeRepository is applied to mockRepository
        everySuspend { mockRepository.sendMessage(any(), any()) } returns Result.success(emptyList()) // Adjust based on actual test needs

        viewModel = DialogueChatViewModel(mockRepository, globalErrorManager)
    }
```

*Note: Update all test methods in this file that manipulated `fakeRepository` properties (like `fakeRepository.shouldFail = true`) to use `everySuspend { ... } returns Result.failure(...)` instead.*

- [ ] **Step 2: Run test to verify it passes**

Run: `./gradlew :shared:cleanTest :shared:testDebugUnitTest --tests "com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueChatViewModelTest"`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/chat/DialogueChatViewModelTest.kt
git commit -m "test: migrate DialogueChatViewModelTest to Mokkery"
```

### Task 2: Refactor DialogueSearchViewModelTest

**Files:**
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/search/DialogueSearchViewModelTest.kt`

**Interfaces:**
- Consumes: `AssetRepository`, `DialogueRepository`, `AudioPlayer`

- [ ] **Step 1: Replace Fakes with Mokkery Mocks in DialogueSearchViewModelTest**

Replace `FakeDialogueRepository`, `FakeAssetRepository`, and `FakeAudioPlayer` with `mock<DialogueRepository>()`, `mock<AssetRepository>()`, and `mock<AudioPlayer>()`. 

```kotlin
// Remove Fake imports
// Add Mokkery imports
import dev.mokkery.mock
import dev.mokkery.everySuspend
import dev.mokkery.answering.returns

// ...
    private lateinit var mockDialogueRepository: DialogueRepository
    private lateinit var mockAssetRepository: AssetRepository
    private lateinit var mockAudioPlayer: AudioPlayer
// ...
    @BeforeTest
    fun setUp() {
        mockDialogueRepository = mock<DialogueRepository>()
        mockAssetRepository = mock<AssetRepository>()
        mockAudioPlayer = mock<AudioPlayer>()
        
        // Define default mock behaviors that previously might have been defaults in Fakes
        
        viewModel = DialogueSearchViewModel(mockDialogueRepository, mockAssetRepository, globalErrorManager, mockAudioPlayer)
    }
```
*Note: Find all places where `fake...shouldFail = true` is used, and replace them with `everySuspend { ... } returns Result.failure(Exception())`.*

- [ ] **Step 2: Run test to verify it passes**

Run: `./gradlew :shared:cleanTest :shared:testDebugUnitTest --tests "com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.DialogueSearchViewModelTest"`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/presentation/dialogue/search/DialogueSearchViewModelTest.kt
git commit -m "test: migrate DialogueSearchViewModelTest to Mokkery"
```

### Task 3: Refactor DialogueGeneratorScreenTest

**Files:**
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/ui/dialogue/chat/DialogueGeneratorScreenTest.kt`

**Interfaces:**
- Consumes: `DialogueChatRepository`

- [ ] **Step 1: Replace Fake in UI Test**

Find where `FakeDialogueChatRepository` is instantiated in `DialogueGeneratorScreenTest.kt` and replace it with `mock<DialogueChatRepository>()`.

```kotlin
// Remove Fake import, add Mokkery imports
import dev.mokkery.mock
import dev.mokkery.everySuspend
import dev.mokkery.answering.returns

// ... Inside the test method ...
        val mockRepo = mock<DialogueChatRepository>()
        everySuspend { mockRepo.sendMessage(any(), any()) } returns Result.success(...)
        // ...
        val viewModel = DialogueChatViewModel(mockRepo, GlobalErrorManager())
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./gradlew :shared:cleanTest :shared:testDebugUnitTest --tests "com.blbulyandavbulyan.larm.kmp.ui.dialogue.chat.DialogueGeneratorScreenTest"`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/ui/dialogue/chat/DialogueGeneratorScreenTest.kt
git commit -m "test: migrate DialogueGeneratorScreenTest to Mokkery"
```

### Task 4: Delete the remaining Fake classes

**Files:**
- Delete: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/domain/asset/repository/FakeAssetRepository.kt`
- Delete: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/domain/dialogue/repository/chat/FakeDialogueChatRepository.kt`
- Delete: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/domain/dialogue/repository/search/FakeDialogueRepository.kt`
- Delete: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/infrastructure/audio/FakeAudioPlayer.kt`

- [ ] **Step 1: Delete the files**

```bash
rm shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/domain/asset/repository/FakeAssetRepository.kt
rm shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/domain/dialogue/repository/chat/FakeDialogueChatRepository.kt
rm shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/domain/dialogue/repository/search/FakeDialogueRepository.kt
rm shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/infrastructure/audio/FakeAudioPlayer.kt
```

- [ ] **Step 2: Run all tests to ensure we didn't miss anything**

Run: `./gradlew :shared:testDebugUnitTest`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "chore(test): remove unused fake implementations"
```
