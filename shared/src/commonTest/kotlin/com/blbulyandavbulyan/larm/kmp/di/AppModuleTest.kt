package com.blbulyandavbulyan.larm.kmp.di

import io.kotest.matchers.nulls.shouldNotBeNull
import kotlin.test.Test

class AppModuleTest {

    @Test
    fun `should instantiate all dependencies without crashing`() {
        AppModule.supabaseClient.shouldNotBeNull()
        AppModule.authRepository.shouldNotBeNull()
        AppModule.httpClient.shouldNotBeNull()
        AppModule.apiClient.shouldNotBeNull()
        AppModule.dialogueRepository.shouldNotBeNull()
        AppModule.dialogueChatRepository.shouldNotBeNull()
        AppModule.assetRepository.shouldNotBeNull()
        AppModule.audioPlayer.shouldNotBeNull()
        AppModule.globalErrorManager.shouldNotBeNull()
    }
}
