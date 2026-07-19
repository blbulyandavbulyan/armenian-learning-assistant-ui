package com.blbulyandavbulyan.larm.kmp.core.error

data class AppError(
    val title: String,
    val message: String,
    val id: String = kotlin.uuid.Uuid.random().toString() // Keeps unique instances if needed
)
