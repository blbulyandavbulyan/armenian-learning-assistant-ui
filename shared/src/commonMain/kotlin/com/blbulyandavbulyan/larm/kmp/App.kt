package com.blbulyandavbulyan.larm.kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.blbulyandavbulyan.larm.kmp.di.AppModule
import com.blbulyandavbulyan.larm.kmp.network.TokenStorage
import com.blbulyandavbulyan.larm.kmp.presentation.AuthState
import com.blbulyandavbulyan.larm.kmp.presentation.AuthViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.DialogueViewModel
import com.blbulyandavbulyan.larm.kmp.ui.DialogueGeneratorScreen
import com.blbulyandavbulyan.larm.kmp.ui.LoginScreen
import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider

@Composable
@Preview
fun App() {
    ArmenianLearningTheme {
        // Initialize KMPAuth Google Provider
        // Make sure to replace "YOUR_CLIENT_ID" with your actual Web Client ID from Google Cloud Console
        GoogleAuthProvider.create(
            // TODO probably hardcoding it is bad
            credentials = GoogleAuthCredentials(serverId = "250390037853-a7tr19hv74iiecponie798fdr0pknujs.apps.googleusercontent.com")
        )

        val authViewModel = remember { AuthViewModel() }
        val authState by authViewModel.authState.collectAsState()

        when (val state = authState) {
            is AuthState.Unauthenticated -> {
                LoginScreen(
                    onTokenReceived = { jwtToken ->
                        authViewModel.onTokenReceived(jwtToken)
                    }
                )
            }
            is AuthState.Authenticated -> {
                // Update the TokenStorage securely configured in Ktor's Auth plugin
                TokenStorage.jwtToken = state.jwtToken
                
                val dialogueViewModel = remember { DialogueViewModel(AppModule.dialogueRepository) }
                DialogueGeneratorScreen(dialogueViewModel)
            }
        }
    }
}
