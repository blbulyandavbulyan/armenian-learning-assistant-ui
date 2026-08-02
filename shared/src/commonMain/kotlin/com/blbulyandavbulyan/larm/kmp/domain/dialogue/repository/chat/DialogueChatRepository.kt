package com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.chat

import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.GeneratedDialogue

interface DialogueChatRepository {
    suspend fun generateDialogue(prompt: String, chatId: String): GeneratedDialogue
    suspend fun saveDialogue(dialogue: GeneratedDialogue): String
}
