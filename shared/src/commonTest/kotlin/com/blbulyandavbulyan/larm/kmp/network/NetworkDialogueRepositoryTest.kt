package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponseMother
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.SearchDialoguesResponseMother
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class NetworkDialogueRepositoryTest {

    @Test
    fun `searchDialogues delegates to ApiClient correctly`() = runTest {
        val mockEngine = MockEngine { request ->
            request.url.encodedPath shouldBe "/dialogues/search"
            request.url.parameters["query"] shouldBe "test-query"
            request.method shouldBe HttpMethod.Get
            respond(
                content = Json.encodeToString(SearchDialoguesResponseMother.SEARCH_RESPONSE_1),
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

        val response = repository.searchDialogues("test-query")

        response shouldBe SearchDialoguesResponseMother.SEARCH_RESPONSE_1
    }

    @Test
    fun `getDialogue delegates to ApiClient correctly`() = runTest {
        val dialogueId = "dialogue_id_123"
        val mockEngine = MockEngine { request ->
            request.url.encodedPath shouldBe "/dialogues/$dialogueId"
            request.method shouldBe HttpMethod.Get
            respond(
                content = Json.encodeToString(GetDialogueResponseMother.FULL_DIALOGUE_1),
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

        val response = repository.getDialogue(dialogueId)
        response shouldBe GetDialogueResponseMother.FULL_DIALOGUE_1
    }
}
