package com.blbulyandavbulyan.larm.kmp.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.text.font.FontFamily
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.test.Test
import io.kotest.matchers.shouldBe

class InputMessageFieldTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun typingAndPressingEnter() = runComposeUiTest {
        var sendTriggered = false
        var currentPrompt by mutableStateOf("")

        setContent {
            ArmenianLearningTheme {
                InputMessageField(
                    value = currentPrompt,
                    fontFamily = FontFamily.Default,
                    onValueChange = { currentPrompt = it },
                    onSend = { sendTriggered = true }
                )
            }
        }

        onNodeWithTag("inputMessageField").performTextInput("Barev!")
        
        onNodeWithTag("inputMessageField").performKeyInput {
            pressKey(Key.Enter)
        }

        sendTriggered shouldBe true
        currentPrompt shouldBe "Barev!"
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun typingAndPressingShiftEnter() = runComposeUiTest {
        var sendTriggered = false
        var currentPrompt by mutableStateOf("")

        setContent {
            ArmenianLearningTheme {
                InputMessageField(
                    value = currentPrompt,
                    fontFamily = FontFamily.Default,
                    onValueChange = { currentPrompt = it },
                    onSend = { sendTriggered = true }
                )
            }
        }

        onNodeWithTag("inputMessageField").performTextInput("Barev!")
        
        onNodeWithTag("inputMessageField").performKeyInput {
            withKeyDown(Key.ShiftLeft) {
                pressKey(Key.Enter)
            }
        }

        sendTriggered shouldBe false
        currentPrompt shouldBe "Barev!\n"
    }
}
