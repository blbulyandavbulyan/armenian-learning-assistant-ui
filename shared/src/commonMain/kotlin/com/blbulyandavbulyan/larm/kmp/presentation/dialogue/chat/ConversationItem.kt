package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat

import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponse

sealed class ConversationItem {
    data class UserMessage(val text: String) : ConversationItem()
    data class AiResponse(
        val response: DialogueChatResponse,
        val isSaving: Boolean = false,
        val isSaved: Boolean = false
    ) : ConversationItem()
    data object Loading : ConversationItem()

}