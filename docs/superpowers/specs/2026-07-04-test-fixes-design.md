# Design Spec: Test Scope Fixes & TODO Resolutions

## Overview
This document outlines the approach to resolving the test-related `TODO` comments introduced in the last commit for the Armenian Learning Assistant KMP project. The primary goal is to ensure robust test coverage for the newly added `saveDialogue` functionality across the network, presentation, and UI layers.

## 1. Object Mother Pattern (`DialogueChatResponseMother`)
**Location:** `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/data/DialogueChatResponseMother.kt`

**Design:**
- Create an `object DialogueChatResponseMother`.
- Extract the fully-populated mock response currently residing in `DialogueGeneratorScreenTest.kt` (`aiResponse_displaysFullDialogueDataCorrectly`) into a constant `val FULL_DIALOGUE_1`.
- Declare a second constant `val FULL_DIALOGUE_2` (with distinct values) directly in the Mother to be used in UI and ViewModel concurrency tests, avoiding the need for a dynamic `create()` helper.

## 2. Network Layer Tests (`ApiClientTest` & `NetworkDialogueRepositoryTest`)
**Design:**
- **ApiClientTest**: 
  - Mock a `POST` request to `/dialogues`.
  - Pass `DialogueChatResponseMother.FULL_DIALOGUE` to `saveDialogue`.
  - Assert that the correct JSON body is sent and the proper String response (ID) is returned.
- **NetworkDialogueRepositoryTest**:
  - Update the existing `generateDialogue` test to use `DialogueChatResponseMother.FULL_DIALOGUE` as the expected output.
  - Implement a `saveDialogue` test mirroring the above, passing the Mother's dialogue and asserting the network path (`/dialogues`) and HTTP method (`POST`) are correct.

## 3. Presentation Layer Tests (`DialogueViewModelTest`)
**Design:**
- **State Control**: Update `FakeDialogueRepository` with a `saveCompletable: CompletableDeferred<String>? = null` (or similar mechanism) for `saveDialogue`. This allows the test execution to pause mid-save.
- **Concurrency Test**: 
  1. Pre-populate the ViewModel's state with two generated dialogues (A and B).
  2. Call `saveDialogue(A)` and `saveDialogue(B)`.
  3. Use Turbine to await the state emission where both `A` and `B` have `isSaving = true`.
  4. Complete the deferred for B successfully, and throw an exception for A.
  5. Assert that the emitted state updates `B` to `isSaving = false, isSaved = true` and `A` to `isSaving = false, isSaved = false`, while also appending a `ConversationItem.Error` to the list.
  6. Assert that the objects passed into the Fake Repository match the exact `DialogueChatResponse` objects for A and B.
- **Single Save Success Test**:
  1. Pre-populate the ViewModel's state with two dialogues.
  2. Call `saveDialogue` and complete it successfully.
  3. Assert that the Fake Repository was invoked with the exact dialogue parameter.
  4. Assert the final state updates to `isSaving = false` and `isSaved = true`.
## 4. UI Layer Tests (`DialogueGeneratorScreenTest`)
**Design:**
- **Initial Setup**: Inject a pre-filled `List<ConversationItem>` containing two `AiResponse` items into `DialogueGeneratorScreen`. Pass a capturing lambda for `onSaveDialogue`.
- **Interaction**: 
  - Using Compose test tags, click the "Save" button for the *second* dialogue first, followed by the *first* dialogue.
- **Assertions**:
  - Verify that the captured list in the `onSaveDialogue` lambda has a size of 2.
  - Assert that `capturedList[0]` is exactly Dialogue 2, and `capturedList[1]` is Dialogue 1.
  - Mock the state change (simulating ViewModel updating `isSaved = true`) and assert that the `SaveButton` for the saved dialogues becomes disabled.
  // TODO is there a native semantics in jetpack compose to indicate that 'stuff is in progress'?
  - **Saving State (Semantics)**: Add a `semantics { stateDescription = "saving" }` modifier to the `SaveButton` when `isSaving` is true, and assert this semantic state exists during the save operation, ensuring the UI logically represents the loading/spinning state without brittle pixel tests.

## Self-Review Checklist
- [x] Placeholder check: No vague TODOs left in this spec.
- [x] Consistency check: The UI, Presentation, and Network layer tests all consistently rely on the new `DialogueChatResponseMother`.
- [x] Scope check: Scope matches exactly the TODOs found in the last commit.
- [x] Ambiguity check: The mechanics of the concurrent ViewModel test are explicitly defined via `CompletableDeferred`.
