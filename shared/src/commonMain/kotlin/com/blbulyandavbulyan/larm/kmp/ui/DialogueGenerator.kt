package com.blbulyandavbulyan.larm.kmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import armenianlearningassistant_kmp.shared.generated.resources.*
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogueGeneratorScreen(viewModel: DialogueViewModel) {
    var prompt by remember { mutableStateOf("") }
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()

    val notoArmenian = FontFamily(Font(Res.font.noto_sans_armenian))

    fun triggerGeneration() {
        if (prompt.isNotBlank()) {
            viewModel.generateDialogue(prompt)
            prompt = ""
        }
    }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A2E),
            Color(0xFF16213E)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(16.dp)
    ) {
        // Header
        Header()

        // Main content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(16.dp)
        ) {
            if (conversation.isEmpty()) {
                EmptyConversationScreen()
            } else {
                ConversationScreen(conversation, notoArmenian)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InputMessageField(value = prompt, fontFamily = notoArmenian, onValueChange = { prompt = it }) {
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
private fun RowScope.InputMessageField(value: String, fontFamily: FontFamily, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.weight(1f)
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
                color = Color.Gray
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFE94560),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFFE94560)
        ),
        shape = RoundedCornerShape(24.dp),
        maxLines = 3,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily)
    )
}

@Composable
private fun BoxScope.EmptyConversationScreen() {
    Text(
        text = stringResource(Res.string.empty_conversation_message),
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White.copy(alpha = 0.5f),
        modifier = Modifier.align(Alignment.Center)
    )
}

@Composable
private fun ConversationScreen(
    conversation: List<ConversationItem>,
    notoArmenian: FontFamily
) {
    SelectionContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(conversation) { item ->
                when (item) {
                    is ConversationItem.UserMessage -> {
                        UserMessageView(item.text, notoArmenian)
                    }

                    is ConversationItem.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFE94560))
                        }
                    }

                    is ConversationItem.Error -> {
                        Text(
                            text = "${stringResource(Res.string.error_prefix)} ${item.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }

                    is ConversationItem.AiResponse -> {
                        DialogueView(item.response, notoArmenian)
                    }
                }
            }
        }
    }
}

@Composable
private fun SendButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE94560),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(stringResource(Res.string.action_send), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Header() {
    Text(
        text = stringResource(Res.string.app_name),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        text = stringResource(Res.string.header_subtitle),
        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFA0A0B0)),
        modifier = Modifier.padding(bottom = 24.dp)
    )
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
                containerColor = Color(0xFFE94560).copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontFamily = fontFamily
                )
            )
        }
    }
}

