package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.dismiss_button
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun ErrorBanner(
    errorTitle: String,
    errorMessage: String,
    modifier: Modifier = Modifier,
    displayDuration: Duration = 7.seconds, // 7 seconds fully visible
    fadeDuration: Duration = 3.seconds,      // 3 seconds fade out
    onDismiss: () -> Unit
) {
    // Create an animatable float for alpha starting at 1f (fully visible)
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(errorTitle, errorMessage) {
        // Reset alpha back to 1.0 immediately if a new error arrives
        alpha.snapTo(1f)

        // 1. Hold full visibility for 7 seconds
        delay(displayDuration)

        // 2. Animate alpha down to 0 over 3 seconds
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = fadeDuration.inWholeMilliseconds.toInt(),
                easing = LinearEasing
            )
        )

        // 3. Remove it from state/composition when invisible
        onDismiss()
    }

    Snackbar(
        action = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismissErrorBannerButton")
            ) {
                Text(text = stringResource(Res.string.dismiss_button))
            }
        },
        modifier = modifier
            .padding(16.dp)
            .alpha(alpha.value) // Applies the fading opacity
    ) {
        Column {
            Text(
                text = errorTitle,
                fontWeight = FontWeight.Bold
            )
            Text(text = errorMessage)
        }
    }
}