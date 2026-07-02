package com.blbulyandavbulyan.larm.kmp.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.input.key.Key

@OptIn(ExperimentalTestApi::class)
class DialogueGeneratorScreenTest {

    @Test
    fun clickingSendWithText_triggersOnGenerateDialogue() = runComposeUiTest {
        var generatedPrompt: String? = null

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = emptyList(),
                    onGenerateDialogue = { generatedPrompt = it }
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
        var callbackTriggered = false

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = emptyList(),
                    onGenerateDialogue = { callbackTriggered = true }
                )
            }
        }

        // Click the send button without typing anything
        onNodeWithTag("sendButton").performClick()
        
        // Assert that the callback was NOT triggered
        callbackTriggered shouldBe false
    }
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

    @Test
    fun userMessage_isDisplayedCorrectly() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = listOf(com.blbulyandavbulyan.larm.kmp.presentation.ConversationItem.UserMessage("Hello user message")),
                    onGenerateDialogue = { }
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
                    conversation = listOf(com.blbulyandavbulyan.larm.kmp.presentation.ConversationItem.Loading),
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
                    conversation = listOf(com.blbulyandavbulyan.larm.kmp.presentation.ConversationItem.Error("Network failure")),
                    onGenerateDialogue = { }
                )
            }
        }
        
        onNodeWithTag("errorMessage").assertIsDisplayed()
        onNode(androidx.compose.ui.test.hasText("Network failure", substring = true)).assertIsDisplayed()
    }

    @Test
    fun aiResponse_displaysFullDialogueDataCorrectly() = runComposeUiTest {
        val mockAiResponse = com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse(
            message = "Here is your dialogue",
            info = com.blbulyandavbulyan.larm.kmp.data.DialogueTitleResponse(
                title = "Սրճարանում",
                transcription = "Srcharanum",
                translations = listOf(com.blbulyandavbulyan.larm.kmp.data.ChatTranslationResponse("At the Cafe", "en"))
            ),
            speakers = listOf(
                com.blbulyandavbulyan.larm.kmp.data.SpeakerResponse("s1", "Մատուցող", "Matutsogh", listOf(com.blbulyandavbulyan.larm.kmp.data.ChatTranslationResponse("Waiter", "en"))),
                com.blbulyandavbulyan.larm.kmp.data.SpeakerResponse("s2", "Հաճախորդ", "Hachakhord", listOf(com.blbulyandavbulyan.larm.kmp.data.ChatTranslationResponse("Customer", "en")))
            ),
            dialoguePhrases = listOf(
                com.blbulyandavbulyan.larm.kmp.data.DialoguePhraseResponse(
                    speakerId = "s1",
                    phrase = com.blbulyandavbulyan.larm.kmp.data.DraftPhrasesResponse(
                        phrase = "Բարև Ձեզ",
                        isoLanguageCode = "hy",
                        transcription = "Barev Dzez",
                        translations = listOf(com.blbulyandavbulyan.larm.kmp.data.ChatTranslationResponse("Hello", "en"))
                    )
                ),
                com.blbulyandavbulyan.larm.kmp.data.DialoguePhraseResponse(
                    speakerId = "s2",
                    phrase = com.blbulyandavbulyan.larm.kmp.data.DraftPhrasesResponse(
                        phrase = "Բարև",
                        isoLanguageCode = "hy",
                        transcription = "Barev",
                        translations = listOf(com.blbulyandavbulyan.larm.kmp.data.ChatTranslationResponse("Hi", "en"))
                    )
                )
            )
        )

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueGeneratorScreen(
                    conversation = listOf(com.blbulyandavbulyan.larm.kmp.presentation.ConversationItem.AiResponse(mockAiResponse)),
                    onGenerateDialogue = { }
                )
            }
        }
        
        // Assert AI's initial conversational message is displayed and has correct text
        onNodeWithTag("aiMessageText").assertIsDisplayed()
        onNode(androidx.compose.ui.test.hasText("Here is your dialogue")).assertIsDisplayed()
        
        // Assert Dialogue Info is displayed
        onNode(androidx.compose.ui.test.hasText("Սրճարանում | At the Cafe")).assertIsDisplayed() // Title + Translation
        onNode(androidx.compose.ui.test.hasText("Srcharanum")).assertIsDisplayed() // Transcription
        
        // Assert Speakers are correctly mapped and displayed
        onAllNodesWithTag("dialogueSpeaker")[0].assertTextEquals("Մատուցող | Waiter")
        onAllNodesWithTag("dialogueSpeaker")[1].assertTextEquals("Հաճախորդ | Customer")

        // Assert Phrases are displayed with transcriptions and translations
        onAllNodesWithTag("dialoguePhraseText")[0].assertTextEquals("Բարև Ձեզ")
        onNode(androidx.compose.ui.test.hasText("Barev Dzez")).assertIsDisplayed()
        onNode(androidx.compose.ui.test.hasText("Hello")).assertIsDisplayed()
        
        onAllNodesWithTag("dialoguePhraseText")[1].assertTextEquals("Բարև")
        onNode(androidx.compose.ui.test.hasText("Barev")).assertIsDisplayed()
        onNode(androidx.compose.ui.test.hasText("Hi")).assertIsDisplayed()
    }
}
