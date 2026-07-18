package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.listen_phrase_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun ListenButton(
    size: Dp = 24.dp,
    testTag: String = "listenButton",
    onClick: () -> Unit
) {
    val text = stringResource(Res.string.listen_phrase_button)

    Box(
        modifier = Modifier
            .padding(start = 6.dp)
            .size(size)
            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        ListenIcon(text, Modifier.fillMaxSize().padding(4.dp))
    }
}

