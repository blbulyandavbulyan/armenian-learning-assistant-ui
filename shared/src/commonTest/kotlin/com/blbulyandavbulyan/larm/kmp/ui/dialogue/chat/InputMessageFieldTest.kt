package com.blbulyandavbulyan.larm.kmp.ui.dialogue.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InputMessageFieldTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun typingAndPressingEnter() = runComposeUiTest {
        var sendTriggerCount = 0
        var currentPrompt by mutableStateOf("")

        setContent {
            ArmenianLearningTheme {
                InputMessageField(
                    value = currentPrompt,
                    fontFamily = FontFamily.Default,
                    onValueChange = { currentPrompt = it },
                    onSend = { sendTriggerCount++ }
                )
            }
        }

        sendTriggerCount shouldBe 0
        onNodeWithTag("inputMessageField").performTextInput("Barev!")

        onNodeWithTag("inputMessageField").performKeyInput {
            pressKey(Key.Enter)
        }

        sendTriggerCount shouldBe 1
        currentPrompt shouldBe "Barev!"
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun typingAndPressingNumpadEnter() = runComposeUiTest {
        var sendTriggerCount = 0
        var currentPrompt by mutableStateOf("")

        setContent {
            ArmenianLearningTheme {
                InputMessageField(
                    value = currentPrompt,
                    fontFamily = FontFamily.Default,
                    onValueChange = { currentPrompt = it },
                    onSend = { sendTriggerCount++ }
                )
            }
        }

        sendTriggerCount shouldBe 0
        onNodeWithTag("inputMessageField").performTextInput("Barev!")

        onNodeWithTag("inputMessageField").performKeyInput {
            pressKey(Key.NumPadEnter)
        }

        sendTriggerCount shouldBe 1
        currentPrompt shouldBe "Barev!"
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun typingAndPressingShiftEnter() = runComposeUiTest {
        var sendTriggerCount = 0
        var currentPrompt by mutableStateOf("")

        setContent {
            ArmenianLearningTheme {
                InputMessageField(
                    value = currentPrompt,
                    fontFamily = FontFamily.Default,
                    onValueChange = { currentPrompt = it },
                    onSend = { sendTriggerCount++ }
                )
            }
        }

        sendTriggerCount shouldBe 0
        onNodeWithTag("inputMessageField").performTextInput("Barev!")

        onNodeWithTag("inputMessageField").performKeyInput {
            withKeyDown(Key.ShiftLeft) {
                pressKey(Key.Enter)
            }
        }

        sendTriggerCount shouldBe 0
        currentPrompt shouldBe "Barev!\n"
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun typingAndPressingShiftNumpadEnter() = runComposeUiTest {
        var sendTriggerCount = 0
        var currentPrompt by mutableStateOf("")

        setContent {
            ArmenianLearningTheme {
                InputMessageField(
                    value = currentPrompt,
                    fontFamily = FontFamily.Default,
                    onValueChange = { currentPrompt = it },
                    onSend = { sendTriggerCount++ }
                )
            }
        }

        sendTriggerCount shouldBe 0
        onNodeWithTag("inputMessageField").performTextInput("Barev!")

        onNodeWithTag("inputMessageField").performKeyInput {
            withKeyDown(Key.ShiftLeft) {
                pressKey(Key.NumPadEnter)
            }
        }

        sendTriggerCount shouldBe 0
        currentPrompt shouldBe "Barev!\n"
    }
}
