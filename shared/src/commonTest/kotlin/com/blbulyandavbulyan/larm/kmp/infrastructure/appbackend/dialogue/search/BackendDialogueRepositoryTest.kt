package com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.dialogue.search

import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DomainMothers
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.ApiClient
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.GetDialogueResponseMother
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.SearchDialoguesResponseMother
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test

class BackendDialogueRepositoryTest {

    @Test
    fun `searchDialogues delegates to ApiClient correctly`() = runTest {
        val mockEngine = MockEngine { request ->
            request.url.encodedPath shouldBe "/dialogues/search"
            request.url.parameters["query"] shouldBe "test-query"
            request.method shouldBe HttpMethod.Get
            respond(
                content = SearchDialoguesResponseMother.SearchResponse1.JSON_RESPONSE,
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
        val repository = BackendDialogueRepository(apiClient)
        val response = repository.searchDialogues("test-query")

        response shouldBe listOf(
            DomainMothers.DIALOGUE_SUMMARY_1,
            DomainMothers.DIALOGUE_SUMMARY_2
        )
    }

    @Test
    fun `searchDialogues delegates to ApiClient correctly for empty results`() = runTest {
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
        val repository = BackendDialogueRepository(apiClient)

        val result = repository.searchDialogues("hello")
        result.size shouldBe 0
    }

    @Test
    fun `getDialogue delegates to ApiClient correctly`() = runTest {
        val dialogueId = "dialogue_id_123"

        val mockEngine = MockEngine { request ->
            request.url.encodedPath shouldBe "/dialogues/$dialogueId"
            request.method shouldBe HttpMethod.Get
            respond(
                content = GetDialogueResponseMother.Dialogue1.JSON_RESPONSE,
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
        val repository = BackendDialogueRepository(apiClient)

        val response = repository.getDialogue(dialogueId)
        response shouldBe DomainMothers.DIALOGUE_1
    }
}
