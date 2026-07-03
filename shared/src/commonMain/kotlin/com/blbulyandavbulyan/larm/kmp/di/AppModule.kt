package com.blbulyandavbulyan.larm.kmp.di

import com.blbulyandavbulyan.larm.kmp.BuildKonfig
import com.blbulyandavbulyan.larm.kmp.network.ApiClient
import com.blbulyandavbulyan.larm.kmp.network.NetworkDialogueRepository
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object AppModule {
    val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
            defaultRequest {
                val baseUrl = BuildKonfig.API_URL
                if (baseUrl.isNotBlank()) {
                    url(baseUrl)
                }
            }
        }
    }

    val apiClient by lazy { ApiClient(httpClient) }
    val dialogueRepository by lazy { NetworkDialogueRepository(apiClient) }
}
