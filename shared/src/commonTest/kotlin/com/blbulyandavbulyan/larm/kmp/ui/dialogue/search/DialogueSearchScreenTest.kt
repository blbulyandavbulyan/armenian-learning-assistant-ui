package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.search.Dialogue
import com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.search.DialogueSummary
import com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.search.DomainMothers
import com.blbulyandavbulyan.larm.kmp.network.FakeAssetRepository
import com.blbulyandavbulyan.larm.kmp.network.FakeDialogueRepository
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.DialogueSearchViewModel
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.assertDialogueTitle
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DialogueSearchScreenTest {
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

    @Test
    fun typingInSearchBar_updatesViewModelQuery_andPersistsWhenReturning() = runComposeUiTest {
        val fakeDialogueRepository = FakeDialogueRepository()
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAssetRepository,
                GlobalErrorManager()
            )
        var backPressed = false

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(
                    viewModel = viewModel,
                    onBack = { backPressed = true },
                    onGetDialogueDetails = {}
                )
            }
        }

        // Type into search bar
        onNodeWithTag("searchInputField").performTextInput("Armenian Query")

        // Check if query is synced with ViewModel
        viewModel.searchQuery.value shouldBe "Armenian Query"

        // Verify back button triggers callback
        onNodeWithTag("backButton").performClick()
        backPressed shouldBe true
        viewModel.searchQuery.value shouldBe "Armenian Query"
    }

    @Test
    fun searchScreen_listenButtonInvokesCorrectAudioEndpoint() = runComposeUiTest {
        val fakeDialogueRepository = createFakeDialogueRepository()
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAssetRepository,
                GlobalErrorManager()
            )

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(viewModel = viewModel, onBack = { }, onGetDialogueDetails = {})
            }
        }

        onNodeWithTag("searchInputField").performTextInput("Hello")
        onNodeWithTag("searchSubmitButton").performClick()

        val dialogueId = DomainMothers.DIALOGUE_1.id

        // Test Case 4: Search screen listen button
        onNodeWithTag("listenButton_$dialogueId").performClick()

        fakeAssetRepository.requestedUrls.last() shouldBe DomainMothers.DIALOGUE_1.title.assets.first().url
    }

    private fun createFakeDialogueRepository() = object : FakeDialogueRepository() {
        override suspend fun searchDialogues(query: String): ImmutableList<DialogueSummary> {
            return persistentListOf(DomainMothers.DIALOGUE_SUMMARY_1, DomainMothers.DIALOGUE_SUMMARY_2)
        }

        override suspend fun getDialogue(id: String): Dialogue {
            return DomainMothers.DIALOGUE_1
        }
    }

    @Test
    fun searchScreen_emptyResults_showsEmptyStateMessage() = runComposeUiTest {
        val fakeDialogueRepository = object : FakeDialogueRepository() {
            override suspend fun searchDialogues(query: String): ImmutableList<DialogueSummary> {
                return persistentListOf()
            }
        }
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAssetRepository,
                GlobalErrorManager()
            )

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(viewModel = viewModel, onBack = { }, onGetDialogueDetails = {})
            }
        }

        onNodeWithTag("emptyResultsMessage").assertDoesNotExist()

        onNodeWithTag("searchInputField").performTextInput("Hello")
        onNodeWithTag("searchSubmitButton").performClick()

        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithTag("emptyResultsMessage").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("emptyResultsMessage").assertExists()
    }

    @Test
    fun searchScreen_error_showsRetryButtonAndTriggersRetry() = runComposeUiTest {
        var callCount = 0
        val fakeDialogueRepository = object : FakeDialogueRepository() {
            @Suppress("TooGenericExceptionThrown")
            override suspend fun searchDialogues(query: String): ImmutableList<DialogueSummary> {
                callCount++
                throw RuntimeException("Network Error")
            }
        }
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAssetRepository,
                GlobalErrorManager()
            )

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(viewModel = viewModel, onBack = { }, onGetDialogueDetails = {})
            }
        }

        onNodeWithTag("searchInputField").performTextInput("Hello")
        onNodeWithTag("searchSubmitButton").performClick()

        // Wait for retry button to appear on error
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithTag("retryButton").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("emptyResultsMessage").assertDoesNotExist()

        onNodeWithTag("retryButton").assertIsDisplayed()
        callCount shouldBe 1

        onNodeWithTag("retryButton").performClick()
        waitForIdle()

        callCount shouldBe 2
    }

    @Test
    fun searchScreen_success_displaysAllInformationCorrectly() = runComposeUiTest {
        val fakeDialogueRepository = createFakeDialogueRepository()
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAssetRepository,
                GlobalErrorManager()
            )

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(viewModel = viewModel, onBack = { }, onGetDialogueDetails = {})
            }
        }

        onNodeWithTag("searchInputField").performTextInput("Hello")
        onNodeWithTag("searchSubmitButton").performClick()

        val response = listOf(DomainMothers.DIALOGUE_SUMMARY_1, DomainMothers.DIALOGUE_SUMMARY_2)

        response.forEach { dialogue ->
            assertDialogueTitle(
                title = dialogue.title,
                phraseTestTag = "searchResultPhrase_${dialogue.id}",
                transcriptionTestTag = "searchResultTranscription_${dialogue.id}"
            )
        }
    }

    @Test
    fun searchScreen_viewDetailsButton_navigatesToDetailScreen() = runComposeUiTest {
        val fakeDialogueRepository = createFakeDialogueRepository()
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAssetRepository,
                GlobalErrorManager()
            )

        var navigateToDialogueId: String? = null
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(
                    viewModel = viewModel,
                    onBack = { },
                    onGetDialogueDetails = { navigateToDialogueId = it }
                )
            }
        }

        onNodeWithTag("searchInputField").performTextInput("Hello")
        onNodeWithTag("searchSubmitButton").performClick()

        val secondDialogue = DomainMothers.DIALOGUE_SUMMARY_2

        onNodeWithTag("viewFullDialogueButton_${secondDialogue.id}").performScrollTo().performClick()

        navigateToDialogueId shouldBe secondDialogue.id
    }

    @Test
    fun searchScreen_listenButton_invokesCorrectAudioEndpoint() = runComposeUiTest {
        val fakeDialogueRepository = createFakeDialogueRepository()
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAssetRepository,
                GlobalErrorManager()
            )

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(viewModel = viewModel, onBack = { }, onGetDialogueDetails = {})
            }
        }

        onNodeWithTag("searchInputField").performTextInput("Hello")
        onNodeWithTag("searchSubmitButton").performClick()

        val secondDialogue = DomainMothers.DIALOGUE_SUMMARY_2

        onNodeWithTag("listenButton_${secondDialogue.id}").performScrollTo().performClick()

        fakeAssetRepository.requestedUrls.last() shouldBe secondDialogue.title.assets.first().url
    }

    @Test
    fun searchScreen_whenLoading_showsLoadingIndicator() = runComposeUiTest {
        val fakeDialogueRepository = object : FakeDialogueRepository() {
            override suspend fun searchDialogues(query: String): ImmutableList<DialogueSummary> {
                kotlinx.coroutines.awaitCancellation()
            }
        }
        val fakeAssetRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAssetRepository,
                GlobalErrorManager()
            )

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(viewModel = viewModel, onBack = { }, onGetDialogueDetails = {})
            }
        }

        onNodeWithTag("searchInputField").performTextInput("Hello")
        onNodeWithTag("searchSubmitButton").performClick()

        onNodeWithTag("loadingIndicator").assertIsDisplayed()
    }
}
