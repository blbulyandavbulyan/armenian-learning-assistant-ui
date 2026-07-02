package com.blbulyandavbulyan.larm.kmp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.action_save_dialogue
import armenianlearningassistant_kmp.shared.generated.resources.unknown_speaker
import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.data.SpeakerResponse
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogueView(dialogue: DialogueChatResponse, fontFamily: FontFamily) {
    val speakersMap = dialogue.speakers.associateBy { it.id }

    Column(modifier = Modifier.fillMaxWidth()) {
        AiMessageBubble(dialogue.message, fontFamily)

        DialogueInfoContent(dialogue, fontFamily)

        Spacer(modifier = Modifier.height(16.dp))

        DialoguePhrasesContent(dialogue, speakersMap, fontFamily)

        Spacer(modifier = Modifier.height(16.dp))

        SaveButton { /* TODO: Save functionality for later branch */ }
    }
}

@Composable
private fun SaveButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3460)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(stringResource(Res.string.action_save_dialogue), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
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
                    containerColor = Color(0xFF0F3460).copy(alpha = 0.8f)
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
                            color = Color(0xFFE94560),
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = phraseObj.phrase.phrase,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = fontFamily
                        )
                    )
                    Text(
                        text = phraseObj.phrase.transcription,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFA0A0B0),
                            fontStyle = FontStyle.Italic
                        )
                    )

                    val phraseTranslations =
                        phraseObj.phrase.translations.joinToString(" | ") { it.translationText }
                    if (phraseTranslations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = phraseTranslations,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF888899))
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
                    color = Color.White,
                    fontFamily = fontFamily
                )
            )
            Text(
                text = dialogue.info.transcription,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFA0A0B0))
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
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            modifier = Modifier.fillMaxWidth(0.85f).padding(bottom = 16.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontFamily = fontFamily
                )
            )
        }
    }
}