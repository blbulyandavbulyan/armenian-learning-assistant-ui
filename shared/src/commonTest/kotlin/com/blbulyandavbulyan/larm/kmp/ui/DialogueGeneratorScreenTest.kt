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
    // TODO not everything is covered, check out code coverage
    //  some important use cases are missing from here
    //  we should check that the phrases are displayed, messages from user and from model are displayed,
    //  speakers and all stuff related to them - displayed.
    //  Meaning that if we display something on ui which comes from our backend - we must ensure that it is actually there
}
