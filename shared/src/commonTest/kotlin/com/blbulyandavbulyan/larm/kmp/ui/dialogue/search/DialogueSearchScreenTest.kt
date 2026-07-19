package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.App
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponseMother
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.SearchDialoguesResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.SearchDialoguesResponseMother
import com.blbulyandavbulyan.larm.kmp.network.FakeAssetRepository
import com.blbulyandavbulyan.larm.kmp.network.FakeDialogueRepository
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.DialogueSearchViewModel
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.detail.DialogueDetailScreen
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
                    onNavigateToDetail = {}
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
    fun searchResults_areDisplayedCorrectly_whenQueryIsSubmitted() = runComposeUiTest {
        val fakeDialogueRepository = createFakeDialogueRepository()
        val fakeAudioRepository = FakeAssetRepository()
        val viewModel =
            DialogueSearchViewModel(
                fakeDialogueRepository,
                fakeAudioRepository,
                GlobalErrorManager()
            )

        val appViewModel = com.blbulyandavbulyan.larm.kmp.presentation.global.AppViewModel()
        val chatViewModel = com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueChatViewModel(
            fakeDialogueRepository, fakeAudioRepository, com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager()
        )

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

        assertDetailScreenContentVisible()

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

    @OptIn(ExperimentalTestApi::class)
    private fun androidx.compose.ui.test.ComposeUiTest.assertDetailScreenContentVisible() {
        val expectedPhrase1 = GetDialogueResponseMother.FULL_DIALOGUE_1.title.phrase
        val expectedTranscription1 = GetDialogueResponseMother.FULL_DIALOGUE_1.title.transcription

        // Assert the correct dialogue is shown in the detail screen
        onNodeWithTag("detailTitleText", useUnmergedTree = true).assertIsDisplayed().assertTextEquals(expectedPhrase1)
        onNodeWithTag("detailTranscriptionText", useUnmergedTree = true).assertIsDisplayed()
            .assertTextEquals(expectedTranscription1)

        val speaker1 = GetDialogueResponseMother.FULL_DIALOGUE_1.speakers[0]
        val phrase1 = GetDialogueResponseMother.FULL_DIALOGUE_1.dialoguePhrases[0].phrase

        val speaker2 = GetDialogueResponseMother.FULL_DIALOGUE_1.speakers[1]
        val phrase2 = GetDialogueResponseMother.FULL_DIALOGUE_1.dialoguePhrases[1].phrase

        // Assert the first speaker and phrase are shown using tags
        onNodeWithTag("speakerName_${speaker1.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(speaker1.name.phrase)
        onNodeWithTag(
            "speakerTranscription_${speaker1.id}",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
            .assertTextEquals("(${speaker1.name.transcription})")
        onNodeWithTag(
            "speakerTranslation_${speaker1.id}_0",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
            .assertTextEquals(speaker1.name.translations[0].translationText)

        onNodeWithTag("phraseText_${phrase1.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase1.phrase)
        onNodeWithTag("phraseTranscription_${phrase1.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase1.transcription)
        // Assert phrase 1 translation is shown
        onNodeWithTag("phraseTranslation_${phrase1.id}_0", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase1.translations[0].translationText)

        // Assert the second speaker and phrase are shown using tags
        onNodeWithTag("speakerName_${speaker2.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(speaker2.name.phrase)
        onNodeWithTag(
            "speakerTranscription_${speaker2.id}",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
            .assertTextEquals("(${speaker2.name.transcription})")
        onNodeWithTag(
            "speakerTranslation_${speaker2.id}_0",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
            .assertTextEquals(speaker2.name.translations[0].translationText)

        onNodeWithTag("phraseText_${phrase2.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase2.phrase)
        onNodeWithTag("phraseTranscription_${phrase2.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase2.transcription)
        // Assert phrase 2 translation is shown
        onNodeWithTag("phraseTranslation_${phrase2.id}_0", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase2.translations[0].translationText)
    }

    @Test
    fun detailScreen_listenButtonsInvokeCorrectAudioEndpoint() = runComposeUiTest {
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
                DialogueDetailScreen(
                    dialogue = GetDialogueResponseMother.FULL_DIALOGUE_1,
                    onBack = { },
                    onPlayAudio = viewModel::playAudio
                )
            }
        }

        val dialogueId = GetDialogueResponseMother.FULL_DIALOGUE_1.id

        // Test Case 2: Title Listen Button
        onNodeWithTag("listenTitleButton_$dialogueId").performClick()

        fakeAudioRepository.requestedUrls.last() shouldBe GetDialogueResponseMother.FULL_DIALOGUE_1.title.assets.first().url

        // Test Case 2: Speaker Listen Button
        val speakerId = GetDialogueResponseMother.FULL_DIALOGUE_1.speakers.first().id
        onNodeWithTag("listenSpeakerButton_$speakerId").performScrollTo().performClick()

        fakeAudioRepository.requestedUrls.last() shouldBe GetDialogueResponseMother.FULL_DIALOGUE_1.speakers.first().name.assets.first().url

        // Test Case 2: Phrase Listen Button
        val phraseId = GetDialogueResponseMother.FULL_DIALOGUE_1.dialoguePhrases.first().phrase.id
        onNodeWithTag("listenPhraseButton_$phraseId").performScrollTo().performClick()

        fakeAudioRepository.requestedUrls.last() shouldBe GetDialogueResponseMother.FULL_DIALOGUE_1.dialoguePhrases.first().phrase.assets.first().url
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
                DialogueSearchScreen(viewModel = viewModel, onBack = { }, onNavigateToDetail = {})
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
