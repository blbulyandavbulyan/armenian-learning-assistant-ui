package com.blbulyandavbulyan.larm.kmp.data.dialogue.search

object SearchDialoguesResponseMother {
    val SEARCH_RESPONSE_1 = SearchDialoguesResponse(
        dialogues = listOf(
            DialogueSummaryResponse(
                id = GetDialogueResponseMother.FULL_DIALOGUE_1.id,
                title = GetDialogueResponseMother.FULL_DIALOGUE_1.title
            ),
            DialogueSummaryResponse(
                id = "dialogue_id_456",
                title = PhraseResponse(
                    id = "title_2",
                    phrase = "Ինչպես եք",
                    isoLanguageCode = "hy",
                    transcription = "Inchpes ek",
                    translations = listOf(
                        PhraseTranslation("2", "en", "How are you")
                    ),
                    assets = listOf(
                        AssetResponse("audio/mpeg", "http://test.audio/title_2")
                    )
                )
            )
        )
    )
}
