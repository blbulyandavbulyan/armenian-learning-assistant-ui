package com.blbulyandavbulyan.larm.kmp.ui.dialogue.chat

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.withKeyDown
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.GeneratedDialogue
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.GeneratedDialogueMother
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.chat.DialogueChatRepository
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.ConversationItem
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueChatViewModel
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DialogueGeneratorScreenTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun clickingSendWithText_triggersOnGenerateDialogue() = runComposeUiTest {
        var generatedPrompt: String? = null

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = emptyList(),
                    onGenerateDialogue = { generatedPrompt = it },
                    onSaveDialogue = {}
                )
            }
        }

        // Type into the input field
        onNodeWithTag("inputMessageField").performTextInput("Hello, generating a dialogue!")

        // Click the send button
        onNodeWithTag("sendButton").performClick()

        // Assert that the callback was triggered with the correct text
        generatedPrompt shouldBe "Hello, generating a dialogue!"
    }

    @Test
    fun clickingSendWithEmptyText_doesNotTriggerOnGenerateDialogue() = runComposeUiTest {
        var callbackTriggerCount = 0

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = emptyList(),
                    onGenerateDialogue = { callbackTriggerCount++ },
                    onSaveDialogue = {}
                )
            }
        }

        callbackTriggerCount shouldBe 0
        // Click the send button without typing anything
        onNodeWithTag("sendButton").performClick()

        // Assert that the callback was NOT triggered
        callbackTriggerCount shouldBe 0
    }

    @Test
    fun emptyConversation_displaysEmptyMessage() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = emptyList(),
                    emptyMessage = "No conversation yet",
                    onGenerateDialogue = { },
                    onSaveDialogue = {}
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
                    onGenerateDialogue = { generatedPrompt = it },
                    onSaveDialogue = {}
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
                    onGenerateDialogue = { generatedPrompt = it },
                    onSaveDialogue = {}
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

    @Test
    fun userMessage_isDisplayedCorrectly() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = listOf(ConversationItem.UserMessage("Hello user message")),
                    onGenerateDialogue = { },
                    onSaveDialogue = {}
                )
            }
        }

        onNodeWithTag("userMessageText").assertIsDisplayed()
            .assertTextEquals("Hello user message")
    }

    @Test
    fun loadingState_isDisplayedCorrectly() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = listOf(ConversationItem.Loading()),
                    onGenerateDialogue = { },
                    onSaveDialogue = {}
                )
            }
        }

        onNodeWithTag("loadingIndicator").assertIsDisplayed()
    }

    @Test
    fun aiResponse_displaysFullDialogueDataCorrectly() = runComposeUiTest {
        val mockAiResponse = GeneratedDialogueMother.FULL_DIALOGUE_1

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = listOf(ConversationItem.AiResponse(mockAiResponse)),
                    onGenerateDialogue = { },
                    onSaveDialogue = {}
                )
            }
        }

        // Assert AI's initial conversational message is displayed and has correct text
        onNodeWithTag("aiMessageText").assertIsDisplayed()
        onNode(hasText("Here is a dialogue:")).assertIsDisplayed()

        // Assert Dialogue Info is displayed
        onNode(hasText("Խանութում | In the shop")).assertIsDisplayed() // Title + Translation
        onNode(hasText("Khanutum")).assertIsDisplayed() // Transcription

        // Assert Speakers are correctly mapped and displayed
        onAllNodesWithTag("dialogueSpeaker")[0].assertTextEquals("Վաճառող | Seller")
        onAllNodesWithTag("dialogueSpeaker")[1].assertTextEquals("Հաճախորդ | Customer")

        // Assert Phrases are displayed with transcriptions and translations
        onAllNodesWithTag("dialoguePhraseText")[0].assertTextEquals("Բարև Ձեզ")
        onNode(hasText("Barev Dzez")).assertIsDisplayed()
        onNode(hasText("Hello")).assertIsDisplayed()

        onAllNodesWithTag("dialoguePhraseText")[1].assertTextEquals("Ողջույն")
        onNode(hasText("Voghjuyn")).assertIsDisplayed()
        onNode(hasText("Greetings")).assertIsDisplayed()
    }

    @Test
    fun saveButton_triggersCallbackCorrectly_andShowsLoading() = runComposeUiTest {
        val savedDialogues = mutableListOf<GeneratedDialogue>()
        val conversation = listOf(
            ConversationItem.AiResponse(GeneratedDialogueMother.FULL_DIALOGUE_1, isSaving = false, isSaved = false),
            ConversationItem.AiResponse(GeneratedDialogueMother.FULL_DIALOGUE_2, isSaving = true, isSaved = false)
        )

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = conversation,
                    onGenerateDialogue = {},
                    onSaveDialogue = { savedDialogues.add(it) }
                )
            }
        }

        // Verify semantics on the saving button (second item)
        onAllNodesWithTag("saveButton")[1]
            .assert(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))

        // Verify that the other button does not have the indeterminate loading semantics
        onAllNodesWithTag("saveButton")[0]
            .assert(!hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))

        // Click the first button
        onAllNodesWithTag("saveButton")[0].performClick()

        savedDialogues.size shouldBe 1
        savedDialogues[0] shouldBe GeneratedDialogueMother.FULL_DIALOGUE_1
    }

    @Test
    fun dialogueGeneratorScreen_withViewModel_delegatesToViewModel() = runComposeUiTest {
        val mockRepo = mock<DialogueChatRepository>()
        everySuspend { mockRepo.generateDialogue(any(), any()) } returns GeneratedDialogueMother.FULL_DIALOGUE_1
        everySuspend { mockRepo.saveDialogue(any()) } returns "saved-id"

        val viewModel = DialogueChatViewModel(mockRepo, GlobalErrorManager())

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    viewModel = viewModel
                )
            }
        }

        // Type a prompt and send
        val testPrompt = "Test prompt for viewmodel delegation"
        onNodeWithTag("inputMessageField").performTextInput(testPrompt)
        onNodeWithTag("sendButton").performClick()

        waitForIdle()

        // Verify that the viewModel received the prompt via its generateDialogue method
        // which calls the repository under the hood
        verifySuspend { mockRepo.generateDialogue(testPrompt, any()) }

        // Wait for the generated dialogue to appear
        onAllNodesWithTag("saveButton")[0].assertIsDisplayed()

        // Click the save button on the generated dialogue
        onAllNodesWithTag("saveButton")[0].performClick()
        waitForIdle()

        // Verify that the viewModel delegated the save action to the repository
        verifySuspend { mockRepo.saveDialogue(GeneratedDialogueMother.FULL_DIALOGUE_1) }
    }

    @Test
    fun shouldDisplayErrorStateAndRetryButton() = runComposeUiTest {
        var retryId: String? = null
        val errorItem = ConversationItem.Error("failed prompt", id = "error-123")

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = listOf(errorItem),
                    onGenerateDialogue = {},
                    onSaveDialogue = {},
                    onRetryDialogue = { retryId = it }
                )
            }
        }

        retryId shouldBe null

        onNodeWithTag("errorItemView").assertIsDisplayed()
        onNodeWithTag("retryButton").assertIsDisplayed().performClick()

        retryId shouldBe "error-123"
    }
}
