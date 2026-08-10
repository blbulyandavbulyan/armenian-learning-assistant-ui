package com.blbulyandavbulyan.larm.kmp.di

import com.blbulyandavbulyan.larm.kmp.BuildKonfig
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.asset.repository.AssetRepository
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthRepository
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.chat.DialogueChatRepository
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.search.DialogueRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.asset.BackendAssetRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.ApiClient
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.dialogue.chat.BackendDialogueChatRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.dialogue.search.BackendDialogueRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.audio.AudioPlayer
import com.blbulyandavbulyan.larm.kmp.infrastructure.audio.PlatformAudioPlayer
import com.blbulyandavbulyan.larm.kmp.infrastructure.auth.supabase.SupabaseAuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object AppModule {
    val supabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
        }
    }

    val authRepository: AuthRepository by lazy {
        SupabaseAuthRepository(supabaseClient)
    }

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
                val token = supabaseClient.auth.currentAccessTokenOrNull()
                if (!token.isNullOrBlank()) {
                    header(HttpHeaders.Authorization, "Bearer $token")
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
