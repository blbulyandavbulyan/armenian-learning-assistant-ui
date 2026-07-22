package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.cash.turbine.Turbine
import app.cash.turbine.turbineScope
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class ErrorBannerTest {

    @Test
    fun errorTitleAndMessageAreDisplayed() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme {
                ErrorBanner(
                    errorTitle = "Connection Failed",
                    errorMessage = "Please check your internet settings.",
                    onDismiss = {}
                )
            }
        }

        onNodeWithText("Connection Failed").assertIsDisplayed()
        onNodeWithText("Please check your internet settings.").assertIsDisplayed()
    }

    @Test
    fun clickingDismissButtonTriggersOnDismiss() = runComposeUiTest {
        turbineScope {
            val dismissEvents = Turbine<Unit>()

            setContent {
                ArmenianLearningTheme {
                    ErrorBanner(
                        errorTitle = "Warning",
                        errorMessage = "Something went wrong",
                        onDismiss = { dismissEvents.add(Unit) }
                    )
                }
            }

            onNodeWithTag("dismissErrorBannerButton").performClick()

            dismissEvents.awaitItem() shouldBe Unit
            dismissEvents.ensureAllEventsConsumed()
        }
    }

    @Test
    fun bannerAutomaticallyDismissesAfterTotalDuration() = runComposeUiTest {
        turbineScope {
            val dismissEvents = Turbine<Unit>()

            // Disable auto-advancing so we explicitly step the clock
            mainClock.autoAdvance = false

            setContent {
                ArmenianLearningTheme {
                    ErrorBanner(
                        errorTitle = "Auto Dismiss Test",
                        errorMessage = "This should disappear",
                        displayDuration = 100.milliseconds,
                        fadeDuration = 50.milliseconds,
                        onDismiss = { dismissEvents.add(Unit) }
                    )
                }
            }

            onNodeWithText("Auto Dismiss Test").assertIsDisplayed()
            dismissEvents.expectNoEvents()

            // Step clock in frames through display (100ms) + fade (50ms) + buffer frame
            mainClock.advanceTimeBy(160)
            mainClock.autoAdvance = true

            dismissEvents.awaitItem() shouldBe Unit
            dismissEvents.ensureAllEventsConsumed()
        }
    }

    @Test
    fun newErrorResetsTimerAndDoesNotDismissPrematurely() = runComposeUiTest {
        turbineScope {
            val dismissEvents = Turbine<Unit>()
            var title by mutableStateOf("First Error")

            mainClock.autoAdvance = false

            setContent {
                ArmenianLearningTheme {
                    ErrorBanner(
                        errorTitle = title,
                        errorMessage = "Error description",
                        displayDuration = 1.seconds,
                        fadeDuration = 500.milliseconds,
                        onDismiss = { dismissEvents.add(Unit) }
                    )
                }
            }

            // Step halfway through initial display time (500ms)
            mainClock.advanceTimeBy(500)
            dismissEvents.expectNoEvents()

            // Trigger state change
            title = "Second Error"

            // Allow Compose to recompose under frame clock
            mainClock.advanceTimeByFrame()
            onNodeWithText("Second Error").assertIsDisplayed()

            // Advance 700ms (Past original 1500ms deadline from start, but inside new timer window)
            mainClock.advanceTimeBy(700)
            dismissEvents.expectNoEvents()

            // Advance remaining 800ms + frame buffer to finish animation and fire callback
            mainClock.advanceTimeBy(816)
            mainClock.autoAdvance = true

            dismissEvents.awaitItem() shouldBe Unit
            dismissEvents.ensureAllEventsConsumed()
        }
    }
}
