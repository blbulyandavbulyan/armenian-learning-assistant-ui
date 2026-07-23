package com.blbulyandavbulyan.larm.kmp

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponseMother
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.SearchDialoguesResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.SearchDialoguesResponseMother
import com.blbulyandavbulyan.larm.kmp.network.FakeAssetRepository
import com.blbulyandavbulyan.larm.kmp.network.FakeDialogueChatRepository
import com.blbulyandavbulyan.larm.kmp.network.FakeDialogueRepository
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueChatViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.DialogueSearchViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.global.AppViewModel
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Test
    fun searchResults_areDisplayedCorrectly_whenQueryIsSubmitted() = runComposeUiTest {
        // TODO rewrite this test to assert that only 'screens' are shown (by tags), like to test 'navigation'

        val fakeDialogueRepository = createFakeDialogueRepository()
        val fakeAudioRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAudioRepository,
                GlobalErrorManager()
            )

        val appViewModel = AppViewModel()
        val chatViewModel = DialogueChatViewModel(FakeDialogueChatRepository(), GlobalErrorManager())

        // Set the state to Search before setting content to avoid animation/recomposition timing issues
        appViewModel.navigateToSearch()

        setContent {
            App(appViewModel = appViewModel, searchViewModel = viewModel, chatViewModel = chatViewModel)
        }

        performSearchAndAssertResultsVisible()

        val dialogueId1 = GetDialogueResponseMother.FULL_DIALOGUE_1.id
        // Click View Full
        onNodeWithTag("viewFullDialogueButton_$dialogueId1").performClick()

        // Wait for animation to finish and detail screen to appear
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithTag("detailTitleText", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }

        // Ensure ViewModel navigation to detail is triggered
        appViewModel.currentScreen.value::class.simpleName shouldBe "Detail"

//        assertDetailScreenContentVisible()

        onNodeWithTag("backButton").performClick()

        waitUntil(timeoutMillis = 5000) { onAllNodesWithTag("searchInputField").fetchSemanticsNodes().isNotEmpty() }

        viewModel.searchQuery.value shouldBe "Hello"
    }

    @OptIn(ExperimentalTestApi::class)
    private fun androidx.compose.ui.test.ComposeUiTest.performSearchAndAssertResultsVisible() {
        onNodeWithTag("searchInputField").performTextInput("Hello")
        onNodeWithTag("searchSubmitButton").performClick()
        waitForIdle()

        val dialogueId1 = GetDialogueResponseMother.FULL_DIALOGUE_1.id
        val expectedPhrase1 = GetDialogueResponseMother.FULL_DIALOGUE_1.title.phrase
        val expectedTranscription1 = GetDialogueResponseMother.FULL_DIALOGUE_1.title.transcription

        val secondDialogue = SearchDialoguesResponseMother.SEARCH_RESPONSE_1.dialogues[1]
        val dialogueId2 = secondDialogue.id

        // Assert the first dialogue is visible
        onNodeWithTag("searchResultCard_$dialogueId1").performScrollTo().assertIsDisplayed()
        onNodeWithTag("searchResultPhrase_$dialogueId1", useUnmergedTree = true).assertIsDisplayed()
            .assertTextEquals(expectedPhrase1)
        onNodeWithTag("searchResultTranscription_$dialogueId1", useUnmergedTree = true).assertIsDisplayed()
            .assertTextEquals(expectedTranscription1)

        // Assert the second dialogue is visible
        onNodeWithTag("searchResultCard_$dialogueId2").performScrollTo().assertIsDisplayed()
        onNodeWithTag("searchResultPhrase_$dialogueId2", useUnmergedTree = true).assertIsDisplayed()
            .assertTextEquals(secondDialogue.title.phrase)
        onNodeWithTag("searchResultTranscription_$dialogueId2", useUnmergedTree = true).assertIsDisplayed()
            .assertTextEquals(secondDialogue.title.transcription)
    }

    private fun createFakeDialogueRepository() = object : FakeDialogueRepository() {
        override suspend fun searchDialogues(query: String): SearchDialoguesResponse {
            return SearchDialoguesResponseMother.SEARCH_RESPONSE_1
        }

        override suspend fun getDialogue(id: String): GetDialogueResponse {
            return GetDialogueResponseMother.FULL_DIALOGUE_1
        }
    }
}
