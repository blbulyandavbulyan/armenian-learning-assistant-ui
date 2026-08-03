package com.blbulyandavbulyan.larm.kmp.presentation.global

import app.cash.turbine.test
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthRepository
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthState
import com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DomainMothers
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
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
    private val authRepository = mock<AuthRepository>()
    private val authStateFlow = MutableStateFlow(AuthState.UNAUTHENTICATED)
    private val userProfileFlow = MutableStateFlow<UserProfile?>(null)
    private lateinit var viewModel: AppViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.observeAuthState() } returns authStateFlow
        every { authRepository.observeUserProfile() } returns userProfileFlow
        everySuspend { authRepository.signOut() } calls {
            authStateFlow.value = AuthState.UNAUTHENTICATED
            userProfileFlow.value = null
        }
        viewModel = AppViewModel(authRepository)
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

        // Should remain on Search, not forced to Generator
        viewModel.currentScreen.value shouldBe ScreenState.Search
    }

    @Test
    fun `auth state LOADING sets screen to Loading`() = runTest {
        authStateFlow.value = AuthState.LOADING
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.currentScreen.value shouldBe ScreenState.Loading
    }

    @Test
    fun `userProfile updates when repository emits profile`() = runTest {
        viewModel.userProfile.value shouldBe null

        val testProfile = UserProfile(
            id = "test_user_1",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = "https://example.com/avatar.png"
        )
        userProfileFlow.value = testProfile
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.userProfile.value shouldBe testProfile
    }

    @Test
    fun `signOut invokes repository signOut`() = runTest {
        val testProfile = UserProfile(
            id = "test_user_1",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null
        )
        authStateFlow.value = AuthState.AUTHENTICATED
        userProfileFlow.value = testProfile
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.userProfile.value shouldBe testProfile
        viewModel.currentScreen.value shouldBe ScreenState.Generator

        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        verifySuspend { authRepository.signOut() }
        viewModel.userProfile.value shouldBe null
        viewModel.currentScreen.value shouldBe ScreenState.Login
    }
}
