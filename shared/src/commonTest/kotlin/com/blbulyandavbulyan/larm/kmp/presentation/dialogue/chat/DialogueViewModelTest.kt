package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat

import app.cash.turbine.test
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponseMother
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueTitleResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.PhraseResponse
import com.blbulyandavbulyan.larm.kmp.network.FakeAssetRepository
import com.blbulyandavbulyan.larm.kmp.network.FakeDialogueRepository
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
class DialogueViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeDialogueRepository
    private lateinit var fakeAudioRepository: FakeAssetRepository
    private lateinit var viewModel: DialogueViewModel

    @BeforeTest
    fun setup() {
        // Required for viewModelScope coroutines in unit tests
        Dispatchers.setMain(testDispatcher)

        fakeRepository = FakeDialogueRepository()
        fakeAudioRepository = FakeAssetRepository()
        viewModel = DialogueViewModel(fakeRepository, fakeAudioRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `navigation state defaults to Generator and updates correctly`() = runTest {
        viewModel.currentScreen.test {
            // Initial state
            awaitItem().shouldBeInstanceOf<ScreenState.Generator>()

            // Navigate to Search
            viewModel.navigateToSearch()
            awaitItem().shouldBeInstanceOf<ScreenState.Search>()

            // Navigate to Detail
            val fakeDialogue = GetDialogueResponse(
                id = "1",
                title = PhraseResponse("1", "Title", "en", "Trans", emptyList(), emptyList()),
                speakers = emptyList(),
                dialoguePhrases = emptyList()
            )
            viewModel.navigateToDetail(fakeDialogue)
            awaitItem().shouldBeInstanceOf<ScreenState.Detail>().dialogue.id shouldBe "1"

            // Navigate to Generator
            viewModel.navigateToGenerator()
            awaitItem().shouldBeInstanceOf<ScreenState.Generator>()
        }
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

    @Test
    fun `saveDialogue updates state correctly on single success`() = runTest {
        val fakeResponse = DialogueChatResponseMother.FULL_DIALOGUE_1

        fakeRepository.dialoguesToReturn.add(fakeResponse)
        fakeRepository.saveCompletable = CompletableDeferred()

        viewModel.generateDialogue("prompt")
        testScheduler.advanceUntilIdle() // Wait for it to finish generating
        val generatedState = viewModel.conversation.value
        val dialogue = (generatedState.last() as ConversationItem.AiResponse).response

        viewModel.conversation.test {
            awaitItem() // Skip current state

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
            // The first awaitItem() consumes the current initial state of the StateFlow upon subscription.
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
            // Assert that the first dialogue save has completed in this intermediate state.
            // Coroutines waiting on the same CompletableDeferred resume in the order they were suspended (FIFO).
            // Since saveDialogue for ai1 was called first, it resumes and emits its saved state first.
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
            awaitItem() // Skip initial state

            // 1. Save ai1 (success pending)
            fakeRepository.shouldFail = false
            viewModel.saveDialogue(ai1.response)

            // Force the test dispatcher to run the queued coroutine for ai1 so it reads shouldFail = false
            // and safely suspends on saveCompletable?.await() before we flip the flag below.
            testScheduler.runCurrent()

            val stateAfterSave1 = awaitItem()
            (stateAfterSave1[1] as ConversationItem.AiResponse).isSaving shouldBe true
            (stateAfterSave1[3] as ConversationItem.AiResponse).isSaving shouldBe false

            // 2. Save ai2 (immediate failure)
            fakeRepository.shouldFail = true
            viewModel.saveDialogue(ai2.response)

            val stateAfterSave2 = awaitItem() // Synchronous update before launch executes
            (stateAfterSave2[1] as ConversationItem.AiResponse).isSaving shouldBe true
            (stateAfterSave2[3] as ConversationItem.AiResponse).isSaving shouldBe true

            // Now the launch coroutines will execute because awaitItem yields.
            // ai2 throws Exception immediately and updates state.
            val stateAfterError = awaitItem()
            (stateAfterError[1] as ConversationItem.AiResponse).isSaving shouldBe true
            (stateAfterError[3] as ConversationItem.AiResponse).isSaving shouldBe false
            (stateAfterError[3] as ConversationItem.AiResponse).isSaved shouldBe false
            stateAfterError.last().shouldBeInstanceOf<ConversationItem.Error>().message shouldBe "Fake Network Error"

            // 3. Complete ai1
            fakeRepository.saveCompletable?.complete("")

            val finalState = awaitItem()
            (finalState[1] as ConversationItem.AiResponse).isSaving shouldBe false
            (finalState[1] as ConversationItem.AiResponse).isSaved shouldBe true
            (finalState[3] as ConversationItem.AiResponse).isSaving shouldBe false
            (finalState[3] as ConversationItem.AiResponse).isSaved shouldBe false
        }
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
    fun `searchDialogues transitions to Error on failure`() = runTest {
        fakeRepository.shouldFail = true
        viewModel.searchState.test {
            awaitItem() shouldBe SearchState.Initial

            viewModel.searchDialogues("query")

            awaitItem() shouldBe SearchState.Loading

            awaitItem().shouldBeInstanceOf<SearchState.Error>().message shouldBe "Fake Network Error"

            testScheduler.advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `playAudio transitions to Error on failure`() = runTest {
        fakeAudioRepository.shouldFail = true
        viewModel.searchState.test {
            awaitItem() // Skip Initial

            viewModel.playAudio("http://example.com")

            awaitItem().shouldBeInstanceOf<SearchState.Error>().message shouldBe "Fake Network Error"

            testScheduler.advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `onDialogueSelected transitions to Detail`() = runTest {
        viewModel.currentScreen.test {
            awaitItem() // Skip Initial Generator

            viewModel.onDialogueSelected("123")

            awaitItem().shouldBeInstanceOf<ScreenState.Detail>().dialogue.id shouldBe "123"

            testScheduler.advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `onDialogueSelected transitions to Error on failure`() = runTest {
        fakeRepository.shouldFail = true
        viewModel.searchState.test {
            awaitItem() // Skip Initial

            viewModel.onDialogueSelected("123")

            awaitItem().shouldBeInstanceOf<SearchState.Error>().message shouldBe "Fake Network Error"

            testScheduler.advanceUntilIdle()
            expectNoEvents()
        }
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
