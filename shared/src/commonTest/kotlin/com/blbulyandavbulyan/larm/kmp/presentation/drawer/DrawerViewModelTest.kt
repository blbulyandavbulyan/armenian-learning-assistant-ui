package com.blbulyandavbulyan.larm.kmp.presentation.drawer

import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthRepository
import com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
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
class DrawerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val authRepository = mock<AuthRepository>()
    private val userProfileFlow = MutableStateFlow<UserProfile?>(null)
    private lateinit var viewModel: DrawerViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.observeUserProfile() } returns userProfileFlow
        everySuspend { authRepository.signOut() } returns Unit
        viewModel = DrawerViewModel(authRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
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
        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        verifySuspend { authRepository.signOut() }
    }
}
