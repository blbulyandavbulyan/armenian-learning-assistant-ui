package com.blbulyandavbulyan.larm.kmp.data.dialogue.chat

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

    val FULL_DIALOGUE_2 = DialogueChatResponse(
        message = "Another dialogue:",
        info = DialogueTitleResponse(
            title = "Ռեստորանում",
            transcription = "Restoranum",
            translations = listOf(ChatTranslationResponse("In the restaurant", "en"))
        ),
        speakers = listOf(
            SpeakerResponse("1", "Մատուցող", "Matutsogh", listOf(ChatTranslationResponse("Waiter", "en"))),
            SpeakerResponse("2", "Հաճախորդ", "Hachakhord", listOf(ChatTranslationResponse("Customer", "en")))
        ),
        dialoguePhrases = listOf(
            DialoguePhraseResponse(
                speakerId = "1",
                phrase = DraftPhrasesResponse(
                    phrase = "Ի՞նչ կպատվիրեք",
                    isoLanguageCode = "hy",
                    transcription = "Inch kpatvirek",
                    translations = listOf(ChatTranslationResponse("What will you order?", "en"))
                )
            ),
            DialoguePhraseResponse(
                speakerId = "2",
                phrase = DraftPhrasesResponse(
                    phrase = "Ես կցանկանայի մեկ սուրճ",
                    isoLanguageCode = "hy",
                    transcription = "Es ktsankanayi mek surch",
                    translations = listOf(ChatTranslationResponse("I would like a coffee", "en"))
                )
            )
        )
    )
}
