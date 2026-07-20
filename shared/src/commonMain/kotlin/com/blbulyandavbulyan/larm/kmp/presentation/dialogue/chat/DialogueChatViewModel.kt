package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_generate_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_save_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.error_unknown
import com.blbulyandavbulyan.larm.kmp.core.UiText
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.network.DialogueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class DialogueChatViewModel(
    private val repository: DialogueRepository,
    private val globalErrorManager: GlobalErrorManager
) : ViewModel() {
    private val _conversation = MutableStateFlow<List<ConversationItem>>(emptyList())
    val conversation: StateFlow<List<ConversationItem>> = _conversation.asStateFlow()

    private val chatId = Uuid.random().toString()

    @Suppress("TooGenericExceptionCaught")
    fun generateDialogue(prompt: String) {
        if (prompt.isBlank()) return

        val current = _conversation.value.toMutableList()
        current.add(ConversationItem.UserMessage(prompt))
        current.add(ConversationItem.Loading)
        _conversation.value = current

        viewModelScope.launch {
            try {
                val response = repository.generateDialogue(prompt, chatId)
                val newConv = _conversation.value.filter { it !is ConversationItem.Loading }.toMutableList()
                newConv.add(ConversationItem.AiResponse(response))
                _conversation.value = newConv
            } catch (e: Throwable) {
                val newConv = _conversation.value.filter { it !is ConversationItem.Loading }.toMutableList()
                _conversation.value = newConv
                println(e)
                globalErrorManager.showError(
                    UiText.from(Res.string.error_failed_to_generate_dialogue),
                    UiText.from(e.message, Res.string.error_unknown)
                )
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun saveDialogue(dialogue: DialogueChatResponse) {
        _conversation.value = _conversation.value.map {
            if (it is ConversationItem.AiResponse && it.response === dialogue) {
                it.copy(isSaving = true)
            } else {
                it
            }
        }

        viewModelScope.launch {
            try {
                repository.saveDialogue(dialogue)
                _conversation.value = _conversation.value.map {
                    if (it is ConversationItem.AiResponse && it.response === dialogue) {
                        it.copy(isSaving = false, isSaved = true)
                    } else {
                        it
                    }
                }
            } catch (e: Throwable) {
                val currentConv = _conversation.value.map {
                    if (it is ConversationItem.AiResponse && it.response === dialogue) {
                        it.copy(isSaving = false)
                    } else {
                        it
                    }
                }.toMutableList()
                _conversation.value = currentConv
                println(e)
                globalErrorManager.showError(
                    UiText.from(Res.string.error_failed_to_save_dialogue),
                    UiText.from(e.message, Res.string.error_unknown)
                )
            }
        }
    }
}
