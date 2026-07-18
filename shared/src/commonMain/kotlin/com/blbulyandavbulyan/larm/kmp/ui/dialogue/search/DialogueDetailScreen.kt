package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.back_button_text
import armenianlearningassistant_kmp.shared.generated.resources.unknown_speaker
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialoguePhraseResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueSpeakerResponse
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueViewModel
import com.blbulyandavbulyan.larm.kmp.ui.common.ListenIcon
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
                DialogueTitle(dialogue = dialogue) { audioUrl ->
                    viewModel.playAudio(audioUrl)
                }

                Spacer(modifier = Modifier.height(height = 16.dp))

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
                            // ==========================================
                            // LEFT COLUMN: SPEAKER INFO
                            // ==========================================
                            SpeakerInfo(
                                speaker = speaker,
                                modifier = Modifier.weight(weight = 0.35f),
                                onPlayAudio = viewModel::playAudio
                            )

                            // ==========================================
                            // RIGHT COLUMN: PHRASE BUBBLE & BUTTON
                            // ==========================================
                            PhraseInfo(
                                dialoguePhrase = dialoguePhrase,
                                modifier = Modifier.weight(weight = 0.65f),
                                onPlayAudio = viewModel::playAudio
                            )
                        }
                    }
                }
            }
            PrimaryVerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState))
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

        ChatBubbleBox(testTag = "listenPhraseButton_${dialoguePhrase.phrase.id}", onClick = { phraseAudio?.let { onPlayAudio(it) } }) {
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

@Composable
private fun SpeakerInfo(
    speaker: GetDialogueSpeakerResponse?,
    modifier: Modifier,
    onPlayAudio: (String) -> Unit
) {
    if (speaker == null) {
        Text(text = stringResource(Res.string.unknown_speaker), modifier = modifier)
        return
    }

    val speakerShape = RoundedTopLeftCutShape(cornerRadius = 24f, cutOffset = 35f)

    // Wrap the speaker info inside a stylized container box
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
            // Speaker Name & Small Audio Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                if (audioAssetUrl != null) {
                    ListenIcon(modifier = Modifier.size(size = 40.dp).padding(4.dp))
                    Spacer(modifier = Modifier.width(width = 10.dp))
                }

                Text(
                    text = speaker.name.phrase,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("speakerName_${speaker.id}")
                )
            }

            // Speaker Transcription
            Text(
                text = "(${speaker.name.transcription})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("speakerTranscription_${speaker.id}")
            )

            // Speaker Translations
            speaker.name.translations.forEach { translation ->
                Text(
                    text = translation.translationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DialogueTitle(
    dialogue: GetDialogueResponse,
    onPlayAudio: (String) -> Unit
) {
    val headerShape = RoundedCutBottomRightShape(
        cornerRadius = 24f, // Smooth rounding
        cutOffset = 50f     // Sharp accent cut
    )

    val audioAssetUrl = dialogue.title.audioAssetUrl
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
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (audioAssetUrl != null) {
                    ListenIcon(modifier = Modifier.size(size = 40.dp).padding(4.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(width = 10.dp))
                }
                Text(
                    text = dialogue.title.phrase,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp).testTag("detailTitleText")
                )
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
    }
}
