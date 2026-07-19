package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat

import app.cash.turbine.test
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponseMother
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueTitleResponse
import com.blbulyandavbulyan.larm.kmp.network.FakeAssetRepository
import com.blbulyandavbulyan.larm.kmp.network.FakeDialogueRepository
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
    private lateinit var fakeRepository: FakeDialogueRepository
    private lateinit var fakeAudioRepository: FakeAssetRepository
    private lateinit var globalErrorManager: GlobalErrorManager
    private lateinit var viewModel: DialogueChatViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeDialogueRepository()
        fakeAudioRepository = FakeAssetRepository()
        globalErrorManager = GlobalErrorManager()
        viewModel = DialogueChatViewModel(fakeRepository, globalErrorManager)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `generateDialogue adds UserMessage, shows Loading, and ends with AiResponse`() = runTest {
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
    }

    @Test
    fun `generateDialogue adds UserMessage, shows Loading, and ends with Error on failure`() = runTest {
        fakeRepository.shouldFail = true
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
            error.message shouldBe "Fake Network Error"
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
        val dialogue = DialogueChatResponse(
            message = "Test",
            info = DialogueTitleResponse("T", "T", emptyList()),
            speakers = emptyList(),
            dialoguePhrases = emptyList()
        )
        fakeRepository.shouldFail = true
        viewModel.conversation.test {
            awaitItem() shouldBe emptyList()
            viewModel.saveDialogue(dialogue)
            testScheduler.advanceUntilIdle()
            val error = globalErrorManager.currentError.value
            error.shouldNotBeNull()
            error.message shouldBe "Fake Network Error"
        }
    }

    @Test
    fun `saveDialogue updates state correctly on single success`() = runTest {
        val fakeResponse = DialogueChatResponseMother.FULL_DIALOGUE_1
        fakeRepository.dialoguesToReturn.add(fakeResponse)
        fakeRepository.saveCompletable = CompletableDeferred()
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
            fakeRepository.saveCompletable?.complete("")
            val finalState = awaitItem()
            val aiSaved = finalState.last() as ConversationItem.AiResponse
            aiSaved.isSaving shouldBe false
            aiSaved.isSaved shouldBe true
            fakeRepository.lastSavedDialogue shouldBe dialogue
        }
    }

    @Test
    fun `saveDialogue multiple saves concurrent states`() = runTest {
        val dialogue1 = DialogueChatResponseMother.FULL_DIALOGUE_1
        val dialogue2 = DialogueChatResponseMother.FULL_DIALOGUE_2
        fakeRepository.dialoguesToReturn.add(dialogue1)
        fakeRepository.dialoguesToReturn.add(dialogue2)
        viewModel.generateDialogue("p1")
        testScheduler.advanceUntilIdle()
        viewModel.generateDialogue("p2")
        testScheduler.advanceUntilIdle()
        val state = viewModel.conversation.value
        val ai1 = state[1] as ConversationItem.AiResponse
        val ai2 = state[3] as ConversationItem.AiResponse
        fakeRepository.saveCompletable = CompletableDeferred()
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
            fakeRepository.saveCompletable?.complete("")
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
        val dialogue1 = DialogueChatResponseMother.FULL_DIALOGUE_1
        val dialogue2 = DialogueChatResponseMother.FULL_DIALOGUE_2
        fakeRepository.dialoguesToReturn.add(dialogue1)
        fakeRepository.dialoguesToReturn.add(dialogue2)
        viewModel.generateDialogue("p1")
        testScheduler.advanceUntilIdle()
        viewModel.generateDialogue("p2")
        testScheduler.advanceUntilIdle()
        val state = viewModel.conversation.value
        val ai1 = state[1] as ConversationItem.AiResponse
        val ai2 = state[3] as ConversationItem.AiResponse
        fakeRepository.saveCompletable = CompletableDeferred()
        viewModel.conversation.test {
            awaitItem()
            fakeRepository.shouldFail = false
            viewModel.saveDialogue(ai1.response)
            testScheduler.runCurrent()
            val stateAfterSave1 = awaitItem()
            (stateAfterSave1[1] as ConversationItem.AiResponse).isSaving shouldBe true
            (stateAfterSave1[3] as ConversationItem.AiResponse).isSaving shouldBe false
            fakeRepository.shouldFail = true
            viewModel.saveDialogue(ai2.response)
            val stateAfterSave2 = awaitItem()
            (stateAfterSave2[1] as ConversationItem.AiResponse).isSaving shouldBe true
            (stateAfterSave2[3] as ConversationItem.AiResponse).isSaving shouldBe true
            val stateAfterError = awaitItem()
            testScheduler.runCurrent()
            (stateAfterError[1] as ConversationItem.AiResponse).isSaving shouldBe true
            (stateAfterError[3] as ConversationItem.AiResponse).isSaving shouldBe false
            (stateAfterError[3] as ConversationItem.AiResponse).isSaved shouldBe false
            globalErrorManager.currentError.value?.message shouldBe "Fake Network Error"
            fakeRepository.saveCompletable?.complete("")
            val finalState = awaitItem()
            (finalState[1] as ConversationItem.AiResponse).isSaving shouldBe false
            (finalState[1] as ConversationItem.AiResponse).isSaved shouldBe true
            (finalState[3] as ConversationItem.AiResponse).isSaving shouldBe false
            (finalState[3] as ConversationItem.AiResponse).isSaved shouldBe false
        }
    }
}
