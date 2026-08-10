package com.blbulyandavbulyan.larm.kmp.presentation.global

import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Dialogue

sealed class ScreenState {
    data object Generator : ScreenState()
    data object Search : ScreenState()
    data class Detail(val dialogue: Dialogue) : ScreenState()
    data object Loading : ScreenState()
    data object Login : ScreenState()
}
