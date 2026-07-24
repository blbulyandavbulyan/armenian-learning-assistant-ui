package com.blbulyandavbulyan.larm.kmp

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
    fun navigationFlow_searchToDetailAndBack() = runComposeUiTest {
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

        // 1. Search something -> go to search screen
        onNodeWithTag("searchInputField").performTextInput("Hello")
        onNodeWithTag("searchSubmitButton").performClick()
        waitForIdle()

        val dialogueId1 = GetDialogueResponseMother.FULL_DIALOGUE_1.id

        // Assert search completed
        onNodeWithTag("viewFullDialogueButton_$dialogueId1").assertIsDisplayed()

        // 2. Press 'view details' button on the search result screen -> go to the details
        onNodeWithTag("viewFullDialogueButton_$dialogueId1").performClick()

        // Wait for detail screen to appear
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithTag("detailTitleText", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }

        // Assert some specific content on the detail screen to verify navigation (e.g. 1 phrase from the dialogue)
        val expectedPhrase = GetDialogueResponseMother.FULL_DIALOGUE_1.dialoguePhrases[0].phrase.phrase
        onNodeWithText(expectedPhrase).assertIsDisplayed()
        onNodeWithTag("viewFullDialogueButton_$dialogueId1").assertDoesNotExist()

        // 3. Press back -> go back
        onNodeWithTag("backButton").performClick()

        // Wait for search screen to reappear
        waitUntil(timeoutMillis = 5000) { onAllNodesWithTag("searchInputField").fetchSemanticsNodes().isNotEmpty() }

        // Assert we are back on search screen
        onNodeWithTag("viewFullDialogueButton_$dialogueId1").assertIsDisplayed()
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
