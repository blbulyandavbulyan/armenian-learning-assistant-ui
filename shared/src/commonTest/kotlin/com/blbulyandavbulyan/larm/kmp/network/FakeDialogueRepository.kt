package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueTitleResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.PhraseResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.SearchDialoguesResponse
import kotlinx.coroutines.CompletableDeferred

// 1. Create a Fake implementation of the Repository for testing
open class FakeDialogueRepository : DialogueRepository {
    var shouldFail = false
    var lastPrompt = ""
    var saveCompletable: CompletableDeferred<String>? = null
    var lastSavedDialogue: DialogueChatResponse? = null
    var dialoguesToReturn = mutableListOf<DialogueChatResponse>()

    @Suppress("TooGenericExceptionThrown")
    override suspend fun generateDialogue(prompt: String, chatId: String): DialogueChatResponse {
        lastPrompt = prompt
        if (shouldFail) {
            throw Exception("Fake Network Error")
        }

        // Return mock data
        return if (dialoguesToReturn.isNotEmpty()) {
            dialoguesToReturn.removeAt(0)
        } else {
            DialogueChatResponse(
                message = "Here is your dialogue",
                info = DialogueTitleResponse("Title", "Transcription", emptyList()),
                speakers = emptyList(),
                dialoguePhrases = emptyList()
            )
        }
    }

    @Suppress("TooGenericExceptionThrown")
    override suspend fun saveDialogue(dialogue: DialogueChatResponse): String {
        lastSavedDialogue = dialogue
        if (shouldFail) {
            throw Exception("Fake Network Error")
        }
        saveCompletable?.await()
        return "fake-uuid-1234"
    }

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
