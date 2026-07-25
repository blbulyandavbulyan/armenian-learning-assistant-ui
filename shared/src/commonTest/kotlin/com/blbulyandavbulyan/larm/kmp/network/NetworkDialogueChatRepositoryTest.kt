package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponseMother
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.SaveDialogueRequestMother
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.test.Test

class NetworkDialogueChatRepositoryTest {
    @Test
    fun `generateDialogue delegates to ApiClient correctly`() = runTest {
        val mockEngine = MockEngine { request ->
            request.url.encodedPath shouldBe "/chat/dialogue"
            request.method shouldBe HttpMethod.Post
            respond(
                content = Json.encodeToString(DialogueChatResponseMother.FULL_DIALOGUE_1),
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
        val repository = NetworkDialogueChatRepository(apiClient)

        val response = repository.generateDialogue(prompt = "Test prompt", chatId = "chat456")

        response shouldBe DialogueChatResponseMother.FULL_DIALOGUE_1
    }

    @Test
    fun `saveDialogue delegates to ApiClient correctly`() = runTest {
        val mockEngine = MockEngine { request ->
            request.url.encodedPath shouldBe "/dialogues"
            request.method shouldBe HttpMethod.Post

            val bodyBytes = request.body.toByteArray()
            val bodyText = bodyBytes.decodeToString()

            val expectedJson = Json.encodeToJsonElement(SaveDialogueRequestMother.FULL_REQUEST_1)
            val actualJson = Json.parseToJsonElement(bodyText)
            actualJson shouldBe expectedJson

            respond(
                content = """{"id": "fake-uuid-1234"}""",
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
        val repository = NetworkDialogueChatRepository(apiClient)
        val response = repository.saveDialogue(DialogueChatResponseMother.FULL_DIALOGUE_1)
        response shouldBe "fake-uuid-1234"
    }
}
