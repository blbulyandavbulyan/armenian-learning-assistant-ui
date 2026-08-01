package com.blbulyandavbulyan.larm.kmp.data.dialogue.search

import kotlinx.serialization.Serializable

@Serializable
data class AssetResponse(val contentType: String, val url: String)

@Serializable
data class PhraseTranslationResponse(val id: String, val isoLanguageCode: String, val translationText: String)

@Serializable
data class PhraseResponse(
    val id: String,
    val phrase: String,
    val isoLanguageCode: String,
    val transcription: String,
    val translations: List<PhraseTranslationResponse>,
    val assets: List<AssetResponse>
)

@Serializable
data class DialogueSummaryResponse(val id: String, val title: PhraseResponse)

@Serializable
data class SearchDialoguesResponse(val dialogues: List<DialogueSummaryResponse>)

@Serializable
data class GetDialogueSpeakerResponse(val id: String, val name: PhraseResponse)

@Serializable
data class GetDialoguePhraseResponse(val speakerId: String, val phrase: PhraseResponse)

@Serializable
data class GetDialogueResponse(
    val id: String,
    val title: PhraseResponse,
    val speakers: List<GetDialogueSpeakerResponse>,
    val dialoguePhrases: List<GetDialoguePhraseResponse>
)
