package com.blbulyandavbulyan.larm.kmp.ui.dialogue.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.audio_playback_error_title
import armenianlearningassistant_kmp.shared.generated.resources.dismiss_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun AudioErrorBanner(
    errorMessage: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    Snackbar(
        action = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("DismissAudioErrorButton")) {
                Text(text = stringResource(Res.string.dismiss_button))
            }
        },
        modifier = modifier.padding(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(Res.string.audio_playback_error_title),
                fontWeight = FontWeight.Bold
            )
            Text(text = errorMessage)
        }
    }
}
