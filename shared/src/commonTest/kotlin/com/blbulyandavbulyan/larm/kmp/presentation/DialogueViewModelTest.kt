package com.blbulyandavbulyan.larm.kmp.presentation

import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.DialogueTitleResponse
import com.blbulyandavbulyan.larm.kmp.network.DialogueRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

import app.cash.turbine.test

// 1. Create a Fake implementation of the Repository for testing
class FakeDialogueRepository : DialogueRepository {
    var shouldFail = false
    var lastPrompt = ""

    override suspend fun generateDialogue(prompt: String, chatId: String): DialogueChatResponse {
        lastPrompt = prompt
        if (shouldFail) {
            throw Exception("Fake Network Error")
        }
        
        // Return mock data
        return DialogueChatResponse(
            message = "Here is your dialogue",
            info = DialogueTitleResponse("Title", "Transcription", emptyList()),
            speakers = emptyList(),
            dialoguePhrases = emptyList()
        )
    }

    override suspend fun saveDialogue(dialogue: DialogueChatResponse): String {
        if (shouldFail) {
            throw Exception("Fake Network Error")
        }
        return "fake-uuid-1234"
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DialogueViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeDialogueRepository
    private lateinit var viewModel: DialogueViewModel

    @BeforeTest
    fun setup() {
        // Required for viewModelScope coroutines in unit tests
        Dispatchers.setMain(testDispatcher)

        fakeRepository = FakeDialogueRepository()
        viewModel = DialogueViewModel(fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `generateDialogue adds UserMessage, shows Loading, and ends with AiResponse`() = runTest {
        viewModel.conversation.test {
            // Initial state is an empty list
            awaitItem() shouldBe emptyList()

            // Trigger generation
            val prompt = "grocery store"
            viewModel.generateDialogue(prompt)

            // The next state should have the UserMessage and the Loading indicator
            val stateWithLoading = awaitItem()
            stateWithLoading.size shouldBe 2
            stateWithLoading[0].shouldBeInstanceOf<ConversationItem.UserMessage>().text shouldBe prompt
            stateWithLoading[1].shouldBeInstanceOf<ConversationItem.Loading>()

            // The final state should remove Loading and add the AiResponse
            val finalState = awaitItem()
            finalState.size shouldBe 2
            finalState[0].shouldBeInstanceOf<ConversationItem.UserMessage>()
            finalState[1].shouldBeInstanceOf<ConversationItem.AiResponse>().response.message shouldBe "Here is your dialogue"

            // Wait for coroutines to finish and ensure no more events are emitted
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
            finalState.size shouldBe 2
            finalState[1].shouldBeInstanceOf<ConversationItem.Error>().message shouldBe "Fake Network Error"

            testScheduler.advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `generateDialogue does nothing when prompt is blank`() = runTest {
        viewModel.conversation.test {
            awaitItem() shouldBe emptyList()

            viewModel.generateDialogue("   ")

            // Wait for coroutines to ensure no events are emitted
            testScheduler.advanceUntilIdle()
            expectNoEvents()
        }
    }
    // TODO the proper test for saveDialogue has to be written,
    //  asserting the state when error ocurred for saving some of the dialogues, and some of them suceed,
    //  must assert isSaving, isSaved properly
    //  assuming that conversation has more then one dialogue
    // TODO the proper object must be send in the DialogueRepository which is associated with the

    @Test
    fun `saveDialogue adds Error to conversation on failure`() = runTest {
        // Setup initial conversation with a generated dialogue
        val dialogue = DialogueChatResponse(
            message = "Test",
            info = DialogueTitleResponse("T", "T", emptyList()),
            speakers = emptyList(),
            dialoguePhrases = emptyList()
        )
        // Set fake to fail on save
        fakeRepository.shouldFail = true

        viewModel.conversation.test {
            awaitItem() shouldBe emptyList()

            viewModel.saveDialogue(dialogue)

            // The next state should append the Error
            val stateWithError = awaitItem()
            stateWithError.size shouldBe 1
            stateWithError[0].shouldBeInstanceOf<ConversationItem.Error>().message shouldBe "Fake Network Error"

            testScheduler.advanceUntilIdle()
            expectNoEvents()
        }
    }
}
