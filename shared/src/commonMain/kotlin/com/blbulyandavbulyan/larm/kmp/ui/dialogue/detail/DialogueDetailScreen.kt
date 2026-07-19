package com.blbulyandavbulyan.larm.kmp.ui.dialogue.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.GetDialogueResponse
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueViewModel
import com.blbulyandavbulyan.larm.kmp.ui.common.GoBackButton
import com.blbulyandavbulyan.larm.kmp.ui.common.PrimaryVerticalScrollbar
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.common.DialogueTitle
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.common.AudioErrorBanner
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
    val audioError by viewModel.audioError.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
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
                    DialogueTitle(
                        dialogueTitle = dialogue.title,
                        testTag = "listenTitleButton_${dialogue.id}"
                    ) { audioUrl ->
                        viewModel.playAudio(audioUrl)
                    }

                    Spacer(modifier = Modifier.height(height = 16.dp))

                    DialoguePhrases(dialogue = dialogue, onPlayAudio = viewModel::playAudio)
                }
                PrimaryVerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState))
            }
        }

        audioError?.let { errorMsg ->
            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding()) {
                AudioErrorBanner(errorMessage = errorMsg, onDismiss = viewModel::dismissAudioError)
            }
        }
    }
}
