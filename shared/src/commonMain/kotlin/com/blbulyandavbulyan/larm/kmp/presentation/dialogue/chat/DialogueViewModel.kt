package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_save
import armenianlearningassistant_kmp.shared.generated.resources.error_unknown
import com.blbulyandavbulyan.larm.kmp.audio.AudioPlayer
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.DialogueSummaryResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.network.AssetRepository
import com.blbulyandavbulyan.larm.kmp.network.DialogueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed class ConversationItem {
    data class UserMessage(val text: String) : ConversationItem()
    data class AiResponse(
        val response: DialogueChatResponse,
        val isSaving: Boolean = false,
        val isSaved: Boolean = false
    ) : ConversationItem()
    data class Error(val message: String) : ConversationItem()
    data object Loading : ConversationItem()
}

sealed class SearchState {
    data object Initial : SearchState()
    data object Loading : SearchState()
    data class Success(val results: List<DialogueSummaryResponse>) : SearchState()
    data class Error(val message: String) : SearchState()
}

sealed class ScreenState {
    data object Generator : ScreenState()
    data object Search : ScreenState()
    data class Detail(val dialogue: GetDialogueResponse) : ScreenState()
}

@OptIn(ExperimentalUuidApi::class)
class DialogueViewModel(
    private val repository: DialogueRepository,
    private val assetRepository: AssetRepository
) : ViewModel() {
    private val _conversation = MutableStateFlow<List<ConversationItem>>(emptyList())
    val conversation: StateFlow<List<ConversationItem>> = _conversation.asStateFlow()

    private val _currentScreen = MutableStateFlow<ScreenState>(ScreenState.Generator)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val audioPlayer = AudioPlayer()

    private val chatId = Uuid.random().toString()

    fun navigateToSearch() {
        _currentScreen.value = ScreenState.Search
    }

    fun navigateToGenerator() {
        _currentScreen.value = ScreenState.Generator
    }

    fun navigateToDetail(dialogue: GetDialogueResponse) {
        _currentScreen.value = ScreenState.Detail(dialogue)
    }

    @Suppress("TooGenericExceptionCaught")
    fun generateDialogue(prompt: String) {
        if (prompt.isBlank()) return

        val current = _conversation.value.toMutableList()
        current.add(ConversationItem.UserMessage(prompt))
        current.add(ConversationItem.Loading)
        _conversation.value = current

        viewModelScope.launch {
            try {
                val response = repository.generateDialogue(prompt, chatId)
                val newConv = _conversation.value.filter { it !is ConversationItem.Loading }.toMutableList()
                newConv.add(ConversationItem.AiResponse(response))
                _conversation.value = newConv
            } catch (e: Exception) {
                val newConv = _conversation.value.filter { it !is ConversationItem.Loading }.toMutableList()
                newConv.add(ConversationItem.Error(e.message ?: getString(Res.string.error_unknown)))
                _conversation.value = newConv
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun saveDialogue(dialogue: DialogueChatResponse) {
        _conversation.value = _conversation.value.map {
            if (it is ConversationItem.AiResponse && it.response === dialogue) {
                it.copy(isSaving = true)
            } else {
                it
            }
        }

        viewModelScope.launch {
            try {
                repository.saveDialogue(dialogue)
                _conversation.value = _conversation.value.map {
                    if (it is ConversationItem.AiResponse && it.response === dialogue) {
                        it.copy(isSaving = false, isSaved = true)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                val currentConv = _conversation.value.map {
                    if (it is ConversationItem.AiResponse && it.response === dialogue) {
                        it.copy(isSaving = false)
                    } else {
                        it
                    }
                }.toMutableList()
                currentConv.add(ConversationItem.Error(e.message ?: getString(Res.string.error_failed_to_save)))
                _conversation.value = currentConv
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @Suppress("TooGenericExceptionCaught")
    fun searchDialogues(query: String) {
        _searchQuery.value = query // TODO, where the heck are the tests for these? You can add the check for it in the existing tests (if they exist)
        if (query.isBlank()) return
        _searchState.value = SearchState.Loading
        viewModelScope.launch {
            try {
                val response = repository.searchDialogues(query)
                _searchState.value = SearchState.Success(response.dialogues)
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: getString(Res.string.error_unknown))
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun playAudio(url: String) {
        viewModelScope.launch {
            try {
                val bytes = assetRepository.getAssetBytes(url)
                audioPlayer.play(bytes)
            } catch (e: Exception) {
                // TODO moreover, almost ALL of the implementations of AudioPlayer -> SWALLOW THE FUCKING EXCETPION, so this won't even WORK AT ALL
                // TODO this is TOO bad for such error, this probably means that the ENTIRE SCREEN will display the dumb error,
                //  even though only AUDIO DOES NOT WORK !!!!
                _searchState.value = SearchState.Error(e.message ?: getString(Res.string.error_unknown))
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun onDialogueSelected(id: String) {
        viewModelScope.launch {
            try {
                val fullDialogue = repository.getDialogue(id)
                navigateToDetail(fullDialogue)
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: getString(Res.string.error_unknown))
            }
        }
    }
}
