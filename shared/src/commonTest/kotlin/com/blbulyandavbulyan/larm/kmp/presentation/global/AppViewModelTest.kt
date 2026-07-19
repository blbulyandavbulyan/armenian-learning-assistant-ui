package com.blbulyandavbulyan.larm.kmp.presentation.global

import app.cash.turbine.test
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.PhraseResponse
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

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AppViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AppViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `navigation state defaults to Generator and updates correctly`() = runTest {
        viewModel.currentScreen.test {
            awaitItem().shouldBeInstanceOf<ScreenState.Generator>()
            viewModel.navigateToSearch()
            awaitItem().shouldBeInstanceOf<ScreenState.Search>()
            val fakeDialogue = GetDialogueResponse(
                id = "1",
                title = PhraseResponse("1", "Title", "en", "Trans", emptyList(), emptyList()),
                speakers = emptyList(),
                dialoguePhrases = emptyList()
            )
            viewModel.navigateToDetail(fakeDialogue)
            awaitItem().shouldBeInstanceOf<ScreenState.Detail>().dialogue.id shouldBe "1"
            viewModel.navigateToGenerator()
            awaitItem().shouldBeInstanceOf<ScreenState.Generator>()
        }
    }
}
