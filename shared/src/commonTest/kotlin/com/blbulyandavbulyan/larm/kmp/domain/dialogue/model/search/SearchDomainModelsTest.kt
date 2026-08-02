package com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test

class SearchDomainModelsTest {

    @Test
    fun audioAssetUrl_returns_first_audio_asset_url() {
        val asset1 = Asset(contentType = "image/png", url = "http://example.com/image.png")
        val asset2 = Asset(contentType = "audio/mpeg", url = "http://example.com/audio1.mp3")
        val asset3 = Asset(contentType = "audio/wav", url = "http://example.com/audio2.wav")

        val phrase = Phrase(
            id = "p1",
            text = "Barev",
            isoLanguageCode = "hy",
            transcription = "Barev",
            translations = persistentListOf(),
            assets = persistentListOf(asset1, asset2, asset3)
        )

        phrase.audioAssetUrl shouldBe "http://example.com/audio1.mp3"
    }

    @Test
    fun audioAssetUrl_returns_null_when_no_audio_asset_present() {
        val asset1 = Asset(contentType = "image/png", url = "http://example.com/image.png")

        val phrase = Phrase(
            id = "p2",
            text = "Barev",
            isoLanguageCode = "hy",
            transcription = "Barev",
            translations = persistentListOf(),
            assets = persistentListOf(asset1)
        )

        phrase.audioAssetUrl shouldBe null
    }
}
