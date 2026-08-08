package com.blbulyandavbulyan.larm.kmp.presentation.global

import app.cash.turbine.test
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthState
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DomainMothers
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val authStateFlow = MutableStateFlow(AuthState.UNAUTHENTICATED)
    private lateinit var viewModel: AppViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AppViewModel(authStateFlow)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `navigation state defaults and updates correctly via manual methods`() = runTest {
        viewModel.currentScreen.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem().shouldBeInstanceOf<ScreenState.Login>()

            viewModel.navigateToSearch()
            awaitItem().shouldBeInstanceOf<ScreenState.Search>()

            viewModel.navigateToLoading()
            awaitItem().shouldBeInstanceOf<ScreenState.Loading>()

            val fakeDialogue = DomainMothers.DIALOGUE_1
            viewModel.navigateToDetail(fakeDialogue)
            awaitItem().shouldBeInstanceOf<ScreenState.Detail>().dialogue shouldBe fakeDialogue

            viewModel.navigateToGenerator()
            awaitItem().shouldBeInstanceOf<ScreenState.Generator>()

            viewModel.navigateToLogin()
            awaitItem().shouldBeInstanceOf<ScreenState.Login>()
        }
    }

    @Test
    fun `auth state UNAUTHENTICATED sets screen to Login`() = runTest {
        authStateFlow.value = AuthState.UNAUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.currentScreen.value shouldBe ScreenState.Login
    }

    @Test
    fun `auth state AUTHENTICATED switches from Login to Generator`() = runTest {
        authStateFlow.value = AuthState.UNAUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.currentScreen.value shouldBe ScreenState.Login

        authStateFlow.value = AuthState.AUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.currentScreen.value shouldBe ScreenState.Generator
    }

    @Test
    fun `auth state AUTHENTICATED does not override Search screen`() = runTest {
        authStateFlow.value = AuthState.UNAUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.navigateToSearch()
        viewModel.currentScreen.value shouldBe ScreenState.Search

        authStateFlow.value = AuthState.AUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.currentScreen.value shouldBe ScreenState.Search
    }

    @Test
    fun `auth state LOADING sets screen to Loading`() = runTest {
        authStateFlow.value = AuthState.LOADING
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.currentScreen.value shouldBe ScreenState.Loading
    }
}
