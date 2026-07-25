package com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.chat

import kotlinx.collections.immutable.ImmutableList

data class ChatTranslation(val translationText: String, val isoLanguageCode: String)
data class DialogueTitle(val text: String, val transcription: String, val translations: ImmutableList<ChatTranslation>)
data class DraftSpeaker(val id: String, val name: String, val transcription: String, val translations: ImmutableList<ChatTranslation>)
data class DraftPhrase(val text: String, val isoLanguageCode: String, val transcription: String, val translations: ImmutableList<ChatTranslation>)
data class DraftDialoguePhrase(val speakerId: String, val phrase: DraftPhrase)
data class GeneratedDialogue(val message: String, val info: DialogueTitle, val speakers: ImmutableList<DraftSpeaker>, val phrases: ImmutableList<DraftDialoguePhrase>)
