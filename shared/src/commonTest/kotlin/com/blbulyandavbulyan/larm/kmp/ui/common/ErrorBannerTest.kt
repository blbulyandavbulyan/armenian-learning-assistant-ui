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

            // 1. Advance past display duration delay (100ms)
            mainClock.advanceTimeBy(100)
            dismissEvents.expectNoEvents()

            // 2. Advance past fade duration animation (50ms)
            mainClock.advanceTimeBy(50)

            // 3. Pump frames & idle scheduler so onDismiss() fires
            waitForIdle()

            dismissEvents.awaitItem() shouldBe Unit
            dismissEvents.ensureAllEventsConsumed()
        }
    }

    @Test
    fun newErrorResetsTimerAndDoesNotDismissPrematurely() = runComposeUiTest {
        turbineScope {
            val dismissEvents = Turbine<Unit>()
            var title by mutableStateOf("First Error")

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

            // T = 500ms
            mainClock.advanceTimeBy(500)
            dismissEvents.expectNoEvents()

            // Restart LaunchedEffect timer (0 / 1500ms for "Second Error")
            title = "Second Error"
            onNodeWithText("Second Error").assertIsDisplayed()

            // Advance 1100ms:
            // - Clock is now at 1600ms total test time (past original 1500ms finish line).
            // - New timer is at 1100ms / 1500ms, so it should NOT have dismissed yet.
            mainClock.advanceTimeBy(1100)
            dismissEvents.expectNoEvents()

            // Advance remaining 400ms to complete the new 1500ms cycle
            mainClock.advanceTimeBy(400)
            waitForIdle()

            dismissEvents.awaitItem() shouldBe Unit
            dismissEvents.ensureAllEventsConsumed()
        }
    }
}
