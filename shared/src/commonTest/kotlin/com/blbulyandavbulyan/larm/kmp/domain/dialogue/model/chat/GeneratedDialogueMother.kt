package com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.chat

import kotlinx.collections.immutable.persistentListOf

object GeneratedDialogueMother {
    val FULL_DIALOGUE_1 = GeneratedDialogue(
        message = "Here is a dialogue:",
        info = DialogueTitle(
            text = "Խանութում",
            transcription = "Khanutum",
            translations = persistentListOf(ChatTranslation("In the shop", "en"))
        ),
        speakers = persistentListOf(
            DraftSpeaker("1", "Վաճառող", "Vacharogh", persistentListOf(ChatTranslation("Seller", "en"))),
            DraftSpeaker("2", "Հաճախորդ", "Hachakhord", persistentListOf(ChatTranslation("Customer", "en")))
        ),
        phrases = persistentListOf(
            DraftDialoguePhrase(
                speakerId = "1",
                phrase = DraftPhrase(
                    text = "Բարև Ձեզ",
                    isoLanguageCode = "hy",
                    transcription = "Barev Dzez",
                    translations = persistentListOf(ChatTranslation("Hello", "en"))
                )
            ),
            DraftDialoguePhrase(
                speakerId = "2",
                phrase = DraftPhrase(
                    text = "Ողջույն",
                    isoLanguageCode = "hy",
                    transcription = "Voghjuyn",
                    translations = persistentListOf(ChatTranslation("Greetings", "en"))
                )
            )
        )
    )

    val FULL_DIALOGUE_2 = GeneratedDialogue(
        message = "Another dialogue:",
        info = DialogueTitle(
            text = "Ռեստորանում",
            transcription = "Restoranum",
            translations = persistentListOf(ChatTranslation("In the restaurant", "en"))
        ),
        speakers = persistentListOf(
            DraftSpeaker("1", "Մատուցող", "Matutsogh", persistentListOf(ChatTranslation("Waiter", "en"))),
            DraftSpeaker("2", "Հաճախորդ", "Hachakhord", persistentListOf(ChatTranslation("Customer", "en")))
        ),
        phrases = persistentListOf(
            DraftDialoguePhrase(
                speakerId = "1",
                phrase = DraftPhrase(
                    text = "Ի՞նչ կպատվիրեք",
                    isoLanguageCode = "hy",
                    transcription = "Inch kpatvirek",
                    translations = persistentListOf(ChatTranslation("What will you order?", "en"))
                )
            ),
            DraftDialoguePhrase(
                speakerId = "2",
                phrase = DraftPhrase(
                    text = "Ես կցանկանայի մեկ սուրճ",
                    isoLanguageCode = "hy",
                    transcription = "Es ktsankanayi mek surch",
                    translations = persistentListOf(ChatTranslation("I would like a coffee", "en"))
                )
            )
        )
    )
}
