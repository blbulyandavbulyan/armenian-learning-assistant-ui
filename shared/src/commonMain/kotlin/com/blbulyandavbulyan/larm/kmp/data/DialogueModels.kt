package com.blbulyandavbulyan.larm.kmp.data

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String,
    val chatId: String
)

@Serializable
data class ChatTranslationResponse(
    val translationText: String,
    val isoLanguageCode: String
)

@Serializable
data class DialogueTitleResponse(
    val title: String,
    val transcription: String,
    val translations: List<ChatTranslationResponse>
)

@Serializable
data class SpeakerResponse(
    val id: String,
    val title: String,
    val transcription: String,
    val translations: List<ChatTranslationResponse>
)

@Serializable
data class DraftPhrasesResponse(
    val phrase: String,
    val isoLanguageCode: String,
    val transcription: String,
    val translations: List<ChatTranslationResponse>
)

@Serializable
data class DialoguePhraseResponse(
    val speakerId: String,
    val phrase: DraftPhrasesResponse
)

@Serializable
data class DialogueChatResponse(
    val message: String,
    val info: DialogueTitleResponse,
    val speakers: List<SpeakerResponse>,
    val dialoguePhrases: List<DialoguePhraseResponse>
)

@Serializable
data class SaveDialogueRequest(
    val info: SaveDialogueTitleRequest,
    val speakers: List<SaveSpeakerRequest>,
    val dialoguePhrases: List<SaveDialoguePhraseRequest>
)

@Serializable
data class SaveDialogueTitleRequest(
    val title: String,
    val transcription: String,
    val translations: List<SaveDialogueTranslationRequest>
)

@Serializable
data class SaveDialogueTranslationRequest(
    val translationText: String,
    val isoLanguageCode: String
)

@Serializable
data class SaveSpeakerRequest(
    val id: String,
    val title: String,
    val transcription: String,
    val translations: List<SaveDialogueTranslationRequest>
)

@Serializable
data class SaveDialoguePhraseRequest(
    val speakerId: String,
    val phrase: SaveDialoguePhraseInnerRequest
)

@Serializable
data class SaveDialoguePhraseInnerRequest(
    val phrase: String,
    val isoLanguageCode: String,
    val transcription: String,
    val translations: List<SaveDialogueTranslationRequest>
)

@Serializable
data class SaveDialogueResponse(
    val id: String
)
