package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.error_prefix
import armenianlearningassistant_kmp.shared.generated.resources.no_results_found
import armenianlearningassistant_kmp.shared.generated.resources.search_dialogues_placeholder
import armenianlearningassistant_kmp.shared.generated.resources.search_results_title
import armenianlearningassistant_kmp.shared.generated.resources.view_dialogue_details
import armenianlearningassistant_kmp.shared.generated.resources.view_full_dialogue_button
import com.blbulyandavbulyan.larm.kmp.data.dialogue.search.DialogueSummaryResponse
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueViewModel
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.SearchState
import com.blbulyandavbulyan.larm.kmp.ui.common.GoBackButton
import com.blbulyandavbulyan.larm.kmp.ui.common.PrimaryVerticalScrollbar
import com.blbulyandavbulyan.larm.kmp.ui.common.SearchField
import com.blbulyandavbulyan.larm.kmp.ui.dialogue.common.DialogueTitle
import com.blbulyandavbulyan.larm.kmp.ui.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
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
            SearchBarAndBackButton(
                query = query,
                onBack = onBack,
                onValueChange = {
                    viewModel.updateSearchQuery(it)
                },
                onSearch = {
                    viewModel.searchDialogues(query)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results Area
            when (val state = searchState) {
                is SearchState.Initial -> {
                    // empty state
                }

                is SearchState.Loading -> LoadingIndicator()

                is SearchState.Error -> GlobalError(state)

                is SearchState.Success -> {
                    DialogueSearchResults(
                        state = state,
                        onGetDialogueDetails = viewModel::onDialogueSelected,
                        onPlayAudio = viewModel::playAudio
                    )
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
                DialogueSearchResult(
                    dialogue = dialogue,
                    onGetDialogueDetails = onGetDialogueDetails,
                    onPlayAudio = onPlayAudio
                )
            }
        }
        PrimaryVerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState))
    }
}

@Composable
private fun DialogueSearchResult(
    dialogue: DialogueSummaryResponse,
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
                DialogueTitle(dialogueTitle = dialogue.title, onPlayAudio = onPlayAudio)
            }

            ViewDialogueDetailsButton(dialogue, onGetDialogueDetails)
        }
    }
}

@Composable
private fun ViewDialogueDetailsButton(
    dialogue: DialogueSummaryResponse,
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

@Composable
private fun GlobalError(state: SearchState.Error) {
    Text(
        text = "${stringResource(Res.string.error_prefix)} ${state.message}",
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SearchBarAndBackButton(
    query: String,
    onBack: () -> Unit,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GoBackButton(width = 50.dp, height = 50.dp, onClick = onBack)
        Spacer(modifier = Modifier.width(8.dp))
        SearchField(
            query = query,
            textFieldModifier = Modifier.weight(1f).height(height = 60.dp).testTag("searchInputField"),
            onSearch = onSearch,
            onValueChange = onValueChange,
            placeholder = { Text(stringResource(Res.string.search_dialogues_placeholder)) }
        )
    }
}
