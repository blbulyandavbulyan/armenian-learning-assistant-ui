package com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.search

import kotlinx.collections.immutable.persistentListOf

object DomainMothers {
    val DIALOGUE_1 = Dialogue(
        id = "dialogue_id_123",
        title = Phrase(
            id = "title_1",
            text = "Բարև Ձեզ",
            isoLanguageCode = "hy",
            transcription = "Barev Dzez",
            translations = persistentListOf(
                PhraseTranslation("1", "en", "Hello")
            ),
            assets = persistentListOf(
                Asset("audio/mpeg", "http://test.audio/title")
            )
        ),
        speakers = persistentListOf(
            Speaker(
                id = "speaker_1",
                name = Phrase(
                    id = "speaker_name_1",
                    text = "Անուն",
                    isoLanguageCode = "hy",
                    transcription = "Anun",
                    translations = persistentListOf(
                        PhraseTranslation("t_sp_1", "en", "Name")
                    ),
                    assets = persistentListOf(
                        Asset("audio/mpeg", "http://test.audio/speaker")
                    )
                )
            ),
            Speaker(
                id = "speaker_2",
                name = Phrase(
                    id = "speaker_name_2",
                    text = "Անուն 2",
                    isoLanguageCode = "hy",
                    transcription = "Anun 2",
                    translations = persistentListOf(
                        PhraseTranslation("t_sp_2", "en", "Name 2")
                    ),
                    assets = persistentListOf(
                        Asset("audio/mpeg", "http://test.audio/speaker2")
                    )
                )
            )
        ),
        phrases = persistentListOf(
            DialoguePhrase(
                speakerId = "speaker_1",
                phrase = Phrase(
                    id = "phrase_resp_1",
                    text = "Ինչպես եք",
                    isoLanguageCode = "hy",
                    transcription = "Inchpes ek",
                    translations = persistentListOf(
                        PhraseTranslation("t_ph_1", "en", "How are you?")
                    ),
                    assets = persistentListOf(
                        Asset("audio/mpeg", "http://test.audio/phrase")
                    )
                )
            ),
            DialoguePhrase(
                speakerId = "speaker_2",
                phrase = Phrase(
                    id = "phrase_resp_2",
                    text = "Լավ եմ",
                    isoLanguageCode = "hy",
                    transcription = "Lav em",
                    translations = persistentListOf(
                        PhraseTranslation("t_ph_2", "en", "I am fine")
                    ),
                    assets = persistentListOf(
                        Asset("audio/mpeg", "http://test.audio/phrase2")
                    )
                )
            )
        )
    )

    val DIALOGUE_SUMMARY_1 = DialogueSummary(
        id = DIALOGUE_1.id,
        title = DIALOGUE_1.title
    )

    val DIALOGUE_SUMMARY_2 = DialogueSummary(
        id = "dialogue_id_456",
        title = Phrase(
            id = "title_2",
            text = "Ինչպես եք",
            isoLanguageCode = "hy",
            transcription = "Inchpes ek",
            translations = persistentListOf(
                PhraseTranslation("2", "en", "How are you")
            ),
            assets = persistentListOf(
                Asset("audio/mpeg", "http://test.audio/title_2")
            )
        )
    )
}
