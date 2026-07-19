package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search

import app.cash.turbine.test
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
            awaitItem() shouldBe SearchState.Initial
            viewModel.searchDialogues("query")
            awaitItem() shouldBe SearchState.Loading
            val successState = awaitItem() as SearchState.Success
            successState.results shouldBe emptyList()
            testScheduler.advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `searchDialogues transitions to global Error on failure`() = runTest {
        fakeRepository.shouldFail = true
        viewModel.searchState.test {
            awaitItem() shouldBe SearchState.Initial
            viewModel.searchDialogues("query")
            awaitItem() shouldBe SearchState.Loading
            awaitItem() shouldBe SearchState.Initial
            testScheduler.advanceUntilIdle()
            val error = globalErrorManager.currentError.value
            error.shouldNotBeNull()
            error.message shouldBe "Fake Network Error"
            expectNoEvents()
        }
    }

    @Test
    fun `playAudio transitions to Error on failure`() = runTest {
        fakeAudioRepository.shouldFail = true
        viewModel.playAudio("http://example.com")
        testScheduler.advanceUntilIdle()
        val error = globalErrorManager.currentError.value
        error.shouldNotBeNull()
        error.message shouldBe "Fake Network Error"
    }

    @Test
    fun playAudio_whenAudioPlayExceptionThrown_updatesAudioErrorStateAndDoesNotChangeSearchState() = runTest {
        fakeAudioRepository.shouldFailWithAudioException = true
        viewModel.playAudio("url")
        testScheduler.advanceUntilIdle()
        val error = globalErrorManager.currentError.value
        error.shouldNotBeNull()
        error.message shouldBe "Fake Audio Error"
        viewModel.searchState.value shouldBe SearchState.Initial
    }

    @Test
    fun `displayDialogue calls callback on success`() = runTest {
        var called = false
        viewModel.displayDialogue("123") { called = true }
        testScheduler.advanceUntilIdle()
        called shouldBe true
    }

    @Test
    fun `displayDialogue transitions to global Error on failure`() = runTest {
        fakeRepository.shouldFail = true
        viewModel.displayDialogue("123") { }
        testScheduler.advanceUntilIdle()
        val error = globalErrorManager.currentError.value
        error.shouldNotBeNull()
        error.message shouldBe "Fake Network Error"
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
            viewModel.searchDialogues("another query")
            awaitItem() shouldBe "another query"
        }
    }
}