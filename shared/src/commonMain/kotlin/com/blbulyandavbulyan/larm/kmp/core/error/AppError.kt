package com.blbulyandavbulyan.larm.kmp.core.error

import com.blbulyandavbulyan.larm.kmp.core.UiText

data class AppError(
    val title: UiText,
    val message: UiText,
    val id: String = kotlin.uuid.Uuid.random().toString() // Keeps unique instances if needed
)
