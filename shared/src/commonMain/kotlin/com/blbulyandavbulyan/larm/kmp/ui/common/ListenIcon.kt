package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.ic_text_to_speech_24px
import org.jetbrains.compose.resources.painterResource

@Composable
fun ListenIcon(
    contentDescription: String? = null,
    modifier: Modifier,
    tint: Color = MaterialTheme.colorScheme.onPrimary
) {
    Icon(
        painter = painterResource(Res.drawable.ic_text_to_speech_24px),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}
