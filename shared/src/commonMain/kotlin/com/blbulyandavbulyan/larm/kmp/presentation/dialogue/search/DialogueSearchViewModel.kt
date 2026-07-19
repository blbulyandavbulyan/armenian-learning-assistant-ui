package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.audio_playback_error_title
import armenianlearningassistant_kmp.shared.generated.resources.error_prefix
import armenianlearningassistant_kmp.shared.generated.resources.error_unknown
import com.blbulyandavbulyan.larm.kmp.audio.AudioPlayException
import com.blbulyandavbulyan.larm.kmp.audio.AudioPlayer
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.network.AssetRepository
import com.blbulyandavbulyan.larm.kmp.network.AudioFetchException
import com.blbulyandavbulyan.larm.kmp.network.DialogueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

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
    fun searchDialogues(query: String) {
        _searchQuery.value = query // TODO, where the heck are the tests for these? You can add the check for it in the existing tests (if they exist)
        if (query.isBlank()) return
        _searchState.value = SearchState.Loading
        viewModelScope.launch {
            try {
                val response = repository.searchDialogues(query)
                _searchState.value = SearchState.Success(response.dialogues)
            } catch (e: Exception) {
                _searchState.value = SearchState.Initial // TODO, WHY??? If user was on the chat screen, he stays there
                println(e)
                globalErrorManager.showError(
                    // TODO, not OR, BUT SPECIFIC ERROR TITLE!!!!!
                    getString(Res.string.error_prefix), // Or a specific search error title
                    e.message ?: getString(Res.string.error_unknown)
                )
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
                    getString(Res.string.audio_playback_error_title),
                    e.message ?: getString(Res.string.error_unknown)
                )
            } catch (e: AudioFetchException) {
                println(e)
                globalErrorManager.showError(
                    getString(Res.string.audio_playback_error_title),
                    e.message ?: getString(Res.string.error_unknown)
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
            } catch (e: Exception) {
                println(e)
                globalErrorManager.showError(
                    getString(Res.string.error_prefix),
                    e.message ?: getString(Res.string.error_unknown)
                )
            }
        }
    }
}