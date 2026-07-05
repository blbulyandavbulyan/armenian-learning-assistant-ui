package com.blbulyandavbulyan.larm.kmp.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun onTokenReceived(jwtToken: String) {
        _authState.value = AuthState.Authenticated(jwtToken)
    }

    fun logout() {
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data class Authenticated(val jwtToken: String) : AuthState()
}
