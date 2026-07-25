package com.blbulyandavbulyan.larm.kmp.di

import io.kotest.matchers.nulls.shouldNotBeNull
import kotlin.test.Test

class AppModuleTest {

    @Test
    fun `should instantiate all dependencies without crashing`() {
        AppModule.httpClient.shouldNotBeNull()
        AppModule.apiClient.shouldNotBeNull()
        AppModule.dialogueRepository.shouldNotBeNull()
        AppModule.dialogueChatRepository.shouldNotBeNull()
        AppModule.audioRepository.shouldNotBeNull()
        AppModule.globalErrorManager.shouldNotBeNull()
    }
}
