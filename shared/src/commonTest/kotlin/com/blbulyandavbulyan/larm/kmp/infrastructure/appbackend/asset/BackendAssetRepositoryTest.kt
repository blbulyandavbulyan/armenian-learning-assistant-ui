package com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.asset

import com.blbulyandavbulyan.larm.kmp.domain.asset.repository.AssetFetchException
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.ApiClient
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.AssetHasNoContentTypeException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.cache.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class BackendAssetRepositoryTest {
    @Test
    fun testGetAudioBytes() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = byteArrayOf(1, 2, 3),
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.CacheControl to listOf("public, max-age=60"),
                    HttpHeaders.ContentType to listOf("audio/mpeg")
                )
            )
        }
        val mockClient = HttpClient(mockEngine) {
            install(HttpCache)
            expectSuccess = true
        }
        val apiClient = ApiClient(client = mockClient)
        val repository = BackendAssetRepository(apiClient)

        val result1 = repository.getAsset("http://example.com/audio.mp3")
        result1.data.size shouldBe 3
        result1.data[0] shouldBe 1.toByte()
        result1.mimeType shouldBe "audio/mpeg"

        val result2 = repository.getAsset("http://example.com/audio.mp3")
        result2.data.size shouldBe 3
        result2.data[0] shouldBe 1.toByte()
        result2.mimeType shouldBe "audio/mpeg"

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
        val repository = BackendAssetRepository(apiClient)

        shouldThrow<AssetFetchException> {
            repository.getAsset("http://example.com/audio.mp3")
        }
    }

    @Test
    fun testGetAssetThrowsAssetHasNoContentTypeExceptionWhenNoContentType() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = byteArrayOf(1, 2, 3),
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.CacheControl to listOf("public, max-age=60")
                ) // No Content-Type
            )
        }
        val mockClient = HttpClient(mockEngine) {
            install(HttpCache)
            expectSuccess = true
        }
        val apiClient = ApiClient(client = mockClient)
        val repository = BackendAssetRepository(apiClient)

        val exception = shouldThrow<AssetFetchException> {
            repository.getAsset("http://example.com/audio.mp3")
        }
        exception.cause.shouldBeInstanceOf<AssetHasNoContentTypeException>()
    }
}
