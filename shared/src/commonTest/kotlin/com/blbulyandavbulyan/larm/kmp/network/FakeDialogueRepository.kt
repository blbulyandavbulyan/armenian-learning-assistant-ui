package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Dialogue
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DialogueSummary
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Phrase
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.search.DialogueRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class FakeDialogueRepository : DialogueRepository {
    var shouldFail = false

    @Suppress("TooGenericExceptionThrown")
    override suspend fun searchDialogues(query: String): ImmutableList<DialogueSummary> {
        if (shouldFail) throw Exception("Fake Network Error")
        return persistentListOf()
    }

    @Suppress("TooGenericExceptionThrown")
    override suspend fun getDialogue(id: String): Dialogue {
        if (shouldFail) throw Exception("Fake Network Error")
        return Dialogue(
            id = id,
            title = Phrase(
                id = "1",
                text = "Title",
                isoLanguageCode = "en",
                transcription = "Transcription",
                translations = persistentListOf(),
                assets = persistentListOf()
            ),
            speakers = persistentListOf(),
            phrases = persistentListOf()
        )
    }
}
