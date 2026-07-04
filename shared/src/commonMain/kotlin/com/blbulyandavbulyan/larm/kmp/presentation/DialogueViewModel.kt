package com.blbulyandavbulyan.larm.kmp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.network.DialogueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.getString
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_save
import armenianlearningassistant_kmp.shared.generated.resources.error_unknown

sealed class ConversationItem {
    data class UserMessage(val text: String) : ConversationItem()
    data class AiResponse(
        val response: DialogueChatResponse,
        val isSaving: Boolean = false,
        val isSaved: Boolean = false
    ) : ConversationItem()
    data class Error(val message: String) : ConversationItem()
    data object Loading : ConversationItem()
}

@OptIn(ExperimentalUuidApi::class)
class DialogueViewModel(private val repository: DialogueRepository) : ViewModel() {
    private val _conversation = MutableStateFlow<List<ConversationItem>>(emptyList())
    val conversation: StateFlow<List<ConversationItem>> = _conversation.asStateFlow()

    private val chatId = Uuid.random().toString()

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
            } catch (e: Exception) {
                val newConv = _conversation.value.filter { it !is ConversationItem.Loading }.toMutableList()
                newConv.add(ConversationItem.Error(e.message ?: getString(Res.string.error_unknown)))
                _conversation.value = newConv
            }
        }
    }

    fun saveDialogue(dialogue: DialogueChatResponse) {
        _conversation.value = _conversation.value.map {
            if (it is ConversationItem.AiResponse && it.response === dialogue) {
                it.copy(isSaving = true)
            } else it
        }

        viewModelScope.launch {
            try {
                repository.saveDialogue(dialogue)
                _conversation.value = _conversation.value.map {
                    if (it is ConversationItem.AiResponse && it.response === dialogue) {
                        it.copy(isSaving = false, isSaved = true)
                    } else it
                }
            } catch (e: Exception) {
                val currentConv = _conversation.value.map {
                    if (it is ConversationItem.AiResponse && it.response === dialogue) {
                        it.copy(isSaving = false)
                    } else it
                }.toMutableList()
                currentConv.add(ConversationItem.Error(e.message ?: getString(Res.string.error_failed_to_save)))
                _conversation.value = currentConv
            }
        }
    }
}
