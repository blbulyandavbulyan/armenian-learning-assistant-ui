package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search

import com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.search.DialogueSummary
import kotlinx.collections.immutable.ImmutableList

sealed class SearchState {
    data object Initial : SearchState()
    data object Loading : SearchState()
    data object Error : SearchState()
    data class Success(val results: ImmutableList<DialogueSummary>) : SearchState()
}
