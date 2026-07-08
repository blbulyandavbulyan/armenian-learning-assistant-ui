package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.back_button_text
import armenianlearningassistant_kmp.shared.generated.resources.unknown_speaker
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueViewModel
import com.blbulyandavbulyan.larm.kmp.ui.common.ListenButton
import com.blbulyandavbulyan.larm.kmp.ui.common.PrimaryVerticalScrollbar
import com.blbulyandavbulyan.larm.kmp.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogueDetailScreen(
    dialogue: GetDialogueResponse,
    onBack: () -> Unit,
    viewModel: DialogueViewModel
) {
    val appColors = AppTheme.colors
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(appColors.gradientTop, appColors.gradientBottom)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        TopAppBar(
            title = { },
            navigationIcon = {
                TextButton(onClick = onBack, modifier = Modifier.testTag("backButton")) {
                    // TODO 'back icon' should be used here
                    Text(stringResource(Res.string.back_button_text))
                }
            }
        )

        val scrollState = rememberScrollState()
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                DialogueTitle(dialogue, viewModel)

                Spacer(modifier = Modifier.height(height = 16.dp))

                dialogue.dialoguePhrases.forEach { dialoguePhrase ->
                    val speaker = dialogue.speakers.find { it.id == dialoguePhrase.speakerId }
                    val speakerName = speaker?.name?.phrase ?: stringResource(Res.string.unknown_speaker)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface // Use your app's surface color
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                        ) {
                            // ==========================================
                            // LEFT COLUMN: SPEAKER INFO
                            // ==========================================
                            Column(
                                modifier = Modifier.weight(weight = 0.35f),
                            ) {
                                // Speaker Name & Small Audio Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = speakerName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.testTag("speakerName_${speaker?.id}")
                                    )

                                    Spacer(modifier = Modifier.width(width = 4.dp))

                                    speaker?.name?.audioAssetUrl?.let { url ->
                                        // Mini circular play button next to the name
                                        ListenButton(size = 40.dp, testTag = "listenSpeakerButton_${speaker.id}") {
                                            viewModel.playAudio(url)
                                        }
                                    }
                                }

                                // Speaker Transcription
                                speaker?.name?.transcription?.let { transcription ->
                                    Text(
                                        text = "($transcription)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(
                                            bottom = 12.dp
                                        ).testTag("speakerTranscription_${speaker.id}")
                                    )
                                }

                                // Speaker Translations
                                speaker?.name?.translations?.forEach { translation ->
                                    Text(
                                        // Note: If your model supports the language name (e.g. "English: Buyer"), format it here!
                                        text = translation.translationText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }

                            // ==========================================
                            // RIGHT COLUMN: PHRASE BUBBLE & BUTTON
                            // ==========================================
                            Column(
                                modifier = Modifier.weight(weight = 0.65f)
                            ) {
                                // The Chat Bubble Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant, // Slightly different shade for the bubble
                                            // This shape mimics a chat bubble tail on the bottom-left
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomEnd = 16.dp,
                                                bottomStart = 2.dp
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomEnd = 16.dp,
                                                bottomStart = 2.dp
                                            )
                                        )
                                        .padding(all = 16.dp)
                                ) {
                                    Column {
                                        // Main Phrase
                                        Row {
                                            Text(
                                                text = dialoguePhrase.phrase.phrase,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier
                                                    .padding(bottom = 6.dp)
                                                    .testTag("phraseText_${dialoguePhrase.phrase.id}")
                                            )

                                            Spacer(modifier = Modifier.width(width = 4.dp))

                                            dialoguePhrase.phrase.audioAssetUrl?.let { url ->
                                                Spacer(modifier = Modifier.height(height = 12.dp))
                                                ListenButton(
                                                    size = 40.dp,
                                                    testTag = "listenPhraseButton_${dialoguePhrase.phrase.id}"
                                                ) {
                                                    viewModel.playAudio(url)
                                                }
                                            }
                                        }

                                        // Phrase Transcription
                                        Text(
                                            text = dialoguePhrase.phrase.transcription,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .padding(bottom = 8.dp)
                                                .testTag("phraseTranscription_${dialoguePhrase.phrase.id}")
                                        )

                                        // Phrase Translations
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
                                }
                            }
                        }
                    }
                }
            }
            PrimaryVerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState))
        }
    }
}

@Composable
private fun DialogueTitle(
    dialogue: GetDialogueResponse,
    viewModel: DialogueViewModel
) {
    Row {
        Text(
            text = dialogue.title.phrase,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp).testTag("detailTitleText")
        )

        Spacer(modifier = Modifier.width(5.dp))

        dialogue.title.audioAssetUrl?.let { url ->
            ListenButton(
                size = 40.dp,
                testTag = "listenTitleButton_${dialogue.id}"
            ) {
                viewModel.playAudio(url)
            }
        }
    }

    Text(
        text = dialogue.title.transcription,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp).testTag("detailTranscriptionText")
    )

    dialogue.title.translations.forEach { translation ->
        Text(
            text = translation.translationText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
