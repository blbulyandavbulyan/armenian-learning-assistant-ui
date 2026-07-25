package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueTitleResponse
import kotlinx.coroutines.CompletableDeferred

class FakeDialogueChatRepository : DialogueChatRepository {
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
}
