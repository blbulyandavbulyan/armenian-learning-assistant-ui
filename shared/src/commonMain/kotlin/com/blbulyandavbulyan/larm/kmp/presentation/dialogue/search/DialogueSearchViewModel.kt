package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.audio_playback_error_title
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_display_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_search_dialogues
import armenianlearningassistant_kmp.shared.generated.resources.error_unknown
import com.blbulyandavbulyan.larm.kmp.audio.AudioPlayException
import com.blbulyandavbulyan.larm.kmp.audio.AudioPlayer
import com.blbulyandavbulyan.larm.kmp.core.UiText
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.network.AssetFetchException
import com.blbulyandavbulyan.larm.kmp.network.AssetRepository
import com.blbulyandavbulyan.larm.kmp.network.DialogueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DialogueSearchViewModel(
    private val repository: DialogueRepository,
    private val assetRepository: AssetRepository,
    private val globalErrorManager: GlobalErrorManager
) : ViewModel() {
    private val audioPlayer = AudioPlayer()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @Suppress("TooGenericExceptionCaught")
    fun searchDialogues(query: String, onSuccess: () -> Unit = {}, onError: () -> Unit = {}) {
        _searchQuery.value = query // TODO, where the heck are the tests for these? You can add the check for it in the existing tests (if they exist)
        if (query.isBlank()) return
        _searchState.value = SearchState.Loading
        viewModelScope.launch {
            try {
                val response = repository.searchDialogues(query)
                _searchState.value = SearchState.Success(response.dialogues)
                onSuccess()
            } catch (e: Throwable) {
                _searchState.value = SearchState.Initial // TODO, WHY??? If user was on the chat screen, he stays there
                println(e)
                globalErrorManager.showError(
                    UiText.from(Res.string.error_failed_to_search_dialogues), // Or a specific search error title
                    UiText.from(e.message, Res.string.error_unknown)
                )
                onError()
            }
        }
    }

    fun playAudio(url: String) {
        viewModelScope.launch {
            try {
                val bytes = assetRepository.getAssetBytes(url)
                audioPlayer.play(bytes)
            } catch (e: AudioPlayException) {
                println(e)
                globalErrorManager.showError(
                    UiText.from(Res.string.audio_playback_error_title),
                    UiText.from(e.message, Res.string.error_unknown)
                )
            } catch (e: AssetFetchException) {
                println(e)
                globalErrorManager.showError(
                    UiText.from(Res.string.audio_playback_error_title),
                    UiText.from(e.message, Res.string.error_unknown)
                )
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun displayDialogue(id: String, onDialogueReady: (GetDialogueResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val fullDialogue = repository.getDialogue(id)
                onDialogueReady(fullDialogue)
            } catch (e: Throwable) {
                println(e)
                globalErrorManager.showError(
                    UiText.from(Res.string.error_failed_to_display_dialogue),
                    UiText.from(e.message, Res.string.error_unknown)
                )
            }
        }
    }
}
