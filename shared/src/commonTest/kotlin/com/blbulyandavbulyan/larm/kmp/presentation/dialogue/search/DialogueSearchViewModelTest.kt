package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search

import app.cash.turbine.test
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.audio_playback_error_title
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_display_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_search_dialogues
import com.blbulyandavbulyan.larm.kmp.core.UiText
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.network.FakeAssetRepository
import com.blbulyandavbulyan.larm.kmp.network.FakeDialogueRepository
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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
class DialogueSearchViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeDialogueRepository
    private lateinit var fakeAudioRepository: FakeAssetRepository
    private lateinit var globalErrorManager: GlobalErrorManager
    private lateinit var viewModel: DialogueSearchViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeDialogueRepository()
        fakeAudioRepository = FakeAssetRepository()
        globalErrorManager = GlobalErrorManager()
        viewModel = DialogueSearchViewModel(fakeRepository, fakeAudioRepository, globalErrorManager)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchDialogues transitions to Loading and then Success`() = runTest {
        viewModel.searchState.test {
            var onErrorWasCalled = false
            var onSuccessWasCalled = false
            awaitItem() shouldBe SearchState.Initial
            viewModel.searchDialogues(
                "query",
                onError = { onErrorWasCalled = true },
                onSuccess = { onSuccessWasCalled = true }
            )
            awaitItem() shouldBe SearchState.Loading
            val successState = awaitItem() as SearchState.Success
            successState.results shouldBe emptyList()
            testScheduler.advanceUntilIdle()
            expectNoEvents()
            onErrorWasCalled shouldBe false
            onSuccessWasCalled shouldBe true
        }
    }

    @Test
    fun `searchDialogues transitions to search error state on failure, and reports error to globalErrorManager`() = runTest {
        fakeRepository.shouldFail = true
        viewModel.searchState.test {
            var onErrorWasCalled = false
            var onSuccessWasCalled = false
            awaitItem() shouldBe SearchState.Initial
            viewModel.searchDialogues(
                "query",
                onError = { onErrorWasCalled = true },
                onSuccess = { onSuccessWasCalled = true }
            )
            awaitItem() shouldBe SearchState.Loading
            awaitItem() shouldBe SearchState.Error
            testScheduler.advanceUntilIdle()
            val error = globalErrorManager.currentError.value
            error.shouldNotBeNull()
            error.message shouldBe UiText.from("Fake Network Error")
            error.title shouldBe UiText.from(Res.string.error_failed_to_search_dialogues)
            expectNoEvents()
            onErrorWasCalled shouldBe true
            onSuccessWasCalled shouldBe false
        }
    }

    @Test
    fun `playAudio transitions to Error on failure`() = runTest {
        fakeAudioRepository.shouldFail = true
        viewModel.playAudio("http://example.com")
        testScheduler.advanceUntilIdle()
        val error = globalErrorManager.currentError.value
        error.shouldNotBeNull()
        error.message shouldBe UiText.from("Fake Network Error")
        error.title shouldBe UiText.from(Res.string.audio_playback_error_title)
    }

    @Test
    fun playAudio_whenAudioPlayExceptionThrown_updatesAudioErrorStateAndDoesNotChangeSearchState() = runTest {
        fakeAudioRepository.shouldFailWithAudioException = true
        viewModel.playAudio("url")
        testScheduler.advanceUntilIdle()
        val error = globalErrorManager.currentError.value
        error.shouldNotBeNull()
        error.message shouldBe UiText.from("Fake Audio Error")
        error.title shouldBe UiText.from(Res.string.audio_playback_error_title)
        viewModel.searchState.value shouldBe SearchState.Initial
    }

    @Test
    fun `displayDialogue calls callback on success`() = runTest {
        var onDialogueReadyCalled = false
        var onErrorCalled = false
        viewModel.displayDialogue(
            "123",
            onError = { onErrorCalled = true },
            onDialogueReady = { onDialogueReadyCalled = true }
        )
        testScheduler.advanceUntilIdle()
        onDialogueReadyCalled shouldBe true
        onErrorCalled shouldBe false
    }

    @Test
    fun `displayDialogue transitions to global Error on failure`() = runTest {
        fakeRepository.shouldFail = true
        var onErrorCalled = false
        var onDialogueReadyCalled = false
        viewModel.displayDialogue("123", { onDialogueReadyCalled = true }, { onErrorCalled = true })
        testScheduler.advanceUntilIdle()
        val error = globalErrorManager.currentError.value
        error.shouldNotBeNull()
        error.message shouldBe UiText.from("Fake Network Error")
        error.title shouldBe UiText.from(Res.string.error_failed_to_display_dialogue)
        onErrorCalled shouldBe true
        onDialogueReadyCalled shouldBe false
    }

    @Test
    fun `searchQuery updates when updateSearchQuery is called`() = runTest {
        viewModel.searchQuery.test {
            awaitItem() shouldBe ""
            viewModel.updateSearchQuery("test query")
            awaitItem() shouldBe "test query"
        }
    }

    @Test
    fun `searchQuery updates when searchDialogues is called`() = runTest {
        viewModel.searchQuery.test {
            awaitItem() shouldBe ""
            viewModel.searchDialogues(
                "another query",
                onSuccess = {},
                onError = {}
            )
            awaitItem() shouldBe "another query"
        }
    }
}
