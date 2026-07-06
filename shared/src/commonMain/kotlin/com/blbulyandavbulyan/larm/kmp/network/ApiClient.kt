package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.ChatRequest
import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.SaveDialogueRequest
import com.blbulyandavbulyan.larm.kmp.data.SaveDialogueResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiClient(private val client: HttpClient) {

    suspend fun generateDialogue(message: String, chatId: String): DialogueChatResponse {
        val requestBody = ChatRequest(message, chatId)
        val response = client.post("/chat/dialogue") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        return response.body()
    }

    suspend fun saveDialogue(request: SaveDialogueRequest): String {
        val response = client.post("/dialogues") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val saveResponse: SaveDialogueResponse = response.body()
        return saveResponse.id
    }
}

