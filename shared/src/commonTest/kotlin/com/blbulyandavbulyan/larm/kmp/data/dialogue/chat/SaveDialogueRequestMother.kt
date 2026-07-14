package com.blbulyandavbulyan.larm.kmp.data.dialogue.chat

object SaveDialogueRequestMother {
    val FULL_REQUEST_1 = SaveDialogueRequest(
        info = SaveDialogueTitleRequest(
            title = "Խանութում",
            transcription = "Khanutum",
            translations = listOf(SaveDialogueTranslationRequest("In the shop", "en"))
        ),
        speakers = listOf(
            SaveSpeakerRequest(
                id = "1",
                title = "Վաճառող",
                transcription = "Vacharogh",
                translations = listOf(SaveDialogueTranslationRequest("Seller", "en"))
            ),
            SaveSpeakerRequest(
                id = "2",
                title = "Հաճախորդ",
                transcription = "Hachakhord",
                translations = listOf(SaveDialogueTranslationRequest("Customer", "en"))
            )
        ),
        dialoguePhrases = listOf(
            SaveDialoguePhraseRequest(
                speakerId = "1",
                phrase = SaveDialoguePhraseInnerRequest(
                    phrase = "Բարև Ձեզ",
                    isoLanguageCode = "hy",
                    transcription = "Barev Dzez",
                    translations = listOf(SaveDialogueTranslationRequest("Hello", "en"))
                )
            ),
            SaveDialoguePhraseRequest(
                speakerId = "2",
                phrase = SaveDialoguePhraseInnerRequest(
                    phrase = "Ողջույն",
                    isoLanguageCode = "hy",
                    transcription = "Voghjuyn",
                    translations = listOf(SaveDialogueTranslationRequest("Greetings", "en"))
                )
            )
        )
    )
}
