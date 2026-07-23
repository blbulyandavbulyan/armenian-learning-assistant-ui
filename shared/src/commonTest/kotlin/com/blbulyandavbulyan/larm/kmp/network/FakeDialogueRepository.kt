package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.PhraseResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.SearchDialoguesResponse

open class FakeDialogueRepository : DialogueRepository {
    var shouldFail = false

    @Suppress("TooGenericExceptionThrown")
    override suspend fun searchDialogues(query: String): SearchDialoguesResponse {
        if (shouldFail) throw Exception("Fake Network Error")
        return SearchDialoguesResponse(emptyList())
    }

    @Suppress("TooGenericExceptionThrown")
    override suspend fun getDialogue(id: String): GetDialogueResponse {
        if (shouldFail) throw Exception("Fake Network Error")
        return GetDialogueResponse(
            id = id,
            title = PhraseResponse(
                id = "1",
                phrase = "Title",
                isoLanguageCode = "en",
                transcription = "Transcription",
                translations = emptyList(),
                assets = emptyList()
            ),
            speakers = emptyList(),
            dialoguePhrases = emptyList()
        )
    }
}
