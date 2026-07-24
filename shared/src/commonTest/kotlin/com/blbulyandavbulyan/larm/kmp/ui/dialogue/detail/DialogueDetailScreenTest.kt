package com.blbulyandavbulyan.larm.kmp.ui.dialogue.detail

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponseMother
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
                    dialogue = GetDialogueResponseMother.FULL_DIALOGUE_1,
                    onBack = { },
                    onPlayAudio = { playedUrls.add(it) }
                )
            }
        }

        val dialogueId = GetDialogueResponseMother.FULL_DIALOGUE_1.id

        // Test Case 1: Title Listen Button
        onNodeWithTag("listenTitleButton_$dialogueId").performClick()
        playedUrls.last() shouldBe GetDialogueResponseMother.FULL_DIALOGUE_1.title.assets.first().url

        // Test Case 2: Speaker Listen Button
        val speakerId = GetDialogueResponseMother.FULL_DIALOGUE_1.speakers.first().id
        onNodeWithTag("listenSpeakerButton_$speakerId").performScrollTo().performClick()
        playedUrls.last() shouldBe GetDialogueResponseMother.FULL_DIALOGUE_1.speakers.first().name.assets.first().url

        // Test Case 3: Phrase Listen Button
        val phraseId = GetDialogueResponseMother.FULL_DIALOGUE_1.dialoguePhrases.first().phrase.id
        onNodeWithTag("listenPhraseButton_$phraseId").performScrollTo().performClick()
        playedUrls.last() shouldBe GetDialogueResponseMother.FULL_DIALOGUE_1.dialoguePhrases.first().phrase.assets.first().url
    }

    @Test
    fun detailScreen_displaysInformationCorrectly() = runComposeUiTest {
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueDetailScreen(
                    dialogue = GetDialogueResponseMother.FULL_DIALOGUE_1,
                    onBack = { },
                    onPlayAudio = { }
                )
            }
        }

        assertDetailScreenContentVisible()
    }

    @OptIn(ExperimentalTestApi::class)
    private fun androidx.compose.ui.test.ComposeUiTest.assertDetailScreenContentVisible() {
        val expectedPhrase1 = GetDialogueResponseMother.FULL_DIALOGUE_1.title.phrase
        val expectedTranscription1 = GetDialogueResponseMother.FULL_DIALOGUE_1.title.transcription

        // Assert the correct dialogue is shown in the detail screen
        onNodeWithTag("detailTitleText", useUnmergedTree = true).assertIsDisplayed().assertTextEquals(expectedPhrase1)
        onNodeWithTag("detailTranscriptionText", useUnmergedTree = true).assertIsDisplayed()
            .assertTextEquals(expectedTranscription1)

        val speaker1 = GetDialogueResponseMother.FULL_DIALOGUE_1.speakers[0]
        val phrase1 = GetDialogueResponseMother.FULL_DIALOGUE_1.dialoguePhrases[0].phrase

        val speaker2 = GetDialogueResponseMother.FULL_DIALOGUE_1.speakers[1]
        val phrase2 = GetDialogueResponseMother.FULL_DIALOGUE_1.dialoguePhrases[1].phrase

        // Assert the first speaker and phrase are shown using tags
        onNodeWithTag("speakerName_${speaker1.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(speaker1.name.phrase)
        onNodeWithTag(
            "speakerTranscription_${speaker1.id}",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
            .assertTextEquals("(${speaker1.name.transcription})")
        onNodeWithTag(
            "speakerTranslation_${speaker1.id}_0",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
            .assertTextEquals(speaker1.name.translations[0].translationText)

        onNodeWithTag("phraseText_${phrase1.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase1.phrase)
        onNodeWithTag("phraseTranscription_${phrase1.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase1.transcription)
        // Assert phrase 1 translation is shown
        onNodeWithTag("phraseTranslation_${phrase1.id}_0", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase1.translations[0].translationText)

        // Assert the second speaker and phrase are shown using tags
        onNodeWithTag("speakerName_${speaker2.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(speaker2.name.phrase)
        onNodeWithTag(
            "speakerTranscription_${speaker2.id}",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
            .assertTextEquals("(${speaker2.name.transcription})")
        onNodeWithTag(
            "speakerTranslation_${speaker2.id}_0",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
            .assertTextEquals(speaker2.name.translations[0].translationText)

        onNodeWithTag("phraseText_${phrase2.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase2.phrase)
        onNodeWithTag("phraseTranscription_${phrase2.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase2.transcription)
        // Assert phrase 2 translation is shown
        onNodeWithTag("phraseTranslation_${phrase2.id}_0", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(phrase2.translations[0].translationText)
    }
}
