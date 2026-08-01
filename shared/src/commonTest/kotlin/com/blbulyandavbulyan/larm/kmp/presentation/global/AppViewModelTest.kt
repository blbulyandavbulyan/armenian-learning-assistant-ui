package com.blbulyandavbulyan.larm.kmp.presentation.global

import app.cash.turbine.test
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthState
import com.blbulyandavbulyan.larm.kmp.domain.auth.FakeAuthRepository
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DomainMothers
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
class AppViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: AppViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        viewModel = AppViewModel(fakeAuthRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `navigation state defaults and updates correctly via manual methods`() = runTest {
        viewModel.currentScreen.test {
            // After authState flow emits UNAUTHENTICATED on init, screen should become Login
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
        fakeAuthRepository.authStateFlow.value = AuthState.UNAUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.currentScreen.value shouldBe ScreenState.Login
    }

    @Test
    fun `auth state AUTHENTICATED switches from Login to Generator`() = runTest {
        fakeAuthRepository.authStateFlow.value = AuthState.UNAUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.currentScreen.value shouldBe ScreenState.Login

        fakeAuthRepository.authStateFlow.value = AuthState.AUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.currentScreen.value shouldBe ScreenState.Generator
    }

    @Test
    fun `auth state AUTHENTICATED does not override Search screen`() = runTest {
        fakeAuthRepository.authStateFlow.value = AuthState.UNAUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.navigateToSearch()
        viewModel.currentScreen.value shouldBe ScreenState.Search

        fakeAuthRepository.authStateFlow.value = AuthState.AUTHENTICATED
        testDispatcher.scheduler.advanceUntilIdle()

        // Should remain on Search, not forced to Generator
        viewModel.currentScreen.value shouldBe ScreenState.Search
    }

    @Test
    fun `auth state LOADING sets screen to Loading`() = runTest {
        fakeAuthRepository.authStateFlow.value = AuthState.LOADING
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.currentScreen.value shouldBe ScreenState.Loading
    }

    @Test
    fun `userProfile updates when repository emits profile`() = runTest {
        viewModel.userProfile.value shouldBe null

        val testProfile = com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile(
            id = "test_user_1",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = "https://example.com/avatar.png"
        )
        fakeAuthRepository.userProfileFlow.value = testProfile
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.userProfile.value shouldBe testProfile
    }

    @Test
    fun `signOut invokes repository signOut`() = runTest {
        val testProfile = com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile(
            id = "test_user_1",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null
        )
        fakeAuthRepository.authStateFlow.value = AuthState.AUTHENTICATED
        fakeAuthRepository.userProfileFlow.value = testProfile
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.userProfile.value shouldBe testProfile
        viewModel.currentScreen.value shouldBe ScreenState.Generator

        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        fakeAuthRepository.signOutCalled shouldBe true
        viewModel.userProfile.value shouldBe null
        viewModel.currentScreen.value shouldBe ScreenState.Login
    }
}
