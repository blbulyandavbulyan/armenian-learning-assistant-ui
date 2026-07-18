package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private class ChatBubbleShape(
    private val cornerRadius: Float = 45f, // Rounded corners for the body
    private val tailWidth: Float = 35f, // Width of the tail at the bubble base
    private val tailHeight: Float = 30f, // How far down the tail points
    private val tailOffset: Float = 60f // Distance from the left edge to the tail
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // 1. Draw the main rounded rectangle body, leaving space at the bottom for the tail
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height - tailHeight,
                    topLeftCornerRadius = CornerRadius(cornerRadius),
                    topRightCornerRadius = CornerRadius(cornerRadius),
                    bottomLeftCornerRadius = CornerRadius(cornerRadius),
                    bottomRightCornerRadius = CornerRadius(cornerRadius)
                )
            )

            // 2. Draw the tail on the bottom-left side
            moveTo(tailOffset, size.height - tailHeight) // Start of tail
            lineTo(tailOffset, size.height) // Tip of the tail pointing down
            lineTo(tailOffset + tailWidth, size.height - tailHeight) // End of tail back to the body
            close()
        }

        return Outline.Generic(path)
    }
}

@Composable
fun ChatBubbleBox(
    testTag: String? = null,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    // You can tweak these values to perfectly match your UI scale
    val bubbleShape = ChatBubbleShape(
        cornerRadius = 32f,
        tailWidth = 40f,
        tailHeight = 30f,
        tailOffset = 50f
    )

    val modifier = Modifier
        .fillMaxWidth()
        .background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = bubbleShape
        )
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            shape = bubbleShape
        )
        .clip(bubbleShape)
        .clickable(onClick = onClick)
        // Extra bottom padding accounts for the tail height so text stays inside the main bubble
        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 40.dp)

    testTag?.let(modifier::testTag)

    Box(
        modifier = modifier
    ) {
        content()
    }
}
