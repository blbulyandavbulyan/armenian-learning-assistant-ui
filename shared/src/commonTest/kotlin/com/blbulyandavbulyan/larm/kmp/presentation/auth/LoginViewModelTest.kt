package com.blbulyandavbulyan.larm.kmp.presentation.auth

import app.cash.turbine.test
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.auth_error_title
import com.blbulyandavbulyan.larm.kmp.core.UiText
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.auth.FakeAuthRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var globalErrorManager: GlobalErrorManager
    private lateinit var viewModel: LoginViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        globalErrorManager = GlobalErrorManager()
        viewModel = LoginViewModel(fakeAuthRepository, globalErrorManager)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithGoogle calls repository and updates loading state`() = runTest {
        fakeAuthRepository.signInDelayMs = 100L

        viewModel.isLoading.test {
            awaitItem() shouldBe false

            viewModel.signInWithGoogle()
            awaitItem() shouldBe true

            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe false
        }

        fakeAuthRepository.signInCalled shouldBe true
    }

    @Test
    fun `signInWithGoogle emits error to GlobalErrorManager on failure`() = runTest {
        fakeAuthRepository.shouldThrowOnSignIn = true

        globalErrorManager.currentError.test {
            awaitItem() shouldBe null // Initial state

            viewModel.signInWithGoogle()
            testDispatcher.scheduler.advanceUntilIdle()

            val error = awaitItem()
            error shouldNotBe null
            error?.title shouldBe UiText.from(Res.string.auth_error_title)
            viewModel.isLoading.value shouldBe false
        }
    }
}
