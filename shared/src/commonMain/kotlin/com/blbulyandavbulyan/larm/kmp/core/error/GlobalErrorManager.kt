package com.blbulyandavbulyan.larm.kmp.core.error

import com.blbulyandavbulyan.larm.kmp.core.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GlobalErrorManager {
    private val _currentError = MutableStateFlow<AppError?>(null)
    val currentError = _currentError.asStateFlow()

    fun showError(title: UiText, message: UiText) {
        _currentError.value = AppError(title, message)
    }

    fun dismissError() {
        _currentError.value = null
    }
}
