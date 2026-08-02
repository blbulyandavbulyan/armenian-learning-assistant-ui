package com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_generate_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_save_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.error_unknown
import com.blbulyandavbulyan.larm.kmp.core.UiText
import com.blbulyandavbulyan.larm.kmp.core.error.GlobalErrorManager
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat.GeneratedDialogue
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.chat.DialogueChatRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class DialogueChatViewModel(
    private val repository: DialogueChatRepository,
    private val globalErrorManager: GlobalErrorManager
) : ViewModel() {
    private val _conversation = MutableStateFlow<PersistentList<ConversationItem>>(persistentListOf())
    val conversation: StateFlow<ImmutableList<ConversationItem>> = _conversation.asStateFlow()

    private val chatId = Uuid.random().toString()

    @Suppress("TooGenericExceptionCaught")
    fun generateDialogue(prompt: String) {
        if (prompt.isBlank()) return

        _conversation.update { current ->
            current.adding(ConversationItem.UserMessage(prompt))
                .adding(ConversationItem.Loading)
        }

        viewModelScope.launch {
            try {
                val response = repository.generateDialogue(prompt, chatId)
                _conversation.update { current ->
                    current.removingAll { it is ConversationItem.Loading }
                        .adding(ConversationItem.AiResponse(response))
                }
            } catch (e: Throwable) {
                _conversation.update { current ->
                    current.removingAll { it is ConversationItem.Loading }
                }
                println(e)
                globalErrorManager.showError(
                    UiText.from(Res.string.error_failed_to_generate_dialogue),
                    UiText.from(e.message, Res.string.error_unknown)
                )
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun saveDialogue(dialogue: GeneratedDialogue) {
        changeConversationState(dialogue) {
            it.copy(isSaving = true)
        }

        viewModelScope.launch {
            try {
                repository.saveDialogue(dialogue)
                changeConversationState(dialogue) {
                    it.copy(isSaving = false, isSaved = true)
                }
            } catch (e: Throwable) {
                changeConversationState(dialogue) {
                    it.copy(isSaving = false)
                }
                println(e)
                globalErrorManager.showError(
                    UiText.from(Res.string.error_failed_to_save_dialogue),
                    UiText.from(e.message, Res.string.error_unknown)
                )
            }
        }
    }

    private fun changeConversationState(
        dialogue: GeneratedDialogue,
        newDialogueConversationItem: (ConversationItem.AiResponse) -> ConversationItem.AiResponse
    ) {
        _conversation.update { current ->
            current.map {
                if (it is ConversationItem.AiResponse && it.response === dialogue) {
                    newDialogueConversationItem(it)
                } else {
                    it
                }
            }.toPersistentList()
        }
    }
}
