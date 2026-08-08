package com.blbulyandavbulyan.larm.kmp.presentation.global

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blbulyandavbulyan.larm.kmp.di.AppModule
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthState
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Dialogue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val authStateFlow: Flow<AuthState> = AppModule.authRepository.observeAuthState()
) : ViewModel() {
    private val _currentScreen = MutableStateFlow<ScreenState>(ScreenState.Generator)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    init {
        viewModelScope.launch {
            authStateFlow.collect { authState ->
                when (authState) {
                    AuthState.AUTHENTICATED -> {
                        if (_currentScreen.value is ScreenState.Login || _currentScreen.value is ScreenState.Loading) {
                            _currentScreen.value = ScreenState.Generator
                        }
                    }
                    AuthState.UNAUTHENTICATED -> {
                        _currentScreen.value = ScreenState.Login
                    }
                    AuthState.LOADING -> {
                        _currentScreen.value = ScreenState.Loading
                    }
                }
            }
        }
    }

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

    fun navigateToLogin() {
        _currentScreen.value = ScreenState.Login
    }
}
