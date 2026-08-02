package com.blbulyandavbulyan.larm.kmp.di

import com.blbulyandavbulyan.larm.kmp.BuildKonfig
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.asset.repository.AssetRepository
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.chat.DialogueChatRepository
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.search.DialogueRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.asset.BackendAssetRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.ApiClient
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.dialogue.chat.BackendDialogueChatRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.dialogue.search.BackendDialogueRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.audio.AudioPlayer
import com.blbulyandavbulyan.larm.kmp.infrastructure.audio.PlatformAudioPlayer
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object AppModule {
    val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    }
                )
            }
            install(HttpCache)
            defaultRequest {
                val baseUrl = BuildKonfig.API_URL
                if (baseUrl.isNotBlank()) {
                    url(baseUrl)
                }
            }
        }
    }

    val apiClient by lazy { ApiClient(httpClient) }
    val dialogueRepository: DialogueRepository by lazy { BackendDialogueRepository(apiClient) }
    val dialogueChatRepository: DialogueChatRepository by lazy { BackendDialogueChatRepository(apiClient) }
    val assetRepository: AssetRepository by lazy { BackendAssetRepository(apiClient) }
    val audioPlayer: AudioPlayer by lazy { PlatformAudioPlayer() }
    val globalErrorManager by lazy { GlobalErrorManager() }
}
