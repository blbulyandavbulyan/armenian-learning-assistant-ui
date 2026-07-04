package com.blbulyandavbulyan.larm.kmp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.action_save_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.action_saved_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.unknown_speaker
import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.SpeakerResponse
import org.jetbrains.compose.resources.stringResource
import com.blbulyandavbulyan.larm.kmp.ui.theme.AppTheme

@Composable
fun DialogueView(
    dialogue: DialogueChatResponse,
    fontFamily: FontFamily,
    isSaving: Boolean = false,
    isSaved: Boolean = false,
    onSaveClick: () -> Unit = {}
) {
    val speakersMap = dialogue.speakers.associateBy { it.id }

    Column(modifier = Modifier.fillMaxWidth()) {
        AiMessageBubble(dialogue.message, fontFamily)

        DialogueInfoContent(dialogue, fontFamily)

        Spacer(modifier = Modifier.height(16.dp))

        DialoguePhrasesContent(dialogue, speakersMap, fontFamily)

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedSaveButton(
            isSaving = isSaving,
            isSaved = isSaved,
            savedText = stringResource(Res.string.action_saved_dialogue),
            saveText = stringResource(Res.string.action_save_dialogue),
            onClick = onSaveClick
        )
    }
}

@Composable
private fun DialoguePhrasesContent(
    dialogue: DialogueChatResponse,
    speakersMap: Map<String, SpeakerResponse>,
    fontFamily: FontFamily
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        dialogue.dialoguePhrases.forEach { phraseObj ->
            val speaker = speakersMap[phraseObj.speakerId]
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomEnd = 16.dp,
                    bottomStart = 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.saveButton.copy(alpha = 0.8f)
                ),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val speakerText = speaker?.let { spk ->
                        val translations = spk.translations.joinToString(" | ") { it.translationText }
                        if (translations.isNotEmpty()) "${spk.title} | $translations" else spk.title
                    } ?: stringResource(Res.string.unknown_speaker)

                    Text(
                        text = speakerText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        ),
                        modifier = Modifier.padding(bottom = 4.dp).testTag("dialogueSpeaker")
                    )
                    Text(
                        text = phraseObj.phrase.phrase,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            fontFamily = fontFamily
                        ),
                        modifier = Modifier.testTag("dialoguePhraseText")
                    )
                    Text(
                        text = phraseObj.phrase.transcription,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    )

                    val phraseTranslations =
                        phraseObj.phrase.translations.joinToString(" | ") { it.translationText }
                    if (phraseTranslations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = phraseTranslations,
                            fontSize = 14.sp,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogueInfoContent(
    dialogue: DialogueChatResponse,
    fontFamily: FontFamily
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val titleTranslations = dialogue.info.translations.joinToString(" | ") { it.translationText }
            val titleText =
                if (titleTranslations.isNotEmpty()) "${dialogue.info.title} | $titleTranslations" else dialogue.info.title

            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = fontFamily
                )
            )
            Text(
                text = dialogue.info.transcription,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
private fun AiMessageBubble(
    message: String,
    fontFamily: FontFamily
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomEnd = 16.dp,
                bottomStart = 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            ),
            modifier = Modifier.fillMaxWidth(0.85f).padding(bottom = 16.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp).testTag("aiMessageText"),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = fontFamily
                )
            )
        }
    }
}
