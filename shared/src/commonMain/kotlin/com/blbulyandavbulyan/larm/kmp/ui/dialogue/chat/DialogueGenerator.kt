package com.blbulyandavbulyan.larm.kmp.ui.dialogue.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.action_send
import armenianlearningassistant_kmp.shared.generated.resources.app_name
import armenianlearningassistant_kmp.shared.generated.resources.empty_conversation_message
import armenianlearningassistant_kmp.shared.generated.resources.header_subtitle
import armenianlearningassistant_kmp.shared.generated.resources.input_placeholder
import armenianlearningassistant_kmp.shared.generated.resources.noto_sans_armenian
import armenianlearningassistant_kmp.shared.generated.resources.search_dialogues_placeholder
import com.blbulyandavbulyan.larm.kmp.data.dialogue.chat.DialogueChatResponse
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.ConversationItem
import com.blbulyandavbulyan.larm.kmp.presentation.dialogue.chat.DialogueChatViewModel
import com.blbulyandavbulyan.larm.kmp.ui.common.PrimaryVerticalScrollbar
import com.blbulyandavbulyan.larm.kmp.ui.common.SearchField
import com.blbulyandavbulyan.larm.kmp.ui.theme.AppTheme
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogueGeneratorScreen(
    viewModel: DialogueChatViewModel,
    onNavigateToSearch: (String) -> Unit
) {
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    DialogueGeneratorScreen(
        conversation = conversation,
        onGenerateDialogue = viewModel::generateDialogue,
        onSaveDialogue = viewModel::saveDialogue,
        onNavigateToSearch = onNavigateToSearch
    )
}

@Composable
fun DialogueGeneratorScreen(
    conversation: List<ConversationItem>,
    emptyMessage: String = stringResource(Res.string.empty_conversation_message),
    onGenerateDialogue: (String) -> Unit,
    onSaveDialogue: (DialogueChatResponse) -> Unit,
    onNavigateToSearch: (String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }

    val notoArmenian = FontFamily(Font(Res.font.noto_sans_armenian))

    fun triggerGeneration() {
        if (prompt.isNotBlank()) {
            onGenerateDialogue(prompt)
            prompt = ""
        }
    }

    val appColors = AppTheme.colors
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            appColors.gradientTop,
            appColors.gradientBottom
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(16.dp)
            .testTag("dialogueGeneratorScreen")
    ) {
        // Header
        Header(onNavigateToSearch = onNavigateToSearch)

        // Main content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(appColors.innerBoxBackground)
                .padding(16.dp)
        ) {
            if (conversation.isEmpty()) {
                EmptyConversationScreen(emptyMessage)
            } else {
                ConversationScreen(conversation, notoArmenian, onSaveDialogue)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InputMessageField(
                value = prompt,
                fontFamily = notoArmenian,
                modifier = Modifier.weight(1f),
                onValueChange = { prompt = it }
            ) {
                triggerGeneration()
            }

            Spacer(modifier = Modifier.width(12.dp))

            SendButton {
                triggerGeneration()
            }
        }
    }
}

@Composable
internal fun InputMessageField(
    value: String,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .testTag("inputMessageField")
            .onPreviewKeyEvent { keyEvent ->
                // If they press Enter without Shift, send the message
                if ((keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) && !keyEvent.isShiftPressed) {
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        onSend()
                        return@onPreviewKeyEvent true
                    } else if (keyEvent.type == KeyEventType.KeyUp) {
                        return@onPreviewKeyEvent true
                    }
                }
                false
            },
        placeholder = {
            Text(
                stringResource(Res.string.input_placeholder),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = AppTheme.colors.unfocusedBorder,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(24.dp),
        maxLines = 3,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily)
    )
}

@Composable
private fun BoxScope.EmptyConversationScreen(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = AppTheme.colors.emptyMessage,
        modifier = Modifier.align(Alignment.Center).testTag("emptyConversationText")
    )
}

@Composable
private fun ConversationScreen(
    conversation: List<ConversationItem>,
    notoArmenian: FontFamily,
    onSaveDialogue: (DialogueChatResponse) -> Unit
) {
    val scrollState = rememberScrollState()

    SelectionContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("conversationScreen")
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp, end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (item in conversation) {
                    when (item) {
                        is ConversationItem.UserMessage -> {
                            UserMessageView(item.text, notoArmenian)
                        }

                        is ConversationItem.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("loadingIndicator"),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        is ConversationItem.AiResponse -> {
                            GeneratedDialogueView(
                                dialogue = item.response,
                                fontFamily = notoArmenian,
                                isSaving = item.isSaving,
                                isSaved = item.isSaved,
                                onSaveClick = { onSaveDialogue(item.response) }
                            )
                        }
                    }
                }
            }

            PrimaryVerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState))
        }
    }
}

@Composable
private fun SendButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        modifier = Modifier.testTag("sendButton")
    ) {
        Text(stringResource(Res.string.action_send), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Header(onNavigateToSearch: (String) -> Unit) {
    var query by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(Res.string.header_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        SearchField(
            query = query,
            Modifier.weight(1f).height(height = 60.dp),
            onSearch = { onNavigateToSearch(query) },
            onValueChange = { query = it },
            placeholder = { Text(stringResource(Res.string.search_dialogues_placeholder)) }
        )
    }
}

@Composable
fun UserMessageView(text: String, fontFamily: FontFamily) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomEnd = 4.dp,
                bottomStart = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth(fraction = 0.85f)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp).testTag("userMessageText"),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontFamily = fontFamily
                )
            )
        }
    }
}
