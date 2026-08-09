package com.blbulyandavbulyan.larm.kmp.ui.auth

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthRepository
import com.blbulyandavbulyan.larm.kmp.presentation.auth.LoginViewModel
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoginScreenTest {
    @Test
    fun loginScreen_displaysElementsAndTriggersSignIn() = runComposeUiTest {
        var signInClickCount = 0

        setContent {
            ArmenianLearningTheme {
                LoginScreen(
                    onSignInWithGoogle = { signInClickCount++ }
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
        signInClickCount shouldBe 0
        onNodeWithTag("signInWithGoogleButton").assertIsDisplayed().performClick()

        signInClickCount shouldBe 1
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

    @Test
    fun loginScreen_withViewModel_delegatesCorrectly() = runComposeUiTest {
        val authRepository = mock<AuthRepository>()
        everySuspend { authRepository.signInWithGoogle() } returns Unit

        val viewModel = LoginViewModel(
            authRepository = authRepository,
            globalErrorManager = GlobalErrorManager()
        )

        setContent {
            ArmenianLearningTheme {
                LoginScreen(viewModel = viewModel)
            }
        }

        onNodeWithTag("signInWithGoogleButton").assertIsDisplayed().performClick()

        waitForIdle()
        verifySuspend { authRepository.signInWithGoogle() }
    }
}
