package com.blbulyandavbulyan.larm.kmp.data.dialogue.search

object SearchDialoguesResponseMother {
    object SearchResponse1 {
        val JSON_RESPONSE = """
            {
              "dialogues": [
                {
                  "id": "dialogue_id_123",
                  "title": {
                    "id": "title_1",
                    "phrase": "Բարև Ձեզ",
                    "isoLanguageCode": "hy",
                    "transcription": "Barev Dzez",
                    "translations": [
                      {
                        "id": "1",
                        "isoLanguageCode": "en",
                        "translationText": "Hello"
                      }
                    ],
                    "assets": [
                      {
                        "contentType": "audio/mpeg",
                        "url": "http://test.audio/title"
                      }
                    ]
                  }
                },
                {
                  "id": "dialogue_id_456",
                  "title": {
                    "id": "title_2",
                    "phrase": "Ինչպես եք",
                    "isoLanguageCode": "hy",
                    "transcription": "Inchpes ek",
                    "translations": [
                      {
                        "id": "2",
                        "isoLanguageCode": "en",
                        "translationText": "How are you"
                      }
                    ],
                    "assets": [
                      {
                        "contentType": "audio/mpeg",
                        "url": "http://test.audio/title_2"
                      }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val RESPONSE = SearchDialoguesResponse(
            dialogues = listOf(
                DialogueSummaryResponse(
                    id = GetDialogueResponseMother.Dialogue1.RESPONSE.id,
                    title = GetDialogueResponseMother.Dialogue1.RESPONSE.title
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
}
