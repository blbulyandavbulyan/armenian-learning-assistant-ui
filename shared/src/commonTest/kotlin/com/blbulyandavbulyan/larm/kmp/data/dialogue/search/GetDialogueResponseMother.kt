package com.blbulyandavbulyan.larm.kmp.data.dialogue.search

object GetDialogueResponseMother {
    object Dialogue1 {
        val JSON_RESPONSE = """
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
              },
              "speakers": [
                {
                  "id": "speaker_1",
                  "name": {
                    "id": "speaker_name_1",
                    "phrase": "Անուն",
                    "isoLanguageCode": "hy",
                    "transcription": "Anun",
                    "translations": [
                      {
                        "id": "t_sp_1",
                        "isoLanguageCode": "en",
                        "translationText": "Name"
                      }
                    ],
                    "assets": [
                      {
                        "contentType": "audio/mpeg",
                        "url": "http://test.audio/speaker"
                      }
                    ]
                  }
                },
                {
                  "id": "speaker_2",
                  "name": {
                    "id": "speaker_name_2",
                    "phrase": "Անուն 2",
                    "isoLanguageCode": "hy",
                    "transcription": "Anun 2",
                    "translations": [
                      {
                        "id": "t_sp_2",
                        "isoLanguageCode": "en",
                        "translationText": "Name 2"
                      }
                    ],
                    "assets": [
                      {
                        "contentType": "audio/mpeg",
                        "url": "http://test.audio/speaker2"
                      }
                    ]
                  }
                }
              ],
              "dialoguePhrases": [
                {
                  "speakerId": "speaker_1",
                  "phrase": {
                    "id": "phrase_resp_1",
                    "phrase": "Ինչպես եք",
                    "isoLanguageCode": "hy",
                    "transcription": "Inchpes ek",
                    "translations": [
                      {
                        "id": "t_ph_1",
                        "isoLanguageCode": "en",
                        "translationText": "How are you?"
                      }
                    ],
                    "assets": [
                      {
                        "contentType": "audio/mpeg",
                        "url": "http://test.audio/phrase"
                      }
                    ]
                  }
                },
                {
                  "speakerId": "speaker_2",
                  "phrase": {
                    "id": "phrase_resp_2",
                    "phrase": "Լավ եմ",
                    "isoLanguageCode": "hy",
                    "transcription": "Lav em",
                    "translations": [
                      {
                        "id": "t_ph_2",
                        "isoLanguageCode": "en",
                        "translationText": "I am fine"
                      }
                    ],
                    "assets": [
                      {
                        "contentType": "audio/mpeg",
                        "url": "http://test.audio/phrase2"
                      }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val RESPONSE = GetDialogueResponse(
            id = "dialogue_id_123",
            title = PhraseResponse(
                id = "title_1",
                phrase = "Բարև Ձեզ",
                isoLanguageCode = "hy",
                transcription = "Barev Dzez",
                translations = listOf(
                    PhraseTranslationResponse("1", "en", "Hello")
                ),
                assets = listOf(
                    AssetResponse("audio/mpeg", "http://test.audio/title")
                )
            ),
            speakers = listOf(
                GetDialogueSpeakerResponse(
                    id = "speaker_1",
                    name = PhraseResponse(
                        id = "speaker_name_1",
                        phrase = "Անուն",
                        isoLanguageCode = "hy",
                        transcription = "Anun",
                        translations = listOf(
                            PhraseTranslationResponse("t_sp_1", "en", "Name")
                        ),
                        assets = listOf(
                            AssetResponse("audio/mpeg", "http://test.audio/speaker")
                        )
                    )
                ),
                GetDialogueSpeakerResponse(
                    id = "speaker_2",
                    name = PhraseResponse(
                        id = "speaker_name_2",
                        phrase = "Անուն 2",
                        isoLanguageCode = "hy",
                        transcription = "Anun 2",
                        translations = listOf(
                            PhraseTranslationResponse("t_sp_2", "en", "Name 2")
                        ),
                        assets = listOf(
                            AssetResponse("audio/mpeg", "http://test.audio/speaker2")
                        )
                    )
                )
            ),
            dialoguePhrases = listOf(
                GetDialoguePhraseResponse(
                    speakerId = "speaker_1",
                    phrase = PhraseResponse(
                        id = "phrase_resp_1",
                        phrase = "Ինչպես եք",
                        isoLanguageCode = "hy",
                        transcription = "Inchpes ek",
                        translations = listOf(
                            PhraseTranslationResponse("t_ph_1", "en", "How are you?")
                        ),
                        assets = listOf(
                            AssetResponse("audio/mpeg", "http://test.audio/phrase")
                        )
                    )
                ),
                GetDialoguePhraseResponse(
                    speakerId = "speaker_2",
                    phrase = PhraseResponse(
                        id = "phrase_resp_2",
                        phrase = "Լավ եմ",
                        isoLanguageCode = "hy",
                        transcription = "Lav em",
                        translations = listOf(
                            PhraseTranslationResponse("t_ph_2", "en", "I am fine")
                        ),
                        assets = listOf(
                            AssetResponse("audio/mpeg", "http://test.audio/phrase2")
                        )
                    )
                )
            )
        )
    }
}
