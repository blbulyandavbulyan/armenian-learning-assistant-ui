package com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.dialogue.search

import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Asset
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Dialogue
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DialoguePhrase
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DialogueSummary
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Phrase
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.PhraseTranslation
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Speaker
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.search.DialogueRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.ApiClient
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.AssetResponse
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.DialogueSummaryResponse
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.GetDialoguePhraseResponse
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.GetDialogueSpeakerResponse
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.PhraseResponse
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.search.PhraseTranslationResponse
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class BackendDialogueRepository(private val apiClient: ApiClient) : DialogueRepository {
    override suspend fun searchDialogues(query: String): ImmutableList<DialogueSummary> {
        return apiClient.searchDialogues(query).dialogues.map { it.toDomain() }.toImmutableList()
    }

    override suspend fun getDialogue(id: String): Dialogue {
        return apiClient.getDialogue(id).toDomain()
    }
}

private fun DialogueSummaryResponse.toDomain(): DialogueSummary {
    return DialogueSummary(
        id = id,
        title = title.toDomain()
    )
}

private fun PhraseResponse.toDomain(): Phrase {
    return Phrase(
        id = id,
        text = phrase,
        isoLanguageCode = isoLanguageCode,
        transcription = transcription,
        translations = translations.map { it.toDomain() }.toImmutableList(),
        assets = assets.map { it.toDomain() }.toImmutableList()
    )
}

private fun PhraseTranslationResponse.toDomain(): PhraseTranslation {
    return PhraseTranslation(
        id = id,
        isoLanguageCode = isoLanguageCode,
        translationText = translationText
    )
}

private fun AssetResponse.toDomain(): Asset {
    return Asset(
        contentType = contentType,
        url = url
    )
}

private fun GetDialogueResponse.toDomain(): Dialogue {
    return Dialogue(
        id = id,
        title = title.toDomain(),
        speakers = speakers.map { it.toDomain() }.toImmutableList(),
        phrases = dialoguePhrases.map { it.toDomain() }.toImmutableList()
    )
}

private fun GetDialogueSpeakerResponse.toDomain(): Speaker {
    return Speaker(
        id = id,
        name = name.toDomain()
    )
}

private fun GetDialoguePhraseResponse.toDomain(): DialoguePhrase {
    return DialoguePhrase(
        speakerId = speakerId,
        phrase = phrase.toDomain()
    )
}
