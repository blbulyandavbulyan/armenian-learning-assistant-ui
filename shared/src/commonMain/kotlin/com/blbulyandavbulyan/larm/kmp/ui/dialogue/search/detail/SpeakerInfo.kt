package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search.detail

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.unknown_speaker
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueSpeakerResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.PhraseTranslation
import com.blbulyandavbulyan.larm.kmp.ui.common.ListenIcon
import com.blbulyandavbulyan.larm.kmp.ui.common.RoundedTopLeftCutShape
import org.jetbrains.compose.resources.stringResource

@Composable
fun SpeakerInfo(
    speaker: GetDialogueSpeakerResponse?,
    modifier: Modifier,
    onPlayAudio: (String) -> Unit
) {
    if (speaker == null) {
        Text(text = stringResource(Res.string.unknown_speaker), modifier = modifier)
        return
    }

    val speakerShape = RoundedTopLeftCutShape(cornerRadius = 24f, cutOffset = 35f)

    val audioAssetUrl = speaker.name.audioAssetUrl
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                shape = speakerShape
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = speakerShape
            )
            .clip(speakerShape)
            .clickable {
                audioAssetUrl?.let { url ->
                    onPlayAudio(url)
                }
            }
            .padding(start = 40.dp, top = 12.dp, end = 12.dp, bottom = 12.dp) // Generous inner padding
    ) {
        Column {
            SpeakerName(
                id = speaker.id,
                name = speaker.name.phrase,
                showListenIcon = audioAssetUrl != null
            )

            SpeakerTranscription(speakerId = speaker.id, transcription = speaker.name.transcription)

            SpeakerTranslations(speakerId = speaker.id, translations = speaker.name.translations)
        }
    }
}

@Composable
private fun SpeakerName(id: String, name: String, showListenIcon: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {

        if (showListenIcon) {
            ListenIcon(modifier = Modifier.size(size = 40.dp).padding(4.dp))
            Spacer(modifier = Modifier.width(width = 10.dp))
        }

        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("speakerName_$id")
        )
    }
}

@Composable
private fun SpeakerTranscription(speakerId: String, transcription: String) {
    Text(
        text = "($transcription)",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .testTag("speakerTranscription_$speakerId")
    )
}

@Composable
private fun SpeakerTranslations(
    speakerId: String, translations: List<PhraseTranslation>
) {
    translations.forEach { translation ->
        Text(
            text = translation.translationText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 2.dp)
                .testTag("speakerTranslation_${speakerId}_${translation.id}")
        )
    }
}