package com.blbulyandavbulyan.larm.kmp.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import coil3.ColorImage
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.test.FakeImageLoaderEngine
import com.blbulyandavbulyan.larm.kmp.ui.common.AvatarImage
import com.blbulyandavbulyan.larm.kmp.ui.common.extractInitials
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import io.kotest.matchers.shouldBe
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AvatarImageTest {

    @OptIn(DelicateCoilApi::class)
    @AfterTest
    fun tearDown() {
        SingletonImageLoader.reset()
    }

    @Test
    fun extractInitials_handlesTwoWords() {
        extractInitials("david bul") shouldBe "DB"
    }

    @Test
    fun extractInitials_handlesThreeWords() {
        extractInitials("John Robert Doe") shouldBe "JR"
    }

    @Test
    fun extractInitials_handlesSingleWord() {
        extractInitials("David") shouldBe "D"
    }

    @Test
    fun extractInitials_handlesEmpty() {
        extractInitials("") shouldBe "U"
        extractInitials("   ") shouldBe "U"
    }

    @Test
    fun extractInitials_handlesNull() {
        extractInitials(null) shouldBe "U"
    }

    @Test
    fun avatarImage_withNullUrl_rendersInitialsAvatar() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme {
                AvatarImage(
                    avatarUrl = null,
                    displayName = "David Bul",
                )
            }
        }

        onNodeWithTag("avatar_fallback_initials").assertIsDisplayed().assertTextEquals("DB")
        onNodeWithTag("avatar_loading_initials").assertDoesNotExist()
        onNodeWithTag("avatar_error_initials").assertDoesNotExist()
        onNodeWithTag("avatar_coil_image").assertDoesNotExist()
    }

    @Test
    fun avatarImage_withBlankUrl_rendersInitialsAvatar() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme {
                AvatarImage(
                    avatarUrl = "   ",
                    displayName = "Alice Wonderland",
                )
            }
        }

        onNodeWithTag("avatar_fallback_initials").assertIsDisplayed().assertTextEquals("AW")
        onNodeWithTag("avatar_loading_initials").assertDoesNotExist()
        onNodeWithTag("avatar_error_initials").assertDoesNotExist()
        onNodeWithTag("avatar_coil_image").assertDoesNotExist()
    }

    @OptIn(DelicateCoilApi::class)
    @Test
    fun avatarImage_withAvatarUrl_rendersSubcomposeAsyncImage() = runComposeUiTest {
        val engine = FakeImageLoaderEngine.Builder()
            .default(ColorImage(0xFF0000FF.toInt()))
            .build()

        SingletonImageLoader.setUnsafe(
            ImageLoader.Builder(PlatformContext.INSTANCE)
                .components { add(engine) }
                .build()
        )

        setContent {
            ArmenianLearningTheme {
                AvatarImage(
                    avatarUrl = "https://example.com/avatar.png",
                    displayName = "David Bul",
                )
            }
        }

        waitForIdle()
        onNodeWithText("DB").assertDoesNotExist()
        onNodeWithTag("avatar_coil_image").assertIsDisplayed()
        onNodeWithTag("avatar_fallback_initials").assertDoesNotExist()
        onNodeWithTag("avatar_loading_initials").assertDoesNotExist()
        onNodeWithTag("avatar_error_initials").assertDoesNotExist()
    }
}
