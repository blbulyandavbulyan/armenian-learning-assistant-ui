package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat

import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.GeneratedDialogue
import kotlin.uuid.Uuid

sealed class ConversationItem {
    data class UserMessage(val text: String) : ConversationItem()
    data class AiResponse(
        val response: GeneratedDialogue,
        val isSaving: Boolean = false,
        val isSaved: Boolean = false
    ) : ConversationItem()
    data object Loading : ConversationItem()
    data class Error(val failedPrompt: String, val id: String = Uuid.random().toString()) : ConversationItem()
}
