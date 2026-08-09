package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppTopBarTest {

    @Test
    fun should_always_show_hamburger_button_and_trigger_callback() = runComposeUiTest {
        var drawerOpenClickCount = 0

        setContent {
            AppTopBar(
                onOpenDrawer = { drawerOpenClickCount++ },
                onBack = null
            )
        }
        drawerOpenClickCount shouldBe 0

        onNodeWithTag("hamburger_button").assertExists().performClick()
        drawerOpenClickCount shouldBe 1
        onNodeWithTag("top_bar_back_button").assertDoesNotExist()
    }

    @Test
    fun should_show_back_button_when_on_back_is_provided() = runComposeUiTest {
        var backClickCount = 0

        setContent {
            AppTopBar(
                onOpenDrawer = {},
                onBack = { backClickCount++ }
            )
        }
        backClickCount shouldBe 0
        onNodeWithTag("hamburger_button").assertExists()
        onNodeWithTag("top_bar_back_button").assertExists().performClick()
        backClickCount shouldBe 1
    }

    @Test
    fun should_render_center_content() = runComposeUiTest {
        setContent {
            AppTopBar(
                onOpenDrawer = {},
                onBack = null,
                centerContent = {
                    Text("Custom Center Slot")
                }
            )
        }

        onNodeWithText("Custom Center Slot").assertExists()
    }
}
