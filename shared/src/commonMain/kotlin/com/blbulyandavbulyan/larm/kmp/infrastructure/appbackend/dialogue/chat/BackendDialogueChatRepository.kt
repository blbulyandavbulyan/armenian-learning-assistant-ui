package com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.dialogue.chat

import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.ChatTranslation
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.DialogueTitle
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.DraftDialoguePhrase
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.DraftPhrase
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.DraftSpeaker
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.GeneratedDialogue
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.chat.DialogueChatRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.ApiClient
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.ChatTranslationResponse
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.SaveDialoguePhraseInnerRequest
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.SaveDialoguePhraseRequest
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.SaveDialogueRequest
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.SaveDialogueTitleRequest
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.SaveDialogueTranslationRequest
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat.SaveSpeakerRequest
import kotlinx.collections.immutable.toImmutableList

class BackendDialogueChatRepository(private val apiClient: ApiClient) : DialogueChatRepository {
    override suspend fun generateDialogue(prompt: String, chatId: String): GeneratedDialogue {
        return apiClient.generateDialogue(prompt, chatId).toDomain()
    }

    override suspend fun saveDialogue(dialogue: GeneratedDialogue): String {
        return apiClient.saveDialogue(dialogue.toRequest())
    }

    private fun GeneratedDialogue.toRequest(): SaveDialogueRequest = SaveDialogueRequest(
        info = SaveDialogueTitleRequest(
            title = info.text,
            transcription = info.transcription,
            translations = info.translations.map { it.toRequest() }
        ),
        speakers = speakers.map { speaker ->
            SaveSpeakerRequest(
                id = speaker.id,
                title = speaker.name,
                transcription = speaker.transcription,
                translations = speaker.translations.map { it.toRequest() }
            )
        },
        dialoguePhrases = phrases.map { dialoguePhrase ->
            SaveDialoguePhraseRequest(
                speakerId = dialoguePhrase.speakerId,
                phrase = SaveDialoguePhraseInnerRequest(
                    phrase = dialoguePhrase.phrase.text,
                    isoLanguageCode = dialoguePhrase.phrase.isoLanguageCode,
                    transcription = dialoguePhrase.phrase.transcription,
                    translations = dialoguePhrase.phrase.translations.map { it.toRequest() }
                )
            )
        }
    )

    private fun ChatTranslation.toRequest(): SaveDialogueTranslationRequest =
        SaveDialogueTranslationRequest(
            translationText = translationText,
            isoLanguageCode = isoLanguageCode
        )

    private fun ChatTranslationResponse.toDomain(): ChatTranslation =
        ChatTranslation(
            translationText = translationText,
            isoLanguageCode = isoLanguageCode
        )

    private fun DialogueChatResponse.toDomain(): GeneratedDialogue {
        return GeneratedDialogue(
            message = this.message,
            info = DialogueTitle(
                text = this.info.title,
                transcription = this.info.transcription,
                translations = this.info.translations.map { it.toDomain() }.toImmutableList()
            ),
            speakers = this.speakers.map { speaker ->
                DraftSpeaker(
                    id = speaker.id,
                    name = speaker.title,
                    transcription = speaker.transcription,
                    translations = speaker.translations.map { it.toDomain() }.toImmutableList()
                )
            }.toImmutableList(),
            phrases = this.dialoguePhrases.map { phrase ->
                DraftDialoguePhrase(
                    speakerId = phrase.speakerId,
                    phrase = DraftPhrase(
                        text = phrase.phrase.phrase,
                        isoLanguageCode = phrase.phrase.isoLanguageCode,
                        transcription = phrase.phrase.transcription,
                        translations = phrase.phrase.translations.map { it.toDomain() }.toImmutableList()
                    )
                )
            }.toImmutableList()
        )
    }
}
