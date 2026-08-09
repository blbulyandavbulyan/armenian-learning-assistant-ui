package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile
import com.blbulyandavbulyan.larm.kmp.presentation.global.ScreenState
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppDrawerContentTest {

    @Test
    fun should_display_user_profile_info() = runComposeUiTest {
        val testProfile = UserProfile(
            id = "user-123",
            email = "tester@example.com",
            displayName = "Armen Sarkisyan",
            avatarUrl = null
        )

        setContent {
            AppDrawerContent(
                userProfile = testProfile,
                currentScreen = ScreenState.Generator,
                onNavigateToGenerator = {},
                onSignOut = {}
            )
        }

        onNodeWithTag("drawer_profile_name").assertIsDisplayed()
            .assertTextEquals("Armen Sarkisyan")
        onNodeWithTag("drawer_profile_email").assertIsDisplayed()
            .assertTextEquals("tester@example.com")
        onNodeWithTag("drawer_profile_avatar").assertExists()
    }

    @Test
    fun should_not_display_email_if_it_is_equal_to_display_name() = runComposeUiTest {
        val testProfile = UserProfile(
            id = "user-123",
            email = "tester@example.com",
            displayName = "tester@example.com",
            avatarUrl = null
        )

        setContent {
            AppDrawerContent(
                userProfile = testProfile,
                currentScreen = ScreenState.Generator,
                onNavigateToGenerator = {},
                onSignOut = {}
            )
        }

        onNodeWithTag("drawer_profile_name").assertIsDisplayed()
            .assertTextEquals("tester@example.com")
        onNodeWithTag("drawer_profile_email").assertDoesNotExist()
    }

    @Test
    fun should_invoke_sign_out_when_sign_out_clicked() = runComposeUiTest {
        var signOutClickCount = 0

        setContent {
            AppDrawerContent(
                userProfile = null,
                currentScreen = ScreenState.Generator,
                onNavigateToGenerator = {},
                onSignOut = { signOutClickCount++ }
            )
        }

        signOutClickCount shouldBe 0
        onNodeWithTag("drawer_sign_out_item").performClick()
        signOutClickCount shouldBe 1
    }

    @Test
    fun should_invoke_navigate_generator_when_generator_item_clicked() = runComposeUiTest {
        var navClickCount = 0

        setContent {
            AppDrawerContent(
                userProfile = null,
                currentScreen = ScreenState.Search,
                onNavigateToGenerator = { navClickCount++ },
                onSignOut = {}
            )
        }

        navClickCount shouldBe 0
        onNodeWithTag("drawer_nav_generator").performClick()
        navClickCount shouldBe 1
    }
}
