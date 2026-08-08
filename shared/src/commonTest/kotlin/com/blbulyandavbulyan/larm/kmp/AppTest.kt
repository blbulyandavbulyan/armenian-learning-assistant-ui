package com.blbulyandavbulyan.larm.kmp

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.asset.repository.FakeAssetRepository
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthRepository
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthState
import com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Dialogue
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DialogueSummary
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DomainMothers
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.chat.FakeDialogueChatRepository
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.search.FakeDialogueRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.GetDialogueResponseMother
import com.blbulyandavbulyan.larm.kmp.infrastructure.audio.FakeAudioPlayer
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueChatViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.DialogueSearchViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.drawer.DrawerViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.global.AppViewModel
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createAuthenticatedAppViewModel(): AppViewModel {
        return AppViewModel(MutableStateFlow(AuthState.AUTHENTICATED))
    }

    private fun createAuthenticatedDrawerViewModel(): DrawerViewModel {
        val authRepository = mock<AuthRepository>()
        every { authRepository.observeUserProfile() } returns MutableStateFlow(null)
        everySuspend { authRepository.signOut() } returns Unit
        return DrawerViewModel(authRepository)
    }

    @Test
    fun navigationFlow_searchToDetailAndBack() = runComposeUiTest {
        val fakeDialogueRepository = TestFakeDialogueRepository()
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAssetRepository,
                GlobalErrorManager(),
                FakeAudioPlayer()
            )

        val appViewModel = createAuthenticatedAppViewModel()
        val drawerViewModel = createAuthenticatedDrawerViewModel()
        val chatViewModel = DialogueChatViewModel(FakeDialogueChatRepository(), GlobalErrorManager())

        // Set the state to Search before setting content to avoid animation/recomposition timing issues
        appViewModel.navigateToSearch()

        setContent {
            App(
                appViewModel = appViewModel,
                drawerViewModel = drawerViewModel,
                searchViewModel = viewModel,
                chatViewModel = chatViewModel
            )
        }

        // 1. Search something -> go to search screen
        onNodeWithTag("top_bar_search_field").performTextInput("Hello")
        onNodeWithTag("searchSubmitButton").performClick()
        waitForIdle()

        val dialogueId1 = GetDialogueResponseMother.Dialogue1.RESPONSE.id

        // Assert search completed
        onNodeWithTag("viewFullDialogueButton_$dialogueId1").assertIsDisplayed()

        // 2. Press 'view details' button on the search result screen -> go to the details
        fakeDialogueRepository.getDialogueCompletable = CompletableDeferred()
        onNodeWithTag("viewFullDialogueButton_$dialogueId1").performClick()

        onNodeWithTag("loadingIndicator").assertIsDisplayed()

        fakeDialogueRepository.getDialogueCompletable?.complete(Unit)

        // Wait for detail screen to appear
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithTag("detailTitleText", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }

        // Assert some specific content on the detail screen to verify navigation (e.g. 1 phrase from the dialogue)
        val expectedPhrase = GetDialogueResponseMother.Dialogue1.RESPONSE.dialoguePhrases[0].phrase.phrase
        onNodeWithText(expectedPhrase).assertIsDisplayed()
        onNodeWithTag("viewFullDialogueButton_$dialogueId1").assertDoesNotExist()

        // 3. Press top bar back button -> go back
        onNodeWithTag("top_bar_back_button").performClick()

        // Wait for search screen to reappear
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithTag("viewFullDialogueButton_$dialogueId1").fetchSemanticsNodes().isNotEmpty()
        }

        // Assert we are back on search screen
        onNodeWithTag("viewFullDialogueButton_$dialogueId1").assertIsDisplayed()
    }

    @Test
    fun navigationFlow_generatorToSearch() = runComposeUiTest {
        val fakeDialogueRepository = TestFakeDialogueRepository()
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAssetRepository,
                GlobalErrorManager(),
                FakeAudioPlayer()
            )

        val appViewModel = createAuthenticatedAppViewModel()
        val drawerViewModel = createAuthenticatedDrawerViewModel()
        val chatViewModel = DialogueChatViewModel(FakeDialogueChatRepository(), GlobalErrorManager())

        setContent {
            App(
                appViewModel = appViewModel,
                drawerViewModel = drawerViewModel,
                searchViewModel = viewModel,
                chatViewModel = chatViewModel
            )
        }

        // App initial state is Generator Screen
        onNodeWithTag("dialogueGeneratorScreen").assertIsDisplayed()

        // 1. Type something in top bar search field on generator screen
        onNodeWithTag("top_bar_search_field").performTextInput("Hello")

        // 2. Press search button
        fakeDialogueRepository.searchCompletable = CompletableDeferred()
        onNodeWithTag("searchSubmitButton").performClick()

        onNodeWithTag("loadingIndicator").assertIsDisplayed()

        fakeDialogueRepository.searchCompletable?.complete(Unit)
        waitForIdle()

        // Wait for search screen to appear
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithTag(
                "viewFullDialogueButton_${GetDialogueResponseMother.Dialogue1.RESPONSE.id}"
            ).fetchSemanticsNodes().isNotEmpty()
        }

        // Assert we are on search screen and search is completed
        onNodeWithTag("viewFullDialogueButton_${GetDialogueResponseMother.Dialogue1.RESPONSE.id}").assertIsDisplayed()
        onNodeWithTag("dialogueGeneratorScreen").assertDoesNotExist()
        onNodeWithTag("top_bar_search_field").assertTextEquals("Hello")
    }

    @Test
    fun navigationFlow_generatorToSearch_whenSearchFails_navigatesToSearchScreen() = runComposeUiTest {
        val fakeDialogueRepository = TestFakeDialogueRepository().apply {
            shouldFailSearch = true
        }
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel = DialogueSearchViewModel(
            fakeDialogueRepository,
            fakeAssetRepository,
            GlobalErrorManager(),
            FakeAudioPlayer()
        )

        val appViewModel = createAuthenticatedAppViewModel()
        val drawerViewModel = createAuthenticatedDrawerViewModel()
        val chatViewModel = DialogueChatViewModel(FakeDialogueChatRepository(), GlobalErrorManager())

        setContent {
            App(
                appViewModel = appViewModel,
                drawerViewModel = drawerViewModel,
                searchViewModel = viewModel,
                chatViewModel = chatViewModel
            )
        }

        // Initial state is Generator Screen
        onNodeWithTag("dialogueGeneratorScreen").assertIsDisplayed()

        // 1. Type query in search field
        onNodeWithTag("top_bar_search_field").performTextInput("Hello")

        // 2. Submit search
        fakeDialogueRepository.searchCompletable = CompletableDeferred()
        onNodeWithTag("searchSubmitButton").performClick()

        onNodeWithTag("loadingIndicator").assertIsDisplayed()

        // Complete deferred to let the search repository throw the exception
        fakeDialogueRepository.searchCompletable?.complete(Unit)
        waitForIdle()

        onNodeWithTag("dialogueSearchScreen").assertIsDisplayed()
        onNodeWithTag("dialogueGeneratorScreen").assertDoesNotExist()
    }

    @Test
    fun navigationFlow_searchToGeneratorBack() = runComposeUiTest {
        val fakeDialogueRepository = TestFakeDialogueRepository()
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel = DialogueSearchViewModel(
            fakeDialogueRepository,
            fakeAssetRepository,
            GlobalErrorManager(),
            FakeAudioPlayer()
        )
        val appViewModel = createAuthenticatedAppViewModel()
        val drawerViewModel = createAuthenticatedDrawerViewModel()
        val chatViewModel = DialogueChatViewModel(FakeDialogueChatRepository(), GlobalErrorManager())

        appViewModel.navigateToSearch()

        setContent {
            App(
                appViewModel = appViewModel,
                drawerViewModel = drawerViewModel,
                searchViewModel = viewModel,
                chatViewModel = chatViewModel
            )
        }

        onNodeWithTag("top_bar_back_button").assertIsDisplayed()
        onNodeWithTag("top_bar_back_button").performClick()
        waitForIdle()

        onNodeWithTag("dialogueGeneratorScreen").assertIsDisplayed()
        onNodeWithTag("top_bar_back_button").assertDoesNotExist()
    }

    @Test
    fun navigationFlow_drawerNavigateToGenerator() = runComposeUiTest {
        val fakeDialogueRepository = TestFakeDialogueRepository()
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel = DialogueSearchViewModel(
            fakeDialogueRepository,
            fakeAssetRepository,
            GlobalErrorManager(),
            FakeAudioPlayer()
        )
        val appViewModel = createAuthenticatedAppViewModel()
        val drawerViewModel = createAuthenticatedDrawerViewModel()
        val chatViewModel = DialogueChatViewModel(FakeDialogueChatRepository(), GlobalErrorManager())

        appViewModel.navigateToSearch()

        setContent {
            App(
                appViewModel = appViewModel,
                drawerViewModel = drawerViewModel,
                searchViewModel = viewModel,
                chatViewModel = chatViewModel
            )
        }

        onNodeWithTag("hamburger_button").performClick()
        waitForIdle()

        onNodeWithTag("drawer_nav_generator").performClick()
        waitForIdle()

        onNodeWithTag("dialogueGeneratorScreen").assertIsDisplayed()
    }

    @Test
    fun app_showsLoadingIndicator_whenStateIsLoading() = runComposeUiTest {
        val appViewModel = createAuthenticatedAppViewModel()
        val drawerViewModel = createAuthenticatedDrawerViewModel()
        val searchViewModel = DialogueSearchViewModel(
            FakeDialogueRepository(),
            FakeAssetRepository(),
            GlobalErrorManager(),
            FakeAudioPlayer()
        )
        val chatViewModel = DialogueChatViewModel(
            FakeDialogueChatRepository(),
            GlobalErrorManager()
        )

        setContent {
            App(
                appViewModel = appViewModel,
                drawerViewModel = drawerViewModel,
                searchViewModel = searchViewModel,
                chatViewModel = chatViewModel
            )
        }

        appViewModel.navigateToLoading()

        onNodeWithTag("loadingIndicator").assertIsDisplayed()
    }

    @Test
    fun app_showsLoginScreen_whenUnauthenticated() = runComposeUiTest {
        val authRepository = mock<AuthRepository>()
        every { authRepository.observeUserProfile() } returns MutableStateFlow(null)
        val appViewModel = AppViewModel(MutableStateFlow(AuthState.UNAUTHENTICATED))
        val drawerViewModel = DrawerViewModel(authRepository)
        val searchViewModel = DialogueSearchViewModel(
            FakeDialogueRepository(),
            FakeAssetRepository(),
            GlobalErrorManager(),
            FakeAudioPlayer()
        )
        val chatViewModel = DialogueChatViewModel(
            FakeDialogueChatRepository(),
            GlobalErrorManager()
        )

        setContent {
            App(
                appViewModel = appViewModel,
                drawerViewModel = drawerViewModel,
                searchViewModel = searchViewModel,
                chatViewModel = chatViewModel
            )
        }

        onNodeWithTag("loginScreen").assertIsDisplayed()
    }

    @Test
    fun app_showsTopBarAndDrawer_whenAuthenticated() = runComposeUiTest {
        val authRepository = mock<AuthRepository>()
        val authStateFlow = MutableStateFlow(AuthState.AUTHENTICATED)
        val userProfileFlow = MutableStateFlow<UserProfile?>(
            UserProfile(
                id = "user-123",
                email = "user@example.com",
                displayName = "David Bulyan",
                avatarUrl = null
            )
        )
        every { authRepository.observeAuthState() } returns authStateFlow
        every { authRepository.observeUserProfile() } returns userProfileFlow
        everySuspend { authRepository.signOut() } calls {
            authStateFlow.value = AuthState.UNAUTHENTICATED
            userProfileFlow.value = null
        }
        val appViewModel = AppViewModel(authStateFlow)
        val drawerViewModel = DrawerViewModel(authRepository)
        val searchViewModel = DialogueSearchViewModel(
            FakeDialogueRepository(),
            FakeAssetRepository(),
            GlobalErrorManager(),
            FakeAudioPlayer()
        )
        val chatViewModel = DialogueChatViewModel(
            FakeDialogueChatRepository(),
            GlobalErrorManager()
        )

        setContent {
            App(
                appViewModel = appViewModel,
                drawerViewModel = drawerViewModel,
                searchViewModel = searchViewModel,
                chatViewModel = chatViewModel
            )
        }

        // Top bar should be visible
        onNodeWithTag("app_top_bar").assertIsDisplayed()
        onNodeWithTag("hamburger_button").assertIsDisplayed()

        // Click hamburger button to open drawer
        onNodeWithTag("hamburger_button").performClick()
        waitForIdle()

        // Drawer header should show name and email, plus sign out item
        onNodeWithTag("drawer_profile_header").assertIsDisplayed()
        onNodeWithText("David Bulyan").assertIsDisplayed()
        onNodeWithText("user@example.com").assertIsDisplayed()
        onNodeWithTag("drawer_nav_generator").assertIsDisplayed()
        onNodeWithTag("drawer_sign_out_item").assertIsDisplayed()

        // Click Sign Out
        onNodeWithTag("drawer_sign_out_item").performClick()
        waitForIdle()

        // After sign out, login screen is displayed and top bar is gone
        onNodeWithTag("loginScreen").assertIsDisplayed()
        onNodeWithTag("app_top_bar").assertDoesNotExist()
    }

    @Test
    fun app_hidesTopBar_whenUnauthenticated() = runComposeUiTest {
        val authRepository = mock<AuthRepository>()
        every { authRepository.observeUserProfile() } returns MutableStateFlow(null)
        val appViewModel = AppViewModel(MutableStateFlow(AuthState.UNAUTHENTICATED))
        val drawerViewModel = DrawerViewModel(authRepository)
        val searchViewModel = DialogueSearchViewModel(
            FakeDialogueRepository(),
            FakeAssetRepository(),
            GlobalErrorManager(),
            FakeAudioPlayer()
        )
        val chatViewModel = DialogueChatViewModel(
            FakeDialogueChatRepository(),
            GlobalErrorManager()
        )

        setContent {
            App(
                appViewModel = appViewModel,
                drawerViewModel = drawerViewModel,
                searchViewModel = searchViewModel,
                chatViewModel = chatViewModel
            )
        }

        onNodeWithTag("loginScreen").assertIsDisplayed()
        onNodeWithTag("app_top_bar").assertDoesNotExist()
    }

    class TestFakeDialogueRepository : FakeDialogueRepository() {
        var searchCompletable: CompletableDeferred<Unit>? = null
        var getDialogueCompletable: CompletableDeferred<Unit>? = null
        var shouldFailSearch: Boolean = false

        override suspend fun searchDialogues(query: String): ImmutableList<DialogueSummary> {
            searchCompletable?.await()
            if (shouldFailSearch) {
                error("Search failed")
            }
            return persistentListOf(DomainMothers.DIALOGUE_SUMMARY_1, DomainMothers.DIALOGUE_SUMMARY_2)
        }

        override suspend fun getDialogue(id: String): Dialogue {
            getDialogueCompletable?.await()
            return DomainMothers.DIALOGUE_1
        }
    }
}
