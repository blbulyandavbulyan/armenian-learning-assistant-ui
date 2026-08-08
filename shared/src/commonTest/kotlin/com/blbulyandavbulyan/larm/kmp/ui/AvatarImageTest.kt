package com.blbulyandavbulyan.larm.kmp.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.blbulyandavbulyan.larm.kmp.ui.common.AvatarImage
import com.blbulyandavbulyan.larm.kmp.ui.common.extractInitials
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AvatarImageTest {

    @Test
    fun extractInitials_handlesMultipleWords() {
        extractInitials("David Bul") shouldBe "DB"
        extractInitials("John Robert Doe") shouldBe "JR"
    }

    @Test
    fun extractInitials_handlesSingleWord() {
        extractInitials("David") shouldBe "D"
        extractInitials("alice") shouldBe "A"
    }

    @Test
    fun extractInitials_handlesNullAndEmpty() {
        extractInitials(null) shouldBe "U"
        extractInitials("") shouldBe "U"
        extractInitials("   ") shouldBe "U"
    }

    @Test
    fun avatarImage_withNullUrl_rendersInitialsAvatar() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme {
                AvatarImage(
                    avatarUrl = null,
                    displayName = "David Bul",
                    modifier = Modifier.testTag("avatar")
                )
            }
        }

        onNodeWithTag("avatar").assertIsDisplayed()
        onNodeWithText("DB").assertIsDisplayed()
    }

    @Test
    fun avatarImage_withBlankUrl_rendersInitialsAvatar() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme {
                AvatarImage(
                    avatarUrl = "   ",
                    displayName = "Alice Wonderland",
                    size = 48.dp,
                    modifier = Modifier.testTag("avatar")
                )
            }
        }

        onNodeWithTag("avatar").assertIsDisplayed()
        onNodeWithText("AW").assertIsDisplayed()
    }

    @Test
    fun avatarImage_withAvatarUrl_rendersSubcomposeAsyncImage() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme {
                AvatarImage(
                    avatarUrl = "https://example.com/avatar.png",
                    displayName = "David Bul",
                    modifier = Modifier.testTag("avatar")
                )
            }
        }

        onNodeWithTag("avatar").assertIsDisplayed()
    }

    @Test
    fun avatarImage_withDifferentSizes_rendersCorrectly() = runComposeUiTest {
        // TODO this is probably ONLY for 'coverage', this test looks dumb, we set different size, but we don't really assert anything
        //  only that they are 'displayed', but 'how' they displayed??? That's the real point of that 'if check' there
        setContent {
            ArmenianLearningTheme {
                AvatarImage(
                    avatarUrl = null,
                    displayName = "Small Avatar",
                    size = 32.dp,
                    modifier = Modifier.testTag("smallAvatar")
                )
                AvatarImage(
                    avatarUrl = null,
                    displayName = "Large Avatar",
                    size = 48.dp,
                    modifier = Modifier.testTag("largeAvatar")
                )
            }
        }

        onNodeWithTag("smallAvatar").assertIsDisplayed()
        onNodeWithText("SA").assertIsDisplayed()
        onNodeWithTag("largeAvatar").assertIsDisplayed()
        onNodeWithText("LA").assertIsDisplayed()
    }
}
