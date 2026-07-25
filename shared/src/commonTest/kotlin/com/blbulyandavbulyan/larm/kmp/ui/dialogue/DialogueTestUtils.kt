package com.blbulyandavbulyan.larm.kmp.ui.dialogue

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.PhraseResponse

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertDialogueTitle(
    title: PhraseResponse,
    phraseTestTag: String = "detailTitleText",
    transcriptionTestTag: String = "detailTranscriptionText",
    translationTestTagPrefix: String = "detailTranslationText"
) {
    onNodeWithTag(phraseTestTag, useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        .assertTextEquals(title.phrase)
    onNodeWithTag(transcriptionTestTag, useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        .assertTextEquals(title.transcription)
    title.translations.forEach { translation ->
        onNodeWithTag(
            "${translationTestTagPrefix}_${translation.id}",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
            .assertTextEquals(translation.translationText)
    }
}
