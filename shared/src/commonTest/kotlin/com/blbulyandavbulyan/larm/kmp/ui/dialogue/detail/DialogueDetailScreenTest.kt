package com.blbulyandavbulyan.larm.kmp.ui.dialogue.detail

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DomainMothers
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Phrase
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.Speaker
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.assertDialogueTitle
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DialogueDetailScreenTest {
    @Test
    fun detailScreen_listenButtonsInvokeCorrectAudioEndpoint() = runComposeUiTest {
        val playedUrls = mutableListOf<String>()
        val dialogueDomain = DomainMothers.DIALOGUE_1

        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueDetailScreen(
                    dialogue = dialogueDomain,
                    onPlayAudio = { playedUrls.add(it) }
                )
            }
        }

        val dialogueId = dialogueDomain.id

        // Test Case 1: Title Listen Button
        onNodeWithTag("listenTitleButton_$dialogueId").performClick()
        playedUrls.last() shouldBe dialogueDomain.title.assets.first().url

        // Test Case 2: Speaker Listen Button
        val speakerId = dialogueDomain.speakers.first().id
        onNodeWithTag("listenSpeakerButton_$speakerId").performScrollTo().performClick()
        playedUrls.last() shouldBe dialogueDomain.speakers.first().name.assets.first().url

        // Test Case 3: Phrase Listen Button
        val phraseId = dialogueDomain.phrases.first().phrase.id
        onNodeWithTag("listenPhraseButton_$phraseId").performScrollTo().performClick()
        playedUrls.last() shouldBe dialogueDomain.phrases.first().phrase.assets.first().url
    }

    @Test
    fun detailScreen_displaysInformationCorrectly() = runComposeUiTest {
        val dialogueDomain = DomainMothers.DIALOGUE_1
        setContent {
            ArmenianLearningTheme(darkTheme = true) {
                DialogueDetailScreen(
                    dialogue = dialogueDomain,
                    onPlayAudio = { }
                )
            }
        }

        assertDetailScreenContentVisible()
    }

    @OptIn(ExperimentalTestApi::class)
    private fun ComposeUiTest.assertDetailScreenContentVisible() {
        val dialogueDomain = DomainMothers.DIALOGUE_1
        assertDialogueTitle(dialogueDomain.title)

        val speaker1 = dialogueDomain.speakers[0]
        val phrase1 = dialogueDomain.phrases[0].phrase

        val speaker2 = dialogueDomain.speakers[1]
        val phrase2 = dialogueDomain.phrases[1].phrase

        assertSpeakerAndPhrase(speaker1, phrase1)
        assertSpeakerAndPhrase(speaker2, phrase2)
    }

    private fun ComposeUiTest.assertSpeakerAndPhrase(
        speaker: Speaker,
        phrase: Phrase
    ) {
        onNodeWithTag("speakerName_${speaker.id}", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            .assertTextEquals(speaker.name.text)
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
            .assertTextEquals(phrase.text)
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
