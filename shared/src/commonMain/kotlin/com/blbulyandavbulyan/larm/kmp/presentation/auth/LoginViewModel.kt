package com.blbulyandavbulyan.larm.kmp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.auth_error_title
import armenianlearningassistant_kmp.shared.generated.resources.error_unknown
import com.blbulyandavbulyan.larm.kmp.core.UiText
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val globalErrorManager: GlobalErrorManager
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    @Suppress("TooGenericExceptionCaught")
    fun signInWithGoogle() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.signInWithGoogle()
            } catch (e: Exception) {
                globalErrorManager.showError(
                    UiText.from(Res.string.auth_error_title),
                    UiText.from(e.message, Res.string.error_unknown)
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
