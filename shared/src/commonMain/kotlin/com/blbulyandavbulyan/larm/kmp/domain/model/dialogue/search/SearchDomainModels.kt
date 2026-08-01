package com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.search

import kotlinx.collections.immutable.ImmutableList

data class Asset(val contentType: String, val url: String)
data class PhraseTranslation(val id: String, val isoLanguageCode: String, val translationText: String)

data class Phrase(
    val id: String,
    val text: String,
    val isoLanguageCode: String,
    val transcription: String,
    val translations: ImmutableList<PhraseTranslation>,
    val assets: ImmutableList<Asset>
) {
    val audioAssetUrl: String? get() = assets.firstOrNull { it.contentType.startsWith("audio/") }?.url
}

data class DialogueSummary(val id: String, val title: Phrase)
data class Speaker(val id: String, val name: Phrase)
data class DialoguePhrase(val speakerId: String, val phrase: Phrase)

data class Dialogue(
    val id: String,
    val title: Phrase,
    val speakers: ImmutableList<Speaker>,
    val phrases: ImmutableList<DialoguePhrase>
)
