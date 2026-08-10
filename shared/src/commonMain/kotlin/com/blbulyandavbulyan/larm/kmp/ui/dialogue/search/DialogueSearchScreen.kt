package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.error_failed_to_search_dialogues
import armenianlearningassistant_kmp.shared.generated.resources.no_results_found
import armenianlearningassistant_kmp.shared.generated.resources.retry_button
import armenianlearningassistant_kmp.shared.generated.resources.search_results_title
import armenianlearningassistant_kmp.shared.generated.resources.view_dialogue_details
import armenianlearningassistant_kmp.shared.generated.resources.view_full_dialogue_button
import com.blbulyandavbulyan.larm.kmp.domain.dialogue.model.search.DialogueSummary
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.DialogueSearchViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.search.SearchState
import com.blbulyandavbulyan.larm.kmp.ui.common.LoadingIndicator
import com.blbulyandavbulyan.larm.kmp.ui.common.PrimaryVerticalScrollbar
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.common.DialogueTitle
import com.blbulyandavbulyan.larm.kmp.ui.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogueSearchScreen(
    viewModel: DialogueSearchViewModel,
    modifier: Modifier = Modifier,
    onGetDialogueDetails: (String) -> Unit
) {
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()

    DialogueSearchScreen(
        searchState = searchState,
        modifier = modifier,
        onSearch = {
            viewModel.searchDialogues(
                query = query,
                onSuccess = {},
                onError = {}
            )
        },
        onGetDialogueDetails = onGetDialogueDetails,
        onPlayAudio = viewModel::playAudio
    )
}

@Composable
fun DialogueSearchScreen(
    searchState: SearchState,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit,
    onGetDialogueDetails: (String) -> Unit,
    onPlayAudio: (String) -> Unit
) {
    val appColors = AppTheme.colors
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(appColors.gradientTop, appColors.gradientBottom)
    )

    Box(
        modifier = modifier.testTag("dialogueSearchScreen")
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
            SearchResultsContent(
                searchState = searchState,
                onGetDialogueDetails = onGetDialogueDetails,
                onPlayAudio = onPlayAudio,
                onSearch = onSearch
            )
        }
    }
}

@Composable
private fun SearchResultsContent(
    searchState: SearchState,
    onGetDialogueDetails: (String) -> Unit,
    onPlayAudio: (String) -> Unit,
    onSearch: () -> Unit
) {
    when (searchState) {
        is SearchState.Initial -> {
            // empty state
        }

        is SearchState.Loading -> LoadingIndicator()

        is SearchState.Success -> {
            DialogueSearchResults(
                state = searchState,
                onGetDialogueDetails = onGetDialogueDetails,
                onPlayAudio = onPlayAudio
            )
        }

        SearchState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.error_failed_to_search_dialogues),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSearch,
                    modifier = Modifier.testTag("retryButton")
                ) {
                    Text(stringResource(Res.string.retry_button))
                }
            }
        }
    }
}

@Composable
private fun DialogueSearchResults(
    state: SearchState.Success,
    onGetDialogueDetails: (dialogueId: String) -> Unit,
    onPlayAudio: (String) -> Unit
) {
    if (state.results.isEmpty()) {
        Text(
            modifier = Modifier.testTag("emptyResultsMessage"),
            text = stringResource(Res.string.no_results_found),
            style = MaterialTheme.typography.bodyLarge
        )
        return
    }

    val scrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(end = 12.dp)
        ) {
            SearchResultsTitle()
            Spacer(modifier = Modifier.height(8.dp))
            state.results.forEach { dialogue ->
                androidx.compose.runtime.key(dialogue.id) {
                    DialogueSearchResult(
                        dialogue = dialogue,
                        onGetDialogueDetails = onGetDialogueDetails,
                        onPlayAudio = onPlayAudio
                    )
                }
            }
        }
        PrimaryVerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState))
    }
}

@Composable
private fun DialogueSearchResult(
    dialogue: DialogueSummary,
    onGetDialogueDetails: (dialogueId: String) -> Unit,
    onPlayAudio: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("searchResultCard_${dialogue.id}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .padding(all = 16.dp)
            ) {
                DialogueTitle(
                    dialogueTitle = dialogue.title,
                    testTag = "listenButton_${dialogue.id}",
                    phraseTestTag = "searchResultPhrase_${dialogue.id}",
                    transcriptionTestTag = "searchResultTranscription_${dialogue.id}",
                    onPlayAudio = onPlayAudio
                )
            }

            ViewDialogueDetailsButton(dialogue, onGetDialogueDetails)
        }
    }
}

@Composable
private fun ViewDialogueDetailsButton(
    dialogue: DialogueSummary,
    onGetDialogueDetails: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .width(width = 36.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.secondary)
            .clickable { onGetDialogueDetails(dialogue.id) }
            .testTag("viewFullDialogueButton_${dialogue.id}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.view_dialogue_details),
            contentDescription = stringResource(Res.string.view_full_dialogue_button),
            modifier = Modifier.fillMaxHeight().width(width = 30.dp),
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}

@Composable
private fun SearchResultsTitle() {
    Text(
        text = stringResource(Res.string.search_results_title),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}
