package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BoxScope.PrimaryVerticalScrollbar(
    modifier: Modifier = Modifier,
    adapter: ScrollbarAdapter
) {
    VerticalScrollbar(
        modifier = modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        adapter = adapter,
        style = defaultScrollbarStyle().copy(
            unhoverColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            hoverColor = MaterialTheme.colorScheme.primary
        )
    )
}
