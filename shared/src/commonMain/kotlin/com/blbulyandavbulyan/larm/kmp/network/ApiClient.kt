package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.ChatRequest
import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
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
}
