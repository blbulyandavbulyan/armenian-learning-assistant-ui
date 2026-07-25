package com.blbulyandavbulyan.larm.kmp.ui.dialogue.detail

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponseMother
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueSpeakerResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.PhraseResponse
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.assertDialogueTitle
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DialogueDetailScreenTest {
    @Test
    fun detailScreen_listenButtonsInvokeCorrectAudioEndpoint() = runComposeUiTest {
        val playedUrls = mutableListOf<String>()

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueDetailScreen(
                    dialogue = GetDialogueResponseMother.Dialogue1.RESPONSE,
                    onBack = { },
                    onPlayAudio = { playedUrls.add(it) }
                )
            }
        }

        val dialogueId = GetDialogueResponseMother.Dialogue1.RESPONSE.id

        // Test Case 1: Title Listen Button
        onNodeWithTag("listenTitleButton_$dialogueId").performClick()
        playedUrls.last() shouldBe GetDialogueResponseMother.Dialogue1.RESPONSE.title.assets.first().url

        // Test Case 2: Speaker Listen Button
        val speakerId = GetDialogueResponseMother.Dialogue1.RESPONSE.speakers.first().id
        onNodeWithTag("listenSpeakerButton_$speakerId").performScrollTo().performClick()
        playedUrls.last() shouldBe GetDialogueResponseMother.Dialogue1.RESPONSE.speakers.first().name.assets.first().url

        // Test Case 3: Phrase Listen Button
        val phraseId = GetDialogueResponseMother.Dialogue1.RESPONSE.dialoguePhrases.first().phrase.id
        onNodeWithTag("listenPhraseButton_$phraseId").performScrollTo().performClick()
        playedUrls.last() shouldBe GetDialogueResponseMother.Dialogue1.RESPONSE.dialoguePhrases.first().phrase.assets.first().url
    }

    @Test
    fun detailScreen_displaysInformationCorrectly() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueDetailScreen(
                    dialogue = GetDialogueResponseMother.Dialogue1.RESPONSE,
                    onBack = { },
                    onPlayAudio = { }
                )
            }
        }

        assertDetailScreenContentVisible()
    }

    @OptIn(ExperimentalTestApi::class)
    private fun ComposeUiTest.assertDetailScreenContentVisible() {
        assertDialogueTitle(GetDialogueResponseMother.Dialogue1.RESPONSE.title)

        val speaker1 = GetDialogueResponseMother.Dialogue1.RESPONSE.speakers[0]
        val phrase1 = GetDialogueResponseMother.Dialogue1.RESPONSE.dialoguePhrases[0].phrase

        val speaker2 = GetDialogueResponseMother.Dialogue1.RESPONSE.speakers[1]
        val phrase2 = GetDialogueResponseMother.Dialogue1.RESPONSE.dialoguePhrases[1].phrase

        assertSpeakerAndPhrase(speaker1, phrase1)
        assertSpeakerAndPhrase(speaker2, phrase2)
    }

    private fun ComposeUiTest.assertSpeakerAndPhrase(
        speaker: GetDialogueSpeakerResponse,
        phrase: PhraseResponse
    ) {
        onNodeWithTag("speakerName_${speaker.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(speaker.name.phrase)
        onNodeWithTag(
            "speakerTranscription_${speaker.id}",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
            .assertTextEquals("(${speaker.name.transcription})")
        speaker.name.translations.forEachIndexed { index, translation ->
            onNodeWithTag(
                "speakerTranslation_${speaker.id}_$index",
                useUnmergedTree = true
            ).performScrollTo().assertIsDisplayed()
                .assertTextEquals(translation.translationText)
        }

        onNodeWithTag("phraseText_${phrase.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase.phrase)
        onNodeWithTag("phraseTranscription_${phrase.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase.transcription)
        phrase.translations.forEachIndexed { index, translation ->
            onNodeWithTag(
                "phraseTranslation_${phrase.id}_$index",
                useUnmergedTree = true
            ).performScrollTo().assertIsDisplayed()
                .assertTextEquals(translation.translationText)
        }
    }
}
