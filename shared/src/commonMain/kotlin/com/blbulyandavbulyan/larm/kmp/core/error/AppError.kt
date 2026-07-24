package com.blbulyandavbulyan.larm.kmp.core.error

import com.blbulyandavbulyan.larm.kmp.core.UiText
import kotlin.uuid.Uuid

data class AppError(
    val title: UiText,
    val message: UiText,
    val id: String = Uuid.random().toString() // Keeps unique instances
)
