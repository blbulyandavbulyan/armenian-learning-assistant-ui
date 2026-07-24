package com.blbulyandavbulyan.larm.kmp.data.dialogue.search

object GetDialogueResponseMother {
    val FULL_DIALOGUE_1 = GetDialogueResponse(
        id = "dialogue_id_123",
        title = PhraseResponse(
            id = "title_1",
            phrase = "Բարև Ձեզ",
            isoLanguageCode = "hy",
            transcription = "Barev Dzez",
            translations = listOf(
                PhraseTranslation("1", "en", "Hello")
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
                        PhraseTranslation("t_sp_1", "en", "Name")
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
                        PhraseTranslation("t_sp_2", "en", "Name 2")
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
                        PhraseTranslation("t_ph_1", "en", "How are you?")
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
                        PhraseTranslation("t_ph_2", "en", "I am fine")
                    ),
                    assets = listOf(
                        AssetResponse("audio/mpeg", "http://test.audio/phrase2")
                    )
                )
            )
        )
    )
}
