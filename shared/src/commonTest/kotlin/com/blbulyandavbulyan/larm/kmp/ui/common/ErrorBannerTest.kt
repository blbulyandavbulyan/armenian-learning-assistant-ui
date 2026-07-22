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
            onNodeWithText("This should disappear").assertIsDisplayed()
            // Initially, no dismiss event should have been emitted
            dismissEvents.expectNoEvents()

            // Fast-forward time through display + fade durations (150ms total)
            mainClock.advanceTimeBy(150)

            dismissEvents.awaitItem() shouldBe Unit
            dismissEvents.ensureAllEventsConsumed()
            onNodeWithText("Auto Dismiss Test").assertDoesNotExist()
            onNodeWithText("This should disappear").assertDoesNotExist()
        }
    }

    @Test
    fun newErrorResetsTimerAndDoesNotDismissPrematurely() = runComposeUiTest {
        turbineScope {
            val dismissEvents = Turbine<Unit>()

            // Fast-forward initial composition state
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

            // Advance halfway through display time
            mainClock.advanceTimeBy(500)
            dismissEvents.expectNoEvents()

            // Trigger a new error state, restarting LaunchedEffect
            title = "Second Error"
            onNodeWithText("Second Error").assertIsDisplayed()

            // Advance past original timer completion window (another 700ms)
            mainClock.advanceTimeBy(700)
            dismissEvents.expectNoEvents()

            // Advance to complete the restarted timer (1500ms total from second error)
            mainClock.advanceTimeBy(800)

            dismissEvents.awaitItem() shouldBe Unit
            dismissEvents.ensureAllEventsConsumed()
            onNodeWithText("Second Error").assertDoesNotExist()
        }
    }
}
