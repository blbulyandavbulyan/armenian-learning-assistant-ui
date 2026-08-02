package com.blbulyandavbulyan.larm.kmp.presentation.global

import androidx.lifecycle.ViewModel
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Dialogue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel : ViewModel() {
    private val _currentScreen = MutableStateFlow<ScreenState>(ScreenState.Generator)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    fun navigateToSearch() {
        _currentScreen.value = ScreenState.Search
    }

    fun navigateToLoading() {
        _currentScreen.value = ScreenState.Loading
    }

    fun navigateToGenerator() {
        _currentScreen.value = ScreenState.Generator
    }

    fun navigateToDetail(dialogue: Dialogue) {
        _currentScreen.value = ScreenState.Detail(dialogue)
    }
}
