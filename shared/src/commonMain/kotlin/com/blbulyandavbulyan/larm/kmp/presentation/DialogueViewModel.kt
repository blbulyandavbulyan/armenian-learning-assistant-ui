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

sealed class ConversationItem {
    data class UserMessage(val text: String) : ConversationItem()
    data class AiResponse(val response: DialogueChatResponse) : ConversationItem()
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
                newConv.add(ConversationItem.Error(e.message ?: "Unknown error"))
                _conversation.value = newConv
            }
        }
    }
}
