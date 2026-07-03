package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse

interface DialogueRepository {
    suspend fun generateDialogue(prompt: String, chatId: String): DialogueChatResponse
}

class NetworkDialogueRepository(private val apiClient: ApiClient) : DialogueRepository {
    override suspend fun generateDialogue(prompt: String, chatId: String): DialogueChatResponse {
        return apiClient.generateDialogue(prompt, chatId)
    }
}
