package com.blbulyandavbulyan.larm.kmp.presentation.auth

import app.cash.turbine.test
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.auth_error_title
import com.blbulyandavbulyan.larm.kmp.core.UiText
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthRepository
import dev.mokkery.answering.calls
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.CompletableDeferred
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
    private val authRepository = mock<AuthRepository>()
    private lateinit var globalErrorManager: GlobalErrorManager
    private lateinit var viewModel: LoginViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        globalErrorManager = GlobalErrorManager()
        viewModel = LoginViewModel(authRepository, globalErrorManager)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithGoogle calls repository and updates loading state`() = runTest {
        val deferred = CompletableDeferred<Unit>()
        everySuspend { authRepository.signInWithGoogle() } calls {
            deferred.await()
        }

        viewModel.isLoading.test {
            awaitItem() shouldBe false

            viewModel.signInWithGoogle()
            awaitItem() shouldBe true

            deferred.complete(Unit)
            awaitItem() shouldBe false
        }

        verifySuspend { authRepository.signInWithGoogle() }
    }

    @Test
    fun `signInWithGoogle emits error to GlobalErrorManager on failure`() = runTest {
        everySuspend { authRepository.signInWithGoogle() } throws RuntimeException("Google Sign In Failed")

        globalErrorManager.currentError.test {
            awaitItem() shouldBe null

            viewModel.signInWithGoogle()
            testDispatcher.scheduler.advanceUntilIdle()

            val error = awaitItem()
            error shouldNotBe null
            error?.title shouldBe UiText.from(Res.string.auth_error_title)
            error?.message shouldBe UiText.from("Google Sign In Failed")
            viewModel.isLoading.value shouldBe false
        }
    }
}
