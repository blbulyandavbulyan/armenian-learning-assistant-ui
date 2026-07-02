# Dialogue Generator UI Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement comprehensive UI tests for `DialogueGeneratorScreen` to ensure the correct display of state variants (Empty, User Message, Loading, Error, AI Response) and proper functioning of custom hotkeys.

**Architecture:** Use Kotlin Multiplatform's `@OptIn(ExperimentalTestApi::class)` and `runComposeUiTest` from `compose.ui.test` to mount the Compose UI. Use standard Kotest assertions for any logic checks. Add missing `testTag` modifiers to UI components in `DialogueGenerator.kt` to allow robust test selection without relying on exact localized strings.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Jetpack Compose UI Testing (`onNodeWithTag`), Kotest.

## Global Constraints

- Must run as JVM UI Tests (run using `./gradlew :shared:jvmTest`).
- Assertions MUST use Kotest (`shouldBe`, `shouldNotBeNull()`, etc.) where applicable, though Compose assertions (`assertIsDisplayed()`) can be used for UI nodes.
- Must preserve the two existing tests for the Send Button logic.

---

### Task 1: Empty State & Custom Hotkeys (Shift+Enter vs Enter)

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGenerator.kt`
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGeneratorScreenTest.kt`

**Interfaces:**
- Consumes: `DialogueGeneratorScreen`, `ConversationItem`, Compose UI Testing Key Events.

- [ ] **Step 1: Add a testTag to the EmptyState text**

Modify `DialogueGenerator.kt` to add a `testTag` to the empty conversation text:

```kotlin
@Composable
private fun BoxScope.EmptyConversationScreen() {
    Text(
        text = stringResource(Res.string.empty_conversation_message),
        style = MaterialTheme.typography.bodyLarge,
        color = AppTheme.colors.emptyMessage,
        modifier = Modifier.align(Alignment.Center).testTag("emptyConversationText")
    )
}
```

- [ ] **Step 2: Write tests for empty state and keyboard shortcuts**

In `DialogueGeneratorScreenTest.kt`, add:

```kotlin
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.input.key.Key

    @Test
    fun emptyConversation_displaysEmptyMessage() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = emptyList(),
                    onGenerateDialogue = { }
                )
            }
        }
        
        onNodeWithTag("emptyConversationText").assertIsDisplayed()
        onNodeWithTag("conversationScreen").assertDoesNotExist()
    }

    @Test
    fun pressingEnterWithoutShift_triggersOnGenerateDialogue() = runComposeUiTest {
        var generatedPrompt: String? = null
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = emptyList(),
                    onGenerateDialogue = { generatedPrompt = it }
                )
            }
        }

        onNodeWithTag("inputMessageField").performTextInput("Hello via enter")
        onNodeWithTag("inputMessageField").performKeyInput { pressKey(Key.Enter) }
        
        generatedPrompt shouldBe "Hello via enter"
    }

    @Test
    fun pressingShiftEnter_doesNotTriggerOnGenerateDialogue() = runComposeUiTest {
        var generatedPrompt: String? = null
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = emptyList(),
                    onGenerateDialogue = { generatedPrompt = it }
                )
            }
        }

        onNodeWithTag("inputMessageField").performTextInput("Hello via shift enter")
        onNodeWithTag("inputMessageField").performKeyInput { 
            withKeyDown(Key.ShiftLeft) {
                pressKey(Key.Enter)
            }
        }
        
        generatedPrompt shouldBe null
    }
```

- [ ] **Step 3: Run test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "com.blbulyandavbulyan.larm.kmp.ui.DialogueGeneratorScreenTest"`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGenerator.kt shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGeneratorScreenTest.kt
git commit -m "test: add ui tests for empty state and keyboard shortcuts"
```

### Task 2: User Message, Loading, and Error States Test

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGenerator.kt`
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGeneratorScreenTest.kt`

**Interfaces:**
- Consumes: `ConversationItem.UserMessage`, `ConversationItem.Loading`, `ConversationItem.Error`.

- [ ] **Step 1: Add test tags for Loading and Error states**

Modify `DialogueGenerator.kt` inside the `ConversationScreen` component to add `testTag`s to the Loading and Error items:

```kotlin
                    is ConversationItem.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("loadingIndicator"),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    is ConversationItem.Error -> {
                        Text(
                            text = "${stringResource(Res.string.error_prefix)} ${item.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("errorMessage")
                        )
                    }
```
Modify `UserMessageView` to attach a tag to the user message text:
```kotlin
            Text(
                text = text,
                modifier = Modifier.padding(16.dp).testTag("userMessageText"),
                // ...
            )
```

Also add a tag to `ConversationScreen` inside `DialogueGenerator.kt`:
```kotlin
    SelectionContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("conversationScreen"),
            // ...
```

- [ ] **Step 2: Write tests for these conversation states**

In `DialogueGeneratorScreenTest.kt`:

```kotlin
import androidx.compose.ui.test.hasText
import com.blbulyandavbulyan.larm.kmp.presentation.ConversationItem

    @Test
    fun userMessage_isDisplayedCorrectly() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = listOf(ConversationItem.UserMessage("Hello user message")),
                    onGenerateDialogue = { }
                )
            }
        }
        
        onNodeWithTag("userMessageText").assertIsDisplayed()
        onNode(hasText("Hello user message")).assertIsDisplayed()
    }

    @Test
    fun loadingState_isDisplayedCorrectly() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = listOf(ConversationItem.Loading),
                    onGenerateDialogue = { }
                )
            }
        }
        
        onNodeWithTag("loadingIndicator").assertIsDisplayed()
    }

    @Test
    fun errorState_isDisplayedCorrectly() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = listOf(ConversationItem.Error("Network failure")),
                    onGenerateDialogue = { }
                )
            }
        }
        onNodeWithTag("errorMessage").assertIsDisplayed()
        onNode(androidx.compose.ui.test.hasText("Network failure", substring = true)).assertIsDisplayed()
    }
```

- [ ] **Step 3: Run test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "com.blbulyandavbulyan.larm.kmp.ui.DialogueGeneratorScreenTest"`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGenerator.kt shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGeneratorScreenTest.kt
git commit -m "test: add ui tests for UserMessage, Loading, and Error conversation items"
```

### Task 3: Full AI Response UI Test

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueView.kt`
- Modify: `shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGeneratorScreenTest.kt`

**Interfaces:**
- Consumes: `DialogueChatResponse`, `DialogueInfo`, `DialoguePhrase`, `SpeakerResponse`

- [ ] **Step 1: Add a testTag to the AI response text bubble**

Modify `AiMessageBubble` in `DialogueView.kt` to attach a tag to the message text:

```kotlin
            Text(
                text = message,
                modifier = Modifier.padding(16.dp).testTag("aiMessageText"),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = fontFamily
                )
            )
```

- [ ] **Step 2: Write test for AI Response rendering**

In `DialogueGeneratorScreenTest.kt`:

```kotlin
import com.blbulyandavbulyan.larm.kmp.data.*

    @Test
    fun aiResponse_displaysFullDialogueDataCorrectly() = runComposeUiTest {
        val mockAiResponse = DialogueChatResponse(
            message = "Here is your dialogue",
            info = DialogueInfo(
                title = "Սրճարանում",
                transcription = "Srcharanum",
                translations = listOf(TranslationResponse("en", "At the Cafe"))
            ),
            speakers = listOf(
                SpeakerResponse("s1", "Մատուցող", listOf(TranslationResponse("en", "Waiter"))),
                SpeakerResponse("s2", "Հաճախորդ", listOf(TranslationResponse("en", "Customer")))
            ),
            dialoguePhrases = listOf(
                DialoguePhraseObj(
                    speakerId = "s1",
                    phrase = DialoguePhrase(
                        phrase = "Բարև Ձեզ",
                        transcription = "Barev Dzez",
                        translations = listOf(TranslationResponse("en", "Hello"))
                    )
                ),
                DialoguePhraseObj(
                    speakerId = "s2",
                    phrase = DialoguePhrase(
                        phrase = "Բարև",
                        transcription = "Barev",
                        translations = listOf(TranslationResponse("en", "Hi"))
                    )
                )
            )
        )

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = listOf(ConversationItem.AiResponse(mockAiResponse)),
                    onGenerateDialogue = { }
                )
            }
        }
        
        // Assert AI's initial conversational message is displayed and has correct text
        onNodeWithTag("aiMessageText").assertIsDisplayed()
        onNode(hasText("Here is your dialogue")).assertIsDisplayed()
        
        // Assert Dialogue Info is displayed
        onNode(hasText("Սրճարանում | At the Cafe")).assertIsDisplayed() // Title + Translation
        onNode(hasText("Srcharanum")).assertIsDisplayed() // Transcription
        
        // Assert Speakers are correctly mapped and displayed
        onNode(hasText("Մատուցող | Waiter")).assertIsDisplayed()
        onNode(hasText("Հաճախորդ | Customer")).assertIsDisplayed()

        // Assert Phrases are displayed with transcriptions and translations
        onNode(hasText("Բարև Ձեզ")).assertIsDisplayed()
        onNode(hasText("Barev Dzez")).assertIsDisplayed()
        onNode(hasText("Hello")).assertIsDisplayed()
        
        onNode(hasText("Բարև")).assertIsDisplayed()
        onNode(hasText("Barev")).assertIsDisplayed()
        onNode(hasText("Hi")).assertIsDisplayed()
    }
```

- [ ] **Step 3: Run test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "com.blbulyandavbulyan.larm.kmp.ui.DialogueGeneratorScreenTest"`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueView.kt shared/src/commonTest/kotlin/com/blbulyandavbulyan/larm/kmp/ui/DialogueGeneratorScreenTest.kt
git commit -m "test: add ui test for full AI Response dialogue display"
```
