package com.blbulyandavbulyan.larm.kmp.ui.dialogue.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.PhraseResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.PhraseTranslation
import com.blbulyandavbulyan.larm.kmp.ui.common.ListenIcon
import com.blbulyandavbulyan.larm.kmp.ui.common.RoundedCutBottomRightShape

@Composable
fun DialogueTitle(
    dialogueTitle: PhraseResponse,
    testTag: String? = null,
    phraseTestTag: String = "detailTitleText",
    transcriptionTestTag: String = "detailTranscriptionText",
    onPlayAudio: (String) -> Unit
) {
    val headerShape = RoundedCutBottomRightShape(
        cornerRadius = 24f, // Smooth rounding
        cutOffset = 50f // Sharp accent cut
    )

    val audioAssetUrl = dialogueTitle.audioAssetUrl
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = headerShape
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = headerShape
            )
            .clip(headerShape)
            .clickable {
                audioAssetUrl?.let { url ->
                    onPlayAudio(url)
                }
            }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(16.dp)
    ) {
        Column {
            DialogueTitlePhrase(
                shouldShowListenButton = audioAssetUrl != null,
                phrase = dialogueTitle.phrase,
                testTag = phraseTestTag
            )

            DialogueTitleTranscription(dialogueTitle.transcription, testTag = transcriptionTestTag)

            DialogueTitleTranslations(dialogueTitle.translations)
        }
    }
}

@Composable
private fun DialogueTitlePhrase(shouldShowListenButton: Boolean, phrase: String, testTag: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (shouldShowListenButton) {
            ListenIcon(
                modifier = Modifier.size(size = 40.dp)
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(width = 10.dp))
        }

        Text(
            text = phrase,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp).testTag(testTag)
        )
    }
}

@Composable
private fun DialogueTitleTranscription(transcription: String, testTag: String) {
    Text(
        text = transcription,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp).testTag(testTag)
    )
}

@Composable
private fun DialogueTitleTranslations(
    translations: List<PhraseTranslation>
) {
    translations.forEach { translation ->
        Text(
            text = translation.translationText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
