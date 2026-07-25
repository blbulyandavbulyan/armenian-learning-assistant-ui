package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.SearchDialoguesResponse

interface DialogueRepository {
    suspend fun searchDialogues(query: String): SearchDialoguesResponse
    suspend fun getDialogue(id: String): GetDialogueResponse
}

class NetworkDialogueRepository(private val apiClient: ApiClient) : DialogueRepository {

    override suspend fun searchDialogues(query: String): SearchDialoguesResponse {
        return apiClient.searchDialogues(query)
    }

    override suspend fun getDialogue(id: String): GetDialogueResponse {
        return apiClient.getDialogue(id)
    }
}
