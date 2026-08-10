package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat

import app.cash.turbine.test
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_generate_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_save_dialogue
import com.blbulyandavbulyan.larm.kmp.core.UiText
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.DialogueTitle
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.GeneratedDialogue
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.GeneratedDialogueMother
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.chat.DialogueChatRepository
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
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
class DialogueChatViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository = mock<DialogueChatRepository>()
    private lateinit var globalErrorManager: GlobalErrorManager
    private lateinit var viewModel: DialogueChatViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        globalErrorManager = GlobalErrorManager()
        viewModel = DialogueChatViewModel(mockRepository, globalErrorManager)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `generateDialogue adds UserMessage, shows Loading, and ends with AiResponse`() = runTest {
        val expectedDialogue = GeneratedDialogue(
            message = "Here is your dialogue",
            info = DialogueTitle("Title", "Transcription", persistentListOf()),
            speakers = persistentListOf(),
            phrases = persistentListOf()
        )
        everySuspend { mockRepository.generateDialogue(any(), any()) } returns expectedDialogue

        viewModel.conversation.test {
            awaitItem() shouldBe emptyList()
            val prompt = "grocery store"
            viewModel.generateDialogue(prompt)
            val stateWithLoading = awaitItem()
            stateWithLoading.size shouldBe 2
            stateWithLoading[0].shouldBeInstanceOf<ConversationItem.UserMessage>().text shouldBe prompt
            stateWithLoading[1].shouldBeInstanceOf<ConversationItem.Loading>()
            val finalState = awaitItem()
            finalState.size shouldBe 2
            finalState[0].shouldBeInstanceOf<ConversationItem.UserMessage>()
            finalState[1].shouldBeInstanceOf<ConversationItem.AiResponse>().response.message shouldBe "Here is your dialogue"
            testScheduler.advanceUntilIdle()
            expectNoEvents()
        }

        verifySuspend { mockRepository.generateDialogue("grocery store", any()) }
    }

    @Test
    fun `generateDialogue adds UserMessage, shows Loading, and ends with Error on failure`() = runTest {
        everySuspend { mockRepository.generateDialogue(any(), any()) } throws RuntimeException("Fake Network Error")

        viewModel.conversation.test {
            awaitItem() shouldBe emptyList()
            val prompt = "fail please"
            viewModel.generateDialogue(prompt)
            val stateWithLoading = awaitItem()
            stateWithLoading.size shouldBe 2
            stateWithLoading[1].shouldBeInstanceOf<ConversationItem.Loading>()
            val finalState = awaitItem()
            finalState.size shouldBe 1
            testScheduler.advanceUntilIdle()
            val error = globalErrorManager.currentError.value
            error.shouldNotBeNull()
            error.message shouldBe UiText.from("Fake Network Error")
            error.title shouldBe UiText.from(Res.string.error_failed_to_generate_dialogue)
            expectNoEvents()
        }
    }

    @Test
    fun `generateDialogue does nothing when prompt is blank`() = runTest {
        viewModel.conversation.test {
            awaitItem() shouldBe emptyList()
            viewModel.generateDialogue("   ")
            testScheduler.advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `saveDialogue adds Error to global error on failure`() = runTest {
        val dialogue = GeneratedDialogue(
            message = "Test",
            info = DialogueTitle("T", "T", persistentListOf()),
            speakers = persistentListOf(),
            phrases = persistentListOf()
        )
        everySuspend { mockRepository.saveDialogue(dialogue) } throws RuntimeException("Fake Network Error")

        viewModel.conversation.test {
            awaitItem() shouldBe emptyList()
            viewModel.saveDialogue(dialogue)
            testScheduler.advanceUntilIdle()
            val error = globalErrorManager.currentError.value
            error.shouldNotBeNull()
            error.message shouldBe UiText.from("Fake Network Error")
            error.title shouldBe UiText.from(Res.string.error_failed_to_save_dialogue)
        }
    }

    @Test
    fun `saveDialogue updates state correctly on single success`() = runTest {
        val fakeResponse = GeneratedDialogueMother.FULL_DIALOGUE_1
        val saveDeferred = CompletableDeferred<String>()

        everySuspend { mockRepository.generateDialogue(any(), any()) } returns fakeResponse
        everySuspend { mockRepository.saveDialogue(fakeResponse) } calls { saveDeferred.await() }

        viewModel.generateDialogue("prompt")
        testScheduler.advanceUntilIdle()
        val generatedState = viewModel.conversation.value
        val dialogue = (generatedState.last() as ConversationItem.AiResponse).response
        viewModel.conversation.test {
            awaitItem()
            viewModel.saveDialogue(dialogue)
            val savingState = awaitItem()
            val aiSaving = savingState.last() as ConversationItem.AiResponse
            aiSaving.isSaving shouldBe true
            aiSaving.isSaved shouldBe false

            saveDeferred.complete("fake-uuid-1234")
            val finalState = awaitItem()
            val aiSaved = finalState.last() as ConversationItem.AiResponse
            aiSaved.isSaving shouldBe false
            aiSaved.isSaved shouldBe true
        }

        verifySuspend { mockRepository.saveDialogue(dialogue) }
    }

    @Test
    fun `saveDialogue multiple saves concurrent states`() = runTest {
        val dialogue1 = GeneratedDialogueMother.FULL_DIALOGUE_1
        val dialogue2 = GeneratedDialogueMother.FULL_DIALOGUE_2
        val saveDeferred = CompletableDeferred<String>()

        everySuspend { mockRepository.generateDialogue("p1", any()) } returns dialogue1
        everySuspend { mockRepository.generateDialogue("p2", any()) } returns dialogue2
        everySuspend { mockRepository.saveDialogue(any()) } calls { saveDeferred.await() }

        viewModel.generateDialogue("p1")
        testScheduler.advanceUntilIdle()
        viewModel.generateDialogue("p2")
        testScheduler.advanceUntilIdle()

        val state = viewModel.conversation.value
        val ai1 = state[1] as ConversationItem.AiResponse
        val ai2 = state[3] as ConversationItem.AiResponse

        viewModel.conversation.test {
            awaitItem()
            viewModel.saveDialogue(ai1.response)
            val stateAfterSave1 = awaitItem()
            (stateAfterSave1[1] as ConversationItem.AiResponse).isSaving shouldBe true
            (stateAfterSave1[3] as ConversationItem.AiResponse).isSaving shouldBe false

            viewModel.saveDialogue(ai2.response)
            val stateAfterSave2 = awaitItem()
            (stateAfterSave2[1] as ConversationItem.AiResponse).isSaving shouldBe true
            (stateAfterSave2[3] as ConversationItem.AiResponse).isSaving shouldBe true

            saveDeferred.complete("fake-uuid-1234")
            val stateAfterComplete1 = awaitItem()
            val ai1Intermediate = stateAfterComplete1[1] as ConversationItem.AiResponse
            val ai2Intermediate = stateAfterComplete1[3] as ConversationItem.AiResponse
            ai1Intermediate.isSaved shouldBe true
            ai2Intermediate.isSaved shouldBe false

            val finalState = awaitItem()
            (finalState[1] as ConversationItem.AiResponse).isSaving shouldBe false
            (finalState[1] as ConversationItem.AiResponse).isSaved shouldBe true
            (finalState[3] as ConversationItem.AiResponse).isSaving shouldBe false
            (finalState[3] as ConversationItem.AiResponse).isSaved shouldBe true
        }
    }

    @Test
    fun `saveDialogue concurrent saves with one success and one failure`() = runTest {
        val dialogue1 = GeneratedDialogueMother.FULL_DIALOGUE_1
        val dialogue2 = GeneratedDialogueMother.FULL_DIALOGUE_2
        val saveDeferred = CompletableDeferred<String>()

        everySuspend { mockRepository.generateDialogue("p1", any()) } returns dialogue1
        everySuspend { mockRepository.generateDialogue("p2", any()) } returns dialogue2
        everySuspend { mockRepository.saveDialogue(dialogue1) } calls { saveDeferred.await() }
        everySuspend { mockRepository.saveDialogue(dialogue2) } throws RuntimeException("Fake Network Error")

        viewModel.generateDialogue("p1")
        testScheduler.advanceUntilIdle()
        viewModel.generateDialogue("p2")
        testScheduler.advanceUntilIdle()

        val state = viewModel.conversation.value
        val ai1 = state[1] as ConversationItem.AiResponse
        val ai2 = state[3] as ConversationItem.AiResponse

        viewModel.conversation.test {
            awaitItem()
            viewModel.saveDialogue(ai1.response)
            testScheduler.runCurrent()
            val stateAfterSave1 = awaitItem()
            (stateAfterSave1[1] as ConversationItem.AiResponse).isSaving shouldBe true
            (stateAfterSave1[3] as ConversationItem.AiResponse).isSaving shouldBe false

            viewModel.saveDialogue(ai2.response)
            val stateAfterSave2 = awaitItem()
            (stateAfterSave2[1] as ConversationItem.AiResponse).isSaving shouldBe true
            (stateAfterSave2[3] as ConversationItem.AiResponse).isSaving shouldBe true

            val stateAfterError = awaitItem()
            testScheduler.runCurrent()
            (stateAfterError[1] as ConversationItem.AiResponse).isSaving shouldBe true
            (stateAfterError[3] as ConversationItem.AiResponse).isSaving shouldBe false
            (stateAfterError[3] as ConversationItem.AiResponse).isSaved shouldBe false
            globalErrorManager.currentError.value?.message shouldBe UiText.from("Fake Network Error")
            globalErrorManager.currentError.value?.title shouldBe UiText.from(Res.string.error_failed_to_save_dialogue)

            saveDeferred.complete("fake-uuid-1234")
            val finalState = awaitItem()
            (finalState[1] as ConversationItem.AiResponse).isSaving shouldBe false
            (finalState[1] as ConversationItem.AiResponse).isSaved shouldBe true
            (finalState[3] as ConversationItem.AiResponse).isSaving shouldBe false
            (finalState[3] as ConversationItem.AiResponse).isSaved shouldBe false
        }
    }
}
