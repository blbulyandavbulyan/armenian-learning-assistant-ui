package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.ChatRequest
import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import io.ktor.client.plugins.defaultRequest

class ApiClient(private val baseUrl: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        defaultRequest {
            if (baseUrl.isNotBlank()) {
                url(baseUrl)
            }
        }
    }

    suspend fun generateDialogue(message: String, chatId: String): DialogueChatResponse {
        val requestBody = ChatRequest(message, chatId)
        val response = client.post("/chat/dialogue") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        return response.body()
    }
}
