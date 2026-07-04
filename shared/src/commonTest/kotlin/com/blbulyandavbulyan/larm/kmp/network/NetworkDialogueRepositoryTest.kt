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

class NetworkDialogueRepositoryTest {

    @Test
    fun `generateDialogue delegates to ApiClient correctly`() = runTest {
        val mockEngine = MockEngine { request ->
            request.url.encodedPath shouldBe "/chat/dialogue"
            request.method shouldBe HttpMethod.Post
            // TODO this test has to be imporved, so that speakers and dialoguePhrases, translations are non empty list
            //  (including nested 'translations'
            respond(
                content = """{
                    "message": "Mocked response",
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
        val repository = NetworkDialogueRepository(apiClient)
        
        val response = repository.generateDialogue(prompt = "Test prompt", chatId = "chat456")
        
        val expectedResponse = DialogueChatResponse(
            message = "Mocked response",
            info = DialogueTitleResponse("Hello", "Barev", emptyList()),
            speakers = emptyList(),
            dialoguePhrases = emptyList()
        )
        response shouldBe expectedResponse
    }

    // TODO where is the test for saveDialogue, should look the same as test above
    //  (assuming that you fixed todo comment above before implementing new test for new method)
    //  you can even reuse the 'DialogueChatResponse' which is 'expected there' as 'input' for this test,
    //  you can create DialogueChatResponseMother interface or whatever fits better for the 'object mother pattern in kotlin'
    //  in the same package as the DialogueChatResponse
    //  and declare constnat there with the DialogueChatResponse defined in the previvious test
    //  actually you may reuse DialogueChatResponse in these tests from com.blbulyandavbulyan.larm.kmp.ui.DialogueGeneratorScreenTest.aiResponse_displaysFullDialogueDataCorrectly
    //  and move that to the mother
}
