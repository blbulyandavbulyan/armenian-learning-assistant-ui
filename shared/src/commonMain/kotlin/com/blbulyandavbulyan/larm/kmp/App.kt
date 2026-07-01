package com.blbulyandavbulyan.larm.kmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.blbulyandavbulyan.larm.kmp.network.ApiClient
import com.blbulyandavbulyan.larm.kmp.network.NetworkDialogueRepository
import com.blbulyandavbulyan.larm.kmp.ui.DialogueGeneratorScreen
import com.blbulyandavbulyan.larm.kmp.ui.DialogueViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        val apiClient = remember { ApiClient(BuildKonfig.BASE_URL) }
        val repository = remember { NetworkDialogueRepository(apiClient) }
        val viewModel = remember { DialogueViewModel(repository) }
        
        DialogueGeneratorScreen(viewModel)
    }
}