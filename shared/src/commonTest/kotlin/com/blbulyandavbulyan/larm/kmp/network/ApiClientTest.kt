package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.DialogueTitleResponse
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ApiClientTest {

    @Test
    fun `generateDialogue sends correct request and parses response`() = runTest {
        val mockEngine = MockEngine { request ->
            request.url.encodedPath shouldBe "/chat/dialogue"
            request.method shouldBe HttpMethod.Post
            request.body.contentType?.match(ContentType.Application.Json) shouldBe true
            respond(
                content = """{
                    "message": "Barev!",
                    "info": {
                        "title": "Hello",
                        "transcription": "Barev",
                        "translations": []
                    },
                    "speakers": [],
                    "dialoguePhrases": []
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val apiClient = ApiClient(client = mockClient)
        
        val response = apiClient.generateDialogue(message = "Hello", chatId = "123")
        
        val expectedResponse = DialogueChatResponse(
            message = "Barev!",
            info = DialogueTitleResponse("Hello", "Barev", emptyList()),
            speakers = emptyList(),
            dialoguePhrases = emptyList()
        )
        response shouldBe expectedResponse
    }
}
