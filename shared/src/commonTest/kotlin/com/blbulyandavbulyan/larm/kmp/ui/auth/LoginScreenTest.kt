package com.blbulyandavbulyan.larm.kmp.ui.auth

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoginScreenTest {
    @Test
    fun loginScreen_displaysElementsAndTriggersSignIn() = runComposeUiTest {
        var signInClicked = false

        setContent {
            ArmenianLearningTheme {
                LoginScreen(
                    onSignInWithGoogle = { signInClicked = true }
                )
            }
        }
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithTag("authAppTitleText").fetchSemanticsNodes().isNotEmpty()
        }

        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithTag("authWelcomeSubtitleText").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("loginScreen").assertIsDisplayed()
        onNodeWithTag("loginCard").assertIsDisplayed()
        onNodeWithTag("authAppTitleText").assertExists()
        onNodeWithTag("authWelcomeSubtitleText").assertExists()
        onNodeWithTag("signInWithGoogleButton").assertIsDisplayed().performClick()

        signInClicked shouldBe true
    }

    @Test
    fun loginScreen_showsLoadingState_whenIsLoadingIsTrue() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme {
                LoginScreen(
                    onSignInWithGoogle = {},
                    isLoading = true
                )
            }
        }

        onNodeWithTag("signInWithGoogleButton").assertIsNotEnabled()
        onNodeWithTag("signInLoadingIndicator").assertIsDisplayed()
    }
}
