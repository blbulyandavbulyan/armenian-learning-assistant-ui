package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.asset.repository.FakeAssetRepository
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DialogueSummary
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DomainMothers
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.search.FakeDialogueRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.audio.FakeAudioPlayer
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.DialogueSearchViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.SearchState
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
    fun searchScreen_whenLoading_showsLoadingIndicator() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(
                    searchState = SearchState.Loading,
                    onSearch = {},
                    onGetDialogueDetails = {},
                    onPlayAudio = {}
                )
            }
        }

        onNodeWithTag("loadingIndicator").assertIsDisplayed()
    }

    @Test
    fun searchScreen_emptyResults_showsEmptyStateMessage() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(
                    searchState = SearchState.Success(persistentListOf()),
                    onSearch = {},
                    onGetDialogueDetails = {},
                    onPlayAudio = {}
                )
            }
        }

        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithTag("emptyResultsMessage").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("emptyResultsMessage").assertExists()
    }

    @Test
    fun searchScreen_error_showsRetryButtonAndTriggersRetry() = runComposeUiTest {
        var searchTriggered = false
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(
                    searchState = SearchState.Error,
                    onSearch = { searchTriggered = true },
                    onGetDialogueDetails = {},
                    onPlayAudio = {}
                )
            }
        }

        onNodeWithTag("retryButton").assertIsDisplayed()
        onNodeWithTag("retryButton").performClick()

        searchTriggered shouldBe true
    }

    @Test
    fun searchScreen_success_displaysAllInformationCorrectly() = runComposeUiTest {
        val results = persistentListOf(DomainMothers.DIALOGUE_SUMMARY_1, DomainMothers.DIALOGUE_SUMMARY_2)

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(
                    searchState = SearchState.Success(results),
                    onSearch = {},
                    onGetDialogueDetails = {},
                    onPlayAudio = {}
                )
            }
        }

        results.forEach { dialogue ->
            assertDialogueTitle(
                title = dialogue.title,
                phraseTestTag = "searchResultPhrase_${dialogue.id}",
                transcriptionTestTag = "searchResultTranscription_${dialogue.id}"
            )
        }
    }

    @Test
    fun searchScreen_viewDetailsButton_navigatesToDetailScreen() = runComposeUiTest {
        val results = persistentListOf(DomainMothers.DIALOGUE_SUMMARY_1, DomainMothers.DIALOGUE_SUMMARY_2)
        var navigateToDialogueId: String? = null

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(
                    searchState = SearchState.Success(results),
                    onSearch = {},
                    onGetDialogueDetails = { navigateToDialogueId = it },
                    onPlayAudio = {}
                )
            }
        }

        val secondDialogue = DomainMothers.DIALOGUE_SUMMARY_2
        onNodeWithTag("viewFullDialogueButton_${secondDialogue.id}").performScrollTo().performClick()

        navigateToDialogueId shouldBe secondDialogue.id
    }

    @Test
    fun searchScreen_listenButton_invokesCorrectAudioEndpoint() = runComposeUiTest {
        val results = persistentListOf(DomainMothers.DIALOGUE_SUMMARY_1, DomainMothers.DIALOGUE_SUMMARY_2)
        var playedAudio: String? = null

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(
                    searchState = SearchState.Success(results),
                    onSearch = {},
                    onGetDialogueDetails = {},
                    onPlayAudio = { playedAudio = it }
                )
            }
        }

        val secondDialogue = DomainMothers.DIALOGUE_SUMMARY_2
        onNodeWithTag("listenButton_${secondDialogue.id}").performScrollTo().performClick()

        playedAudio shouldBe secondDialogue.title.assets.first().url
    }

    @Test
    fun searchScreen_withViewModel_delegatesCorrectly() = runComposeUiTest {
        val fakeRepo = object : FakeDialogueRepository() {
            override suspend fun searchDialogues(query: String): ImmutableList<DialogueSummary> {
                return persistentListOf(DomainMothers.DIALOGUE_SUMMARY_1)
            }
        }
        val viewModel = DialogueSearchViewModel(
            fakeRepo,
            FakeAssetRepository(),
            GlobalErrorManager(),
            FakeAudioPlayer()
        )
        viewModel.updateSearchQuery("test query")
        viewModel.searchDialogues("test query", {}, {})

        var navigateToId: String? = null
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(
                    viewModel = viewModel,
                    onGetDialogueDetails = { navigateToId = it }
                )
            }
        }

        val dialogue = DomainMothers.DIALOGUE_SUMMARY_1
        onNodeWithTag("viewFullDialogueButton_${dialogue.id}").performScrollTo().performClick()
        navigateToId shouldBe dialogue.id
    }
}
