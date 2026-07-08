package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.back_button_text
import armenianlearningassistant_kmp.shared.generated.resources.error_prefix
import armenianlearningassistant_kmp.shared.generated.resources.no_results_found
import armenianlearningassistant_kmp.shared.generated.resources.search_dialogues_placeholder
import armenianlearningassistant_kmp.shared.generated.resources.search_results_title
import armenianlearningassistant_kmp.shared.generated.resources.view_full_dialogue_button
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.SearchState
import com.blbulyandavbulyan.larm.kmp.ui.common.ListenButton
import com.blbulyandavbulyan.larm.kmp.ui.common.PrimaryVerticalScrollbar
import com.blbulyandavbulyan.larm.kmp.ui.common.SearchField
import com.blbulyandavbulyan.larm.kmp.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogueSearchScreen(viewModel: DialogueViewModel, onBack: () -> Unit) {
    val searchState by viewModel.searchState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    val appColors = AppTheme.colors
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(appColors.gradientTop, appColors.gradientBottom)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // Search Bar with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack, modifier = Modifier.testTag("backButton")) {
                    Text(stringResource(Res.string.back_button_text))
                }
                Spacer(modifier = Modifier.width(8.dp))
                SearchField(
                    query = query,
                    textFieldModifier = Modifier.weight(1f).height(height = 60.dp).testTag("searchInputField"),
                    onSearch = { viewModel.searchDialogues(query) },
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text(stringResource(Res.string.search_dialogues_placeholder)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results Area
            when (val state = searchState) {
                is SearchState.Initial -> {
                    // empty state
                }

                is SearchState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is SearchState.Error -> {
                    Text(
                        text = "${stringResource(Res.string.error_prefix)} ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is SearchState.Success -> {
                    if (state.results.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.no_results_found),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        val scrollState = rememberScrollState()
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.search_results_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                state.results.forEach { dialogue ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .testTag("searchResultCard_${dialogue.id}"),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row {
                                                Text(
                                                    text = dialogue.title.phrase,
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.testTag("searchResultPhrase_${dialogue.id}")
                                                )

                                                Spacer(modifier = Modifier.width(5.dp))

                                                dialogue.title.audioAssetUrl?.let { url ->
                                                    ListenButton(
                                                        size = 40.dp,
                                                        testTag = "listenButton_${dialogue.id}"
                                                    ) { viewModel.playAudio(url) }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = dialogue.title.transcription,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.testTag("searchResultTranscription_${dialogue.id}")
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Translations
                                            dialogue.title.translations.forEach { translation ->
                                                Box(
                                                    modifier = Modifier
                                                        .padding(vertical = 2.dp)
                                                        .background(
                                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                                            shape = RoundedCornerShape(
                                                                8.dp
                                                            )
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = translation.translationText,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Action Buttons
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Distinct View Full Button
                                                Button(
                                                    onClick = { viewModel.onDialogueSelected(dialogue.id) },
                                                    modifier = Modifier.testTag(
                                                        "viewFullDialogueButton_${dialogue.id}"
                                                    ),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.secondary,
                                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                                    )
                                                ) {
                                                    Text(stringResource(Res.string.view_full_dialogue_button))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            PrimaryVerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState))
                        }
                    }
                }
            }
        }
    }
}
