package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search

import app.cash.turbine.test
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.audio_playback_error_title
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_display_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_search_dialogues
import com.blbulyandavbulyan.larm.kmp.core.UiText
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.asset.model.AssetData
import com.blbulyandavbulyan.larm.kmp.domain.asset.repository.AssetFetchException
import com.blbulyandavbulyan.larm.kmp.domain.asset.repository.AssetRepository
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DomainMothers
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.search.DialogueRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.audio.Audio
import com.blbulyandavbulyan.larm.kmp.infrastructure.audio.AudioPlayException
import com.blbulyandavbulyan.larm.kmp.infrastructure.audio.AudioPlayer
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
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
    private val mockRepository = mock<DialogueRepository>()
    private val mockAssetRepository = mock<AssetRepository>()
    private val mockAudioPlayer = mock<AudioPlayer>()
    private lateinit var globalErrorManager: GlobalErrorManager
    private lateinit var viewModel: DialogueSearchViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        globalErrorManager = GlobalErrorManager()
        viewModel = DialogueSearchViewModel(mockRepository, mockAssetRepository, globalErrorManager, mockAudioPlayer)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchDialogues transitions to Loading and then Success`() = runTest {
        everySuspend { mockRepository.searchDialogues("query") } returns persistentListOf()

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

        verifySuspend { mockRepository.searchDialogues("query") }
    }

    @Test
    fun `searchDialogues transitions to search error state on failure, and reports error to globalErrorManager`() = runTest {
        everySuspend { mockRepository.searchDialogues(any()) } throws RuntimeException("Fake Network Error")

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
        everySuspend {
            mockAssetRepository.getAsset("http://example.com")
        } throws AssetFetchException(message = "Fake Network Error")

        viewModel.playAudio("http://example.com")
        testScheduler.advanceUntilIdle()
        val error = globalErrorManager.currentError.value
        error.shouldNotBeNull()
        error.message shouldBe UiText.from("Fake Network Error")
        error.title shouldBe UiText.from(Res.string.audio_playback_error_title)
    }

    @Test
    fun playAudio_whenAudioPlayExceptionThrown_updatesAudioErrorStateAndDoesNotChangeSearchState() = runTest {
        val fakeAsset = AssetData(ByteArray(0), "audio/wav")
        everySuspend { mockAssetRepository.getAsset("url") } returns fakeAsset
        everySuspend {
            mockAudioPlayer.play(Audio(fakeAsset.data, fakeAsset.mimeType))
        } throws AudioPlayException(message = "Fake Audio Error")

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
        everySuspend { mockRepository.getDialogue("123") } returns DomainMothers.DIALOGUE_1

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

        verifySuspend { mockRepository.getDialogue("123") }
    }

    @Test
    fun `displayDialogue transitions to global Error on failure`() = runTest {
        everySuspend { mockRepository.getDialogue("123") } throws RuntimeException("Fake Network Error")

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
        everySuspend { mockRepository.searchDialogues("another query") } returns persistentListOf()

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
