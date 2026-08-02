package com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.data.dialogue.chat

object DialogueChatResponseMother {
    val FULL_DIALOGUE_1 = DialogueChatResponse(
        message = "Here is a dialogue:",
        info = DialogueTitleResponse(
            title = "Խանութում",
            transcription = "Khanutum",
            translations = listOf(ChatTranslationResponse("In the shop", "en"))
        ),
        speakers = listOf(
            SpeakerResponse("1", "Վաճառող", "Vacharogh", listOf(ChatTranslationResponse("Seller", "en"))),
            SpeakerResponse("2", "Հաճախորդ", "Hachakhord", listOf(ChatTranslationResponse("Customer", "en")))
        ),
        dialoguePhrases = listOf(
            DialoguePhraseResponse(
                speakerId = "1",
                phrase = DraftPhrasesResponse(
                    phrase = "Բարև Ձեզ",
                    isoLanguageCode = "hy",
                    transcription = "Barev Dzez",
                    translations = listOf(ChatTranslationResponse("Hello", "en"))
                )
            ),
            DialoguePhraseResponse(
                speakerId = "2",
                phrase = DraftPhrasesResponse(
                    phrase = "Ողջույն",
                    isoLanguageCode = "hy",
                    transcription = "Voghjuyn",
                    translations = listOf(ChatTranslationResponse("Greetings", "en"))
                )
            )
        )
    )
}
