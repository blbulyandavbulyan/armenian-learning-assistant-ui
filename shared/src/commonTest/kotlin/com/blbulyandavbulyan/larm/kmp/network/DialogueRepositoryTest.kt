package com.blbulyandavbulyan.larm.kmp.network

import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test

class DialogueRepositoryTest {

    @Test
    fun testSearchDialogues() = runTest {
        val mockEngine = MockEngine { request ->
            request.url.encodedPath shouldBe "/dialogues/search"
            request.url.parameters["query"] shouldBe "hello"
            respond(
                content = """{"dialogues": []}""",
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
        val repository = NetworkDialogueRepository(apiClient)

        val result = repository.searchDialogues("hello")
        result.dialogues.size shouldBe 0
    }

    @Test
    fun testGetDialogue() = runTest {
        val mockEngine = MockEngine { request ->
            request.url.encodedPath shouldBe "/dialogues/1"
            respond(
                content = """
                    {
                      "id": "1",
                      "title": {
                        "id": "1",
                        "phrase": "title",
                        "isoLanguageCode": "en",
                        "transcription": "",
                        "translations": [],
                        "assets": []
                      },
                      "speakers": [],
                      "dialoguePhrases": []
                    }
                    """.trimIndent(),
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
        val repository = NetworkDialogueRepository(apiClient)

        val result = repository.getDialogue("1")
        result.id shouldBe "1"
    }
}
