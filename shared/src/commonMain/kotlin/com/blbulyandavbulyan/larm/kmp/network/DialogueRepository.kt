package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.ChatTranslationResponse
import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.SaveDialoguePhraseInnerRequest
import com.blbulyandavbulyan.larm.kmp.data.SaveDialoguePhraseRequest
import com.blbulyandavbulyan.larm.kmp.data.SaveDialogueRequest
import com.blbulyandavbulyan.larm.kmp.data.SaveDialogueTitleRequest
import com.blbulyandavbulyan.larm.kmp.data.SaveDialogueTranslationRequest
import com.blbulyandavbulyan.larm.kmp.data.SaveSpeakerRequest

interface DialogueRepository {
    suspend fun generateDialogue(prompt: String, chatId: String): DialogueChatResponse
    suspend fun saveDialogue(dialogue: DialogueChatResponse): String
}

class NetworkDialogueRepository(private val apiClient: ApiClient) : DialogueRepository {
    override suspend fun generateDialogue(prompt: String, chatId: String): DialogueChatResponse {
        return apiClient.generateDialogue(prompt, chatId)
    }

    override suspend fun saveDialogue(dialogue: DialogueChatResponse): String {
        val request = SaveDialogueRequest(
            info = SaveDialogueTitleRequest(
                title = dialogue.info.title,
                transcription = dialogue.info.transcription,
                translations = dialogue.info.translations.map(::mapTranslationResponseToRequest)
            ),
            speakers = dialogue.speakers.map { speakerResponse ->
                SaveSpeakerRequest(
                    id = speakerResponse.id,
                    title = speakerResponse.title,
                    transcription = speakerResponse.transcription,
                    translations = speakerResponse.translations.map(::mapTranslationResponseToRequest)
                ) 
            },
            dialoguePhrases = dialogue.dialoguePhrases.map { dialoguePhraseResponse ->
                SaveDialoguePhraseRequest(
                    speakerId = dialoguePhraseResponse.speakerId,
                    phrase = SaveDialoguePhraseInnerRequest(
                        phrase = dialoguePhraseResponse.phrase.phrase,
                        isoLanguageCode = dialoguePhraseResponse.phrase.isoLanguageCode,
                        transcription = dialoguePhraseResponse.phrase.transcription,
                        translations = dialoguePhraseResponse.phrase.translations.map(::mapTranslationResponseToRequest)
                    )
                ) 
            }
        )
        return apiClient.saveDialogue(request)
    }

    private fun mapTranslationResponseToRequest(translationResponse: ChatTranslationResponse): SaveDialogueTranslationRequest =
        SaveDialogueTranslationRequest(
            translationText = translationResponse.translationText,
            isoLanguageCode = translationResponse.isoLanguageCode
        )
}
