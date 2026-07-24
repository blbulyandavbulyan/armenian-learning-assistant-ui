package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.ChatRequest
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.SaveDialogueRequest
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.SaveDialogueResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.SearchDialoguesResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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

    suspend fun searchDialogues(query: String): SearchDialoguesResponse {
        val response = client.get("/dialogues/search") {
            parameter("query", query)
        }
        return response.body()
    }

    suspend fun getDialogue(id: String): GetDialogueResponse {
        val response = client.get("/dialogues/$id")
        return response.body()
    }

    suspend fun getAssetBytes(url: String): ByteArray {
        val response = client.get(url)
        return response.readRawBytes()
    }
}
