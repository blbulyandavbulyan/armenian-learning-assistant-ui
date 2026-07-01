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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import armenianlearningassistant_kmp.shared.generated.resources.*
import com.blbulyandavbulyan.larm.kmp.data.DialogueChatResponse
import org.jetbrains.compose.resources.Font

@Composable
fun DialogueGeneratorScreen(viewModel: DialogueViewModel) {
    var prompt by remember { mutableStateOf("") }
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()

    val notoArmenian = FontFamily(Font(Res.font.noto_sans_armenian))

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
        Text(
            text = "Dialogue Generator",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Generate premium Armenian dialogues using AI",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFA0A0B0)),
            modifier = Modifier.padding(bottom = 24.dp)
        )

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
                Text(
                    text = "No dialogue generated yet. Ask me to create one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
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
                                        text = "Error: ${item.message}",
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier.weight(1f)
                    .onPreviewKeyEvent {
                        if (it.key == Key.Enter || it.key == Key.NumPadEnter) {
                            // If they press Enter without Shift, send the message
                            if (!it.isShiftPressed) {
                                if (it.type == KeyEventType.KeyDown) {
                                    if (prompt.isNotBlank()) {
                                        viewModel.generateDialogue(prompt)
                                        prompt = ""
                                    }
                                    return@onPreviewKeyEvent true
                                } else if (it.type == KeyEventType.KeyUp) {
                                    return@onPreviewKeyEvent true
                                }
                            }
                        }
                        false
                    },
                placeholder = {
                    Text(
                        "E.g., I want to go to the grocery store... (Enter to send, Shift+Enter for new line)",
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
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = notoArmenian)
            )

            Spacer(modifier = Modifier.width(12.dp))

            FloatingActionButton(
                onClick = {
                    if (prompt.isNotBlank()) {
                        viewModel.generateDialogue(prompt)
                        prompt = ""
                    }
                },
                containerColor = Color(0xFFE94560),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Send", fontWeight = FontWeight.Bold)
            }
        }
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

@Composable
fun DialogueView(dialogue: DialogueChatResponse, fontFamily: FontFamily) {
    val speakersMap = dialogue.speakers.associateBy { it.id }

    Column(modifier = Modifier.fillMaxWidth()) {
        // AI's message bubble
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp, 
                    topEnd = 16.dp, 
                    bottomEnd = 16.dp, 
                    bottomStart = 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth(0.85f).padding(bottom = 16.dp)
            ) {
                Text(
                    text = dialogue.message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontFamily = fontFamily
                    )
                )
            }
        }

        // Dialogue structured content
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val titleTranslations = dialogue.info.translations.joinToString(" | ") { it.translationText }
                val titleText = if (titleTranslations.isNotEmpty()) "${dialogue.info.title} | $titleTranslations" else dialogue.info.title

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = fontFamily
                    )
                )
                Text(
                    text = dialogue.info.transcription,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFA0A0B0))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            dialogue.dialoguePhrases.forEach { phraseObj ->
                val speaker = speakersMap[phraseObj.speakerId]
                Card(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 4.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F3460).copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val speakerText = speaker?.let { spk ->
                            val translations = spk.translations.joinToString(" | ") { it.translationText }
                            if (translations.isNotEmpty()) "${spk.title} | $translations" else spk.title
                        } ?: "Unknown"

                        Text(
                            text = speakerText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFFE94560),
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = phraseObj.phrase.phrase,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = fontFamily
                            )
                        )
                        Text(
                            text = phraseObj.phrase.transcription,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFA0A0B0),
                                fontStyle = FontStyle.Italic
                            )
                        )

                        val phraseTranslations = phraseObj.phrase.translations.joinToString(" | ") { it.translationText }
                        if (phraseTranslations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = phraseTranslations,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF888899))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { /* TODO: Save functionality for later branch */ },
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3460)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Save Dialogue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
