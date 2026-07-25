package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.arrow_back_24px
import armenianlearningassistant_kmp.shared.generated.resources.back_button_text
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GoBackButton(
    width: Dp = 24.dp,
    height: Dp = 24.dp,
    testTag: String = "backButton",
    onClick: () -> Unit
) {
    val text = stringResource(Res.string.back_button_text)

    Box(
        modifier = Modifier
            .padding(start = 6.dp)
            .width(width)
            .height(height)
            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.arrow_back_24px),
            contentDescription = text,
            modifier = Modifier.fillMaxSize(),
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
