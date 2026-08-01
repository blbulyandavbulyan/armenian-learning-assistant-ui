package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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

        onNodeWithText("Armen Sarkisyan").assertExists()
        onNodeWithText("tester@example.com").assertExists()
    }

    @Test
    fun should_invoke_sign_out_when_sign_out_clicked() = runComposeUiTest {
        var signOutClicked = false

        setContent {
            AppDrawerContent(
                userProfile = null,
                currentScreen = ScreenState.Generator,
                onNavigateToGenerator = {},
                onSignOut = { signOutClicked = true }
            )
        }

        onNodeWithTag("drawer_sign_out_item").performClick()
        signOutClicked shouldBe true
    }

    @Test
    fun should_invoke_navigate_generator_when_generator_item_clicked() = runComposeUiTest {
        var navClicked = false

        setContent {
            AppDrawerContent(
                userProfile = null,
                currentScreen = ScreenState.Search,
                onNavigateToGenerator = { navClicked = true },
                onSignOut = {}
            )
        }

        onNodeWithTag("drawer_nav_generator").performClick()
        navClicked shouldBe true
    }
}
