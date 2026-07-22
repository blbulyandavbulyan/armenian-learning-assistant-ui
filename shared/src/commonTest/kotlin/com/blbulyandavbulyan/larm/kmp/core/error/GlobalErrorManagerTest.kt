package com.blbulyandavbulyan.larm.kmp.core.error

import com.blbulyandavbulyan.larm.kmp.core.UiText
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GlobalErrorManagerTest {

    private lateinit var globalErrorManager: GlobalErrorManager

    @BeforeTest
    fun setup() {
        globalErrorManager = GlobalErrorManager()
    }

    @Test
    fun `initial state should have null currentError`() {
        globalErrorManager.currentError.value shouldBe null
    }

    @Test
    fun `showError should update currentError with given title and message`() {
        val title = UiText.DynamicString("Error Title")
        val message = UiText.DynamicString("Error Message")

        globalErrorManager.showError(title, message)

        val error = globalErrorManager.currentError.value
        error shouldNotBe null
        error?.title shouldBe title
        error?.message shouldBe message
    }

    @Test
    fun `dismissError should set currentError back to null`() = runTest {
        val title = UiText.DynamicString("Error Title")
        val message = UiText.DynamicString("Error Message")

        globalErrorManager.showError(title, message)
        globalErrorManager.currentError.value shouldNotBe null

        globalErrorManager.dismissError()

        globalErrorManager.currentError.value shouldBe null
    }
}
