package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat

import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.GeneratedDialogue
import kotlin.uuid.Uuid

sealed class ConversationItem {
    abstract val id: String
    data class UserMessage(val text: String, override val id: String = Uuid.random().toString()) : ConversationItem()
    data class AiResponse(
        val response: GeneratedDialogue,
        val isSaving: Boolean = false,
        val isSaved: Boolean = false,
        override val id: String = Uuid.random().toString()
    ) : ConversationItem()
    data class Loading(override val id: String = Uuid.random().toString()) : ConversationItem()
    data class Error(val failedPrompt: String, override val id: String = Uuid.random().toString()) : ConversationItem()
}
