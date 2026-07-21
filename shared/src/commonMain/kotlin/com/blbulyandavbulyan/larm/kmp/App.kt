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
import com.blbulyandavbulyan.larm.kmp.core.error.AppError
import com.blbulyandavbulyan.larm.kmp.di.AppModule
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueChatViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.DialogueSearchViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.global.AppViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.global.ScreenState
import com.blbulyandavbulyan.larm.kmp.ui.common.ErrorBanner
import com.blbulyandavbulyan.larm.kmp.ui.common.LoadingIndicator
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

        Content(currentScreen, chatViewModel, appViewModel, searchViewModel, appError)
    }
}

@Composable
private fun Content(
    currentScreen: ScreenState,
    chatViewModel: DialogueChatViewModel,
    appViewModel: AppViewModel,
    searchViewModel: DialogueSearchViewModel,
    appError: AppError?
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentScreen) { state ->
            when (state) {
                is ScreenState.Generator -> {
                    DialogueGeneratorScreen(
                        viewModel = chatViewModel,
                        onNavigateToSearch = { query ->
                            if (query.isNotBlank()) {
                                appViewModel.navigateToLoading()
                                searchViewModel.updateSearchQuery(query)
                                searchViewModel.searchDialogues(
                                    query = query,
                                    onSuccess = appViewModel::navigateToSearch,
                                    onError = appViewModel::navigateToGenerator
                                )
                            }
                        }
                    )
                }

                is ScreenState.Loading -> LoadingIndicator()

                is ScreenState.Search -> {
                    DialogueSearchScreen(
                        viewModel = searchViewModel,
                        onBack = appViewModel::navigateToGenerator,
                        onNavigateToDetail = appViewModel::navigateToDetail
                    )
                }

                is ScreenState.Detail -> {
                    DialogueDetailScreen(
                        dialogue = state.dialogue,
                        onBack = appViewModel::navigateToSearch,
                        onPlayAudio = searchViewModel::playAudio
                    )
                }
            }
        }

        OptionalErrorBanner(appError)
    }
}

@Composable
private fun OptionalErrorBanner(appError: AppError?) {
    appError?.let { error ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            println("Error handler got error $error")
            ErrorBanner(
                errorTitle = error.title.asString(),
                errorMessage = error.message.asString(),
                onDismiss = { AppModule.globalErrorManager.dismissError() }
            )
        }
    }
}
