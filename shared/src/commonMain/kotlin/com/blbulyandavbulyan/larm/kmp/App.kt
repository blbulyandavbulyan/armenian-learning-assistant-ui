package com.blbulyandavbulyan.larm.kmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.blbulyandavbulyan.larm.kmp.network.ApiClient
import com.blbulyandavbulyan.larm.kmp.network.NetworkDialogueRepository
import com.blbulyandavbulyan.larm.kmp.ui.DialogueGeneratorScreen
import com.blbulyandavbulyan.larm.kmp.presentation.DialogueViewModel

import com.blbulyandavbulyan.larm.kmp.ui.theme.ArmenianLearningTheme

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Composable
@Preview
fun App() {
    ArmenianLearningTheme {
        val httpClient = remember {
            HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    })
                }
                defaultRequest {
                    val baseUrl = BuildKonfig.BASE_URL
                    if (baseUrl.isNotBlank()) {
                        url(baseUrl)
                    }
                }
            }
        }
        val apiClient = remember { ApiClient(httpClient) }
        val repository = remember { NetworkDialogueRepository(apiClient) }
        val viewModel = remember { DialogueViewModel(repository) }
        
        DialogueGeneratorScreen(viewModel)
    }
}