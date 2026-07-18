package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueViewModel
import com.blbulyandavbulyan.larm.kmp.ui.common.GoBackButton
import com.blbulyandavbulyan.larm.kmp.ui.common.ListenIcon
import com.blbulyandavbulyan.larm.kmp.ui.common.PrimaryVerticalScrollbar
import com.blbulyandavbulyan.larm.kmp.ui.common.RoundedCutBottomRightShape
import com.blbulyandavbulyan.larm.kmp.ui.theme.AppTheme

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
                GoBackButton(width = 50.dp, height = 50.dp, onClick = onBack)
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

                DialoguePhrases(dialogue = dialogue, onPlayAudio = viewModel::playAudio)
            }
            PrimaryVerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState))
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
        cutOffset = 50f // Sharp accent cut
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
                    ListenIcon(
                        modifier = Modifier.size(size = 40.dp)
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
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
