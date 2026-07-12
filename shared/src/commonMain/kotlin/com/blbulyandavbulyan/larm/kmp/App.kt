package com.blbulyandavbulyan.larm.kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.blbulyandavbulyan.larm.kmp.di.AppModule
import com.blbulyandavbulyan.larm.kmp.presentation.DialogueViewModel
import com.blbulyandavbulyan.larm.kmp.ui.DialogueGeneratorScreen
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme

@Composable
@Preview
fun App() {
    ArmenianLearningTheme {
        // ViewModel should ideally be injected or created by a factory
        // Using remember here is acceptable for ViewModel instance caching until a formal DI library is used
        val viewModel = remember { DialogueViewModel(AppModule.dialogueRepository) }

        DialogueGeneratorScreen(viewModel)
    }
}
