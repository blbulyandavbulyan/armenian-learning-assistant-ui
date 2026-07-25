package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchFieldTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clickingSearchButtonWithEmptyQueryDoesNotTriggerSearch() = runComposeUiTest {
        var searchTriggered = false
        setContent {
            ArmenianLearningTheme {
                SearchField(
                    query = "",
                    textFieldModifier = Modifier.testTag("searchField"),
                    onSearch = { searchTriggered = true },
                    onValueChange = {},
                    placeholder = {}
                )
            }
        }

        onNodeWithTag("searchSubmitButton").performClick()
        searchTriggered shouldBe false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clickingSearchButtonWithValidQueryTriggersSearch() = runComposeUiTest {
        var searchTriggered = false
        setContent {
            ArmenianLearningTheme {
                SearchField(
                    query = "Barev",
                    textFieldModifier = Modifier.testTag("searchField"),
                    onSearch = { searchTriggered = true },
                    onValueChange = {},
                    placeholder = {}
                )
            }
        }

        onNodeWithTag("searchSubmitButton").performClick()
        searchTriggered shouldBe true
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun typingInFieldTriggersOnValueChange() = runComposeUiTest {
        var query by mutableStateOf("")
        setContent {
            ArmenianLearningTheme {
                SearchField(
                    query = query,
                    textFieldModifier = Modifier.testTag("searchField"),
                    onSearch = {},
                    onValueChange = { query = it },
                    placeholder = {}
                )
            }
        }

        onNodeWithTag("searchField").performTextInput("Barev")
        query shouldBe "Barev"
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun imeActionSearchWithEmptyQueryDoesNotTriggerSearch() = runComposeUiTest {
        var searchTriggered = false
        setContent {
            ArmenianLearningTheme {
                SearchField(
                    query = "",
                    textFieldModifier = Modifier.testTag("searchField"),
                    onSearch = { searchTriggered = true },
                    onValueChange = {},
                    placeholder = {}
                )
            }
        }

        onNodeWithTag("searchField").performImeAction()
        searchTriggered shouldBe false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun imeActionSearchWithValidQueryTriggersSearch() = runComposeUiTest {
        var searchTriggered = false
        setContent {
            ArmenianLearningTheme {
                SearchField(
                    query = "Barev",
                    textFieldModifier = Modifier.testTag("searchField"),
                    onSearch = { searchTriggered = true },
                    onValueChange = {},
                    placeholder = {}
                )
            }
        }

        onNodeWithTag("searchField").performImeAction()
        searchTriggered shouldBe true
    }
}
