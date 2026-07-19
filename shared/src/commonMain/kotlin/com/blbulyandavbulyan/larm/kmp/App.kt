package com.blbulyandavbulyan.larm.kmp

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blbulyandavbulyan.larm.kmp.di.AppModule
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueChatViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.DialogueSearchViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.global.AppViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.global.ScreenState
import com.blbulyandavbulyan.larm.kmp.ui.common.ErrorBanner
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.chat.DialogueGeneratorScreen
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.detail.DialogueDetailScreen
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.search.DialogueSearchScreen
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme

@Composable
fun App(
    appViewModel: AppViewModel = remember {
        AppViewModel()
    },
    searchViewModel: DialogueSearchViewModel = remember {
        DialogueSearchViewModel(
            AppModule.dialogueRepository,
            AppModule.audioRepository,
            AppModule.globalErrorManager
        )
    },
    chatViewModel: DialogueChatViewModel = remember {
        DialogueChatViewModel(
            AppModule.dialogueRepository,
            AppModule.globalErrorManager
        )
    }
) {
    ArmenianLearningTheme {
        val currentScreen by appViewModel.currentScreen.collectAsStateWithLifecycle()

        val appError by AppModule.globalErrorManager.currentError.collectAsStateWithLifecycle()

        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(targetState = currentScreen) { state ->
                when (state) {
                    is ScreenState.Generator -> {
                        DialogueGeneratorScreen(
                            viewModel = chatViewModel,
                            onNavigateToSearch = { query ->
                                if (query.isNotBlank()) {
                                    searchViewModel.updateSearchQuery(query)
                                    searchViewModel.searchDialogues(query)
                                }
                                appViewModel.navigateToSearch()
                            }
                        )
                    }
                    is ScreenState.Search -> {
                        DialogueSearchScreen(
                            viewModel = searchViewModel,
                            onBack = { appViewModel.navigateToGenerator() },
                            onNavigateToDetail = { appViewModel.navigateToDetail(it) }
                        )
                    }
                    is ScreenState.Detail -> {
                        DialogueDetailScreen(
                            dialogue = state.dialogue,
                            onBack = { appViewModel.navigateToSearch() },
                            onPlayAudio = searchViewModel::playAudio
                        )
                    }
                }
            }

            appError?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ErrorBanner(
                        errorTitle = error.title,
                        errorMessage = error.message,
                        onDismiss = { AppModule.globalErrorManager.dismissError() }
                    )
                }
            }
        }
    }
}
