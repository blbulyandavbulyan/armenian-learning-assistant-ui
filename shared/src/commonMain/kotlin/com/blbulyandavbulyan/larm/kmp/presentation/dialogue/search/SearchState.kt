package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search

import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.DialogueSummaryResponse

sealed class SearchState {
    data object Initial : SearchState()
    data object Loading : SearchState()
    data class Success(val results: List<DialogueSummaryResponse>) : SearchState()
}