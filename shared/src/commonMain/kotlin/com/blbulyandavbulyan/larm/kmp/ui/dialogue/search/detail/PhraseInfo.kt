package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialoguePhraseResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.ui.common.ListenIcon

@Composable
fun DialoguePhrases(
    dialogue: GetDialogueResponse,
    onPlayAudio: (String) -> Unit
) {
    dialogue.dialoguePhrases.forEach { dialoguePhrase ->
        val speaker = dialogue.speakers.find { it.id == dialoguePhrase.speakerId }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                SpeakerInfo(
                    speaker = speaker,
                    modifier = Modifier.weight(weight = 0.35f),
                    onPlayAudio = onPlayAudio
                )

                PhraseInfo(
                    dialoguePhrase = dialoguePhrase,
                    modifier = Modifier.weight(weight = 0.65f),
                    onPlayAudio = onPlayAudio
                )
            }
        }
    }
}

@Composable
private fun PhraseInfo(
    dialoguePhrase: GetDialoguePhraseResponse,
    modifier: Modifier,
    onPlayAudio: (String) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        // The Chat Bubble Box
        val phraseAudio = dialoguePhrase.phrase.audioAssetUrl

        ChatBubbleBox(
            testTag = "listenPhraseButton_${dialoguePhrase.phrase.id}",
            onClick = {
                phraseAudio?.let {
                    onPlayAudio(it)
                }
            }
        ) {
            Column {
                MainPhrase(dialoguePhrase = dialoguePhrase, displayListenIcon = phraseAudio != null)

                PhraseTranscription(dialoguePhrase)

                PhraseTranslations(dialoguePhrase)
            }
        }
    }
}

@Composable
private fun PhraseTranslations(dialoguePhrase: GetDialoguePhraseResponse) {
    dialoguePhrase.phrase.translations.forEachIndexed { index, translation ->
        Text(
            text = translation.translationText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .testTag("phraseTranslation_${dialoguePhrase.phrase.id}_$index")
        )
    }
}

@Composable
private fun PhraseTranscription(dialoguePhrase: GetDialoguePhraseResponse) {
    Text(
        text = dialoguePhrase.phrase.transcription,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .testTag("phraseTranscription_${dialoguePhrase.phrase.id}")
    )
}

@Composable
private fun MainPhrase(
    dialoguePhrase: GetDialoguePhraseResponse,
    displayListenIcon: Boolean
) {
    Row {
        if (displayListenIcon) {
            ListenIcon(modifier = Modifier.size(size = 40.dp).padding(4.dp))
            Spacer(modifier = Modifier.width(width = 10.dp))
        }

        Text(
            text = dialoguePhrase.phrase.phrase,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(bottom = 6.dp)
                .testTag("phraseText_${dialoguePhrase.phrase.id}")
        )
    }
}
