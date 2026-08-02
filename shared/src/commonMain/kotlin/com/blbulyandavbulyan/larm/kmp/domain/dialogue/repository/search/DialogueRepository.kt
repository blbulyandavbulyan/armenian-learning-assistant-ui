package com.blbulyandavbulyan.larm.kmp.domain.dialogue.repository.search

import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Dialogue
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DialogueSummary
import kotlinx.collections.immutable.ImmutableList

interface DialogueRepository {
    suspend fun searchDialogues(query: String): ImmutableList<DialogueSummary>
    suspend fun getDialogue(id: String): Dialogue
}
