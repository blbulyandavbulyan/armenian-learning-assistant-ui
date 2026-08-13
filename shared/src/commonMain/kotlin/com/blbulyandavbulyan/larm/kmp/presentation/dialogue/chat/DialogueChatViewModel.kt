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

        val loadingItem = ConversationItem.Loading()
        _conversation.update { current ->
            current.adding(ConversationItem.UserMessage(prompt))
                .adding(loadingItem)
        }

        executeDialogueGeneration(prompt, loadingItem.id)
    }

    fun retryDialogue(id: String) {
        val errorItem = _conversation.value.filterIsInstance<ConversationItem.Error>().find { it.id == id } ?: return
        val loadingItem = ConversationItem.Loading(id = errorItem.id)

        _conversation.update { current ->
            current.map {
                if (it is ConversationItem.Error && it.id == id) {
                    loadingItem
                } else it
            }.toPersistentList()
        }

        executeDialogueGeneration(errorItem.failedPrompt, loadingItem.id, errorItem)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun executeDialogueGeneration(
        prompt: String,
        loadingId: String,
        fallbackError: ConversationItem.Error? = null
    ) {
        viewModelScope.launch {
            try {
                val response = repository.generateDialogue(prompt, chatId)
                _conversation.update { current ->
                    current.map {
                        if (it is ConversationItem.Loading && it.id == loadingId) {
                            ConversationItem.AiResponse(response)
                        } else it
                    }.toPersistentList()
                }
            } catch (e: Throwable) {
                val errorItem = fallbackError ?: ConversationItem.Error(prompt, loadingId)
                _conversation.update { current ->
                    current.map {
                        if (it is ConversationItem.Loading && it.id == loadingId) {
                            errorItem
                        } else it
                    }.toPersistentList()
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
