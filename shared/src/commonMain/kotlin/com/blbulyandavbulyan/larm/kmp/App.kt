package com.blbulyandavbulyan.larm.kmp

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blbulyandavbulyan.larm.kmp.di.AppModule
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.ScreenState
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.chat.DialogueGeneratorScreen
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.detail.DialogueDetailScreen
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.search.DialogueSearchScreen
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme

@Composable
fun App(
    viewModel: DialogueViewModel = remember {
        DialogueViewModel(
            AppModule.dialogueRepository,
            AppModule.audioRepository
        )
    }
) {
    ArmenianLearningTheme {
        val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

        Crossfade(targetState = currentScreen) { state ->
            when (state) {
                is ScreenState.Generator -> {
                    DialogueGeneratorScreen(
                        viewModel = viewModel,
                        onNavigateToSearch = { query ->
                            if (query.isNotBlank()) {
                                viewModel.searchDialogues(query)
                            }
                            viewModel.navigateToSearch()
                        }
                    )
                }
                is ScreenState.Search -> {
                    DialogueSearchScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateToGenerator() }
                    )
                }
                is ScreenState.Detail -> {
                    DialogueDetailScreen(
                        dialogue = state.dialogue,
                        onBack = { viewModel.navigateToSearch() },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
