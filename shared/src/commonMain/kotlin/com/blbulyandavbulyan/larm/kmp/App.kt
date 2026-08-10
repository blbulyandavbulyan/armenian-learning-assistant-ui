package com.blbulyandavbulyan.larm.kmp

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.search_dialogues_placeholder
import com.blbulyandavbulyan.larm.kmp.di.AppModule
import com.blbulyandavbulyan.larm.kmp.presentation.auth.LoginViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueChatViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.DialogueSearchViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.drawer.DrawerViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.global.AppRouterViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.global.ScreenState
import com.blbulyandavbulyan.larm.kmp.ui.auth.LoginScreen
import com.blbulyandavbulyan.larm.kmp.ui.common.AppDrawerContent
import com.blbulyandavbulyan.larm.kmp.ui.common.AppTopBar
import com.blbulyandavbulyan.larm.kmp.ui.common.LoadingIndicator
import com.blbulyandavbulyan.larm.kmp.ui.common.OptionalErrorBanner
import com.blbulyandavbulyan.larm.kmp.ui.common.SearchField
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.chat.DialogueGeneratorScreen
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.detail.DialogueDetailScreen
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.search.DialogueSearchScreen
import com.blbulyandavbulyan.larm.kmp.ui.theme.AppTheme
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun App(
    appRouterViewModel: AppRouterViewModel = remember { AppRouterViewModel() },
    drawerViewModel: DrawerViewModel = remember { DrawerViewModel(AppModule.authRepository) },
    searchViewModel: DialogueSearchViewModel = remember {
        DialogueSearchViewModel(
            AppModule.dialogueRepository,
            AppModule.assetRepository,
            AppModule.globalErrorManager,
            AppModule.audioPlayer
        )
    },
    chatViewModel: DialogueChatViewModel = remember {
        DialogueChatViewModel(
            AppModule.dialogueChatRepository,
            AppModule.globalErrorManager
        )
    },
    loginViewModel: LoginViewModel = remember {
        LoginViewModel(
            AppModule.authRepository,
            AppModule.globalErrorManager
        )
    }
) {
    ArmenianLearningTheme {
        Content(
            chatViewModel = chatViewModel,
            appRouterViewModel = appRouterViewModel,
            drawerViewModel = drawerViewModel,
            searchViewModel = searchViewModel,
            loginViewModel = loginViewModel,
        )
    }
}

@Composable
private fun Content(
    chatViewModel: DialogueChatViewModel,
    appRouterViewModel: AppRouterViewModel,
    drawerViewModel: DrawerViewModel,
    searchViewModel: DialogueSearchViewModel,
    loginViewModel: LoginViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by appRouterViewModel.currentScreen.collectAsStateWithLifecycle()
    val appError by AppModule.globalErrorManager.currentError.collectAsStateWithLifecycle()
    val appColors = AppTheme.colors
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(appColors.gradientTop, appColors.gradientBottom)
    )

    Box(modifier = modifier.fillMaxSize().background(gradientBackground)) {
        if (currentScreen is ScreenState.Login) {
            LoginScreen(viewModel = loginViewModel)
        } else {
            MainScaffold(
                currentScreen = currentScreen,
                chatViewModel = chatViewModel,
                appRouterViewModel = appRouterViewModel,
                drawerViewModel = drawerViewModel,
                searchViewModel = searchViewModel
            )
        }

        OptionalErrorBanner(appError)
    }
}

@Composable
private fun MainScaffold(
    currentScreen: ScreenState,
    chatViewModel: DialogueChatViewModel,
    appRouterViewModel: AppRouterViewModel,
    drawerViewModel: DrawerViewModel,
    searchViewModel: DialogueSearchViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val userProfile by drawerViewModel.userProfile.collectAsStateWithLifecycle()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userProfile = userProfile,
                currentScreen = currentScreen,
                onNavigateToGenerator = {
                    coroutineScope.launch { drawerState.close() }
                    appRouterViewModel.navigateToGenerator()
                },
                onSignOut = {
                    coroutineScope.launch { drawerState.close() }
                    drawerViewModel.signOut()
                }
            )
        },
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBarContainer(
                currentScreen = currentScreen,
                searchViewModel = searchViewModel,
                appRouterViewModel = appRouterViewModel,
                onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                ScreenNavigationContent(
                    currentScreen = currentScreen,
                    chatViewModel = chatViewModel,
                    appRouterViewModel = appRouterViewModel,
                    searchViewModel = searchViewModel
                )
            }
        }
    }
}

@Composable
private fun AppTopBarContainer(
    currentScreen: ScreenState,
    searchViewModel: DialogueSearchViewModel,
    appRouterViewModel: AppRouterViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backAction: (() -> Unit)? = when (currentScreen) {
        is ScreenState.Search -> appRouterViewModel::navigateToGenerator
        is ScreenState.Detail -> appRouterViewModel::navigateToSearch
        else -> null
    }

    AppTopBar(
        onOpenDrawer = onOpenDrawer,
        onBack = backAction,
        centerContent = {
            if (currentScreen is ScreenState.Generator || currentScreen is ScreenState.Search) {
                val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle()
                SearchField(
                    query = searchQuery,
                    onValueChange = searchViewModel::updateSearchQuery,
                    onSearch = {
                        appRouterViewModel.navigateToLoading()
                        searchViewModel.searchDialogues(
                            query = searchQuery,
                            onSuccess = appRouterViewModel::navigateToSearch,
                            onError = appRouterViewModel::navigateToSearch
                        )
                    },
                    placeholder = {
                        Text(stringResource(Res.string.search_dialogues_placeholder))
                    },
                    textFieldModifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("top_bar_search_field")
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ScreenNavigationContent(
    currentScreen: ScreenState,
    chatViewModel: DialogueChatViewModel,
    appRouterViewModel: AppRouterViewModel,
    searchViewModel: DialogueSearchViewModel
) {
    Crossfade(targetState = currentScreen) { state ->
        when (state) {
            is ScreenState.Generator -> DialogueGeneratorScreen(viewModel = chatViewModel)

            is ScreenState.Loading -> LoadingIndicator()

            is ScreenState.Search -> {
                DialogueSearchScreen(
                    viewModel = searchViewModel,
                    onGetDialogueDetails = { id ->
                        appRouterViewModel.navigateToLoading()
                        searchViewModel.displayDialogue(
                            id = id,
                            onDialogueReady = appRouterViewModel::navigateToDetail,
                            onError = appRouterViewModel::navigateToSearch
                        )
                    }
                )
            }

            is ScreenState.Detail -> {
                DialogueDetailScreen(
                    dialogue = state.dialogue,
                    onPlayAudio = searchViewModel::playAudio
                )
            }

            is ScreenState.Login -> {
                // Handled in outer condition
            }
        }
    }
}
