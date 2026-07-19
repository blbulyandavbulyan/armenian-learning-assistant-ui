package com.blbulyandavbulyan.larm.kmp.network

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.cache.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AssetRepositoryTest {
    @Test
    fun testGetAudioBytes() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = byteArrayOf(1, 2, 3),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.CacheControl, "public, max-age=60")
            )
        }
        val mockClient = HttpClient(mockEngine) {
            install(HttpCache)
            expectSuccess = true
        }
        val apiClient = ApiClient(client = mockClient)
        val repository = NetworkAssetRepository(apiClient)

        val result1 = repository.getAssetBytes("http://example.com/audio.mp3")
        result1.size shouldBe 3
        result1[0] shouldBe 1.toByte()

        val result2 = repository.getAssetBytes("http://example.com/audio.mp3")
        result2.size shouldBe 3
        result2[0] shouldBe 1.toByte()

        mockEngine.requestHistory.size shouldBe 1
    }

    @Test
    fun testGetAudioBytesThrowsAudioFetchExceptionOn500() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }
        val mockClient = HttpClient(mockEngine) {
            install(HttpCache)
            expectSuccess = true
        }
        val apiClient = ApiClient(client = mockClient)
        val repository = NetworkAssetRepository(apiClient)

        shouldThrow<AssetFetchException> {
            repository.getAssetBytes("http://example.com/audio.mp3")
        }
    }
}
