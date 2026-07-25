package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.*
import com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.search.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.search.PhraseTranslation as DomainPhraseTranslation

interface DialogueRepository {
    suspend fun searchDialogues(query: String): ImmutableList<DialogueSummary>
    suspend fun getDialogue(id: String): Dialogue
}

class NetworkDialogueRepository(private val apiClient: ApiClient) : DialogueRepository {
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

private fun com.blbulyandavbulyan.larm.kmp.data.dialogue.search.PhraseTranslation.toDomain(): DomainPhraseTranslation {
    return DomainPhraseTranslation(
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
