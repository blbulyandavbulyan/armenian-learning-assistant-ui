package com.blbulyandavbulyan.larm.kmp.presentation.global

import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse

sealed class ScreenState {
    data object Generator : ScreenState()
    data object Search : ScreenState()
    data class Detail(val dialogue: GetDialogueResponse) : ScreenState()
    data object Loading : ScreenState()
}
