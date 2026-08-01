package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.chat.DialogueTitle
import com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.chat.GeneratedDialogue
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred

class FakeDialogueChatRepository : DialogueChatRepository {
    var shouldFail = false
    var lastPrompt = ""
    var saveCompletable: CompletableDeferred<String>? = null
    var lastSavedDialogue: GeneratedDialogue? = null
    var dialoguesToReturn = mutableListOf<GeneratedDialogue>()

    @Suppress("TooGenericExceptionThrown")
    override suspend fun generateDialogue(prompt: String, chatId: String): GeneratedDialogue {
        lastPrompt = prompt
        if (shouldFail) {
            throw Exception("Fake Network Error")
        }

        // Return mock data
        return if (dialoguesToReturn.isNotEmpty()) {
            dialoguesToReturn.removeAt(0)
        } else {
            GeneratedDialogue(
                message = "Here is your dialogue",
                info = DialogueTitle("Title", "Transcription", persistentListOf()),
                speakers = persistentListOf(),
                phrases = persistentListOf()
            )
        }
    }

    @Suppress("TooGenericExceptionThrown")
    override suspend fun saveDialogue(dialogue: GeneratedDialogue): String {
        lastSavedDialogue = dialogue
        if (shouldFail) {
            throw Exception("Fake Network Error")
        }
        saveCompletable?.await()
        return "fake-uuid-1234"
    }
}
