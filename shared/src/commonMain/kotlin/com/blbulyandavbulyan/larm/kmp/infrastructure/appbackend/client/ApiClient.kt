package com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client

import com.blbulyandavbulyan.larm.kmp.domain.asset.model.AssetData
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.ChatRequest
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.SaveDialogueRequest
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.SaveDialogueResponse
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.SearchDialoguesResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Represents the API client for Armenian Learning Assistant Backend.
 * Contains only those methods which are related to the Armenian Learning Assistant Backend.
 */
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

    suspend fun getAsset(url: String): AssetData {
        val response = client.get(url)
        val mimeType = response.headers[HttpHeaders.ContentType] ?: throw AssetHasNoContentTypeException()
        return AssetData(response.readRawBytes(), mimeType)
    }
}

class AssetHasNoContentTypeException : Exception("Asset response is missing Content-Type header")
