package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponseMother
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.SearchDialoguesResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.SearchDialoguesResponseMother
import com.blbulyandavbulyan.larm.kmp.network.FakeAssetRepository
import com.blbulyandavbulyan.larm.kmp.network.FakeDialogueRepository
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.DialogueSearchViewModel
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
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
        val fakeAudioRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAudioRepository,
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
        val fakeAudioRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAudioRepository,
                GlobalErrorManager()
            )

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueSearchScreen(viewModel = viewModel, onBack = { }, onGetDialogueDetails = {})
            }
        }

        onNodeWithTag("searchInputField").performTextInput("Hello")
        onNodeWithTag("searchSubmitButton").performClick()

        val dialogueId = GetDialogueResponseMother.FULL_DIALOGUE_1.id

        // Test Case 4: Search screen listen button
        onNodeWithTag("listenButton_$dialogueId").performClick()

        fakeAudioRepository.requestedUrls.last() shouldBe GetDialogueResponseMother.FULL_DIALOGUE_1.title.assets.first().url
    }

    private fun createFakeDialogueRepository() = object : FakeDialogueRepository() {
        override suspend fun searchDialogues(query: String): SearchDialoguesResponse {
            return SearchDialoguesResponseMother.SEARCH_RESPONSE_1
        }

        override suspend fun getDialogue(id: String): GetDialogueResponse {
            return GetDialogueResponseMother.FULL_DIALOGUE_1
        }
    }

    // TODO YOU DROPPED THIS ENTIRE SECTION OF TODO COMMENTS, BUT I DONT BELIEVE YOU THAT YOU ACTUALLY FIXED THEM
    //  WE SHOULD CHECK WHAT OF THEM ARE REALLY FIXED, AND WHAT OF THEM YOU JUST CASUALLY DROPPED THINKING THAT THERE WILL BE NO CONSEQUENCES FOR YOU
    // TODO, implement tests
    //  1. When search result returned more then one dialogue, then after pressing 'View details' the correct dialogue is shown in the detail screen,
    //  and all the required information is present on the screen
    //  2. When 'Details' are shown on the screen, then pressing listening buttons for each thing which could be 'listened',
    //  invokes the correct endpoint with the RIGHT audi url on the backend
    //  3. Audio cache testing, when 'Details' are shown on the screen,
    //  and user presses listen button for some phrase several times -> then the caching works, and the backend endpoint is invoked only ONCE
    //  4. When 'Dialogues search screen' is shown (not details one), and user presses 'listen' button near the dialogue,
    //  the correct backend endpoint for the audio is invoked (with the right URL for this specific dialogue)
}
