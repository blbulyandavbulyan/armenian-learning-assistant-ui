package com.blbulyandavbulyan.larm.kmp.ui.dialogue.search

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class RoundedCutBottomRightShape(
    private val cornerRadius: Float = 32f, // How round the 3 normal corners are
    private val cutOffset: Float = 40f // How far inward the slant starts
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val r = cornerRadius

            // Start at top-left corner (after the curve)
            moveTo(r, 0f)

            // Top edge to Top-Right corner
            lineTo(size.width - r, 0f)
            arcTo(
                rect = Rect(left = size.width - 2 * r, top = 0f, right = size.width, bottom = 2 * r),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Right edge down to where the slanted cut begins
            lineTo(x = size.width, y = size.height * 0.7f)

            // THE SHARP CUT: Diagonal line straight to the bottom edge
            lineTo(size.width - cutOffset, size.height)

            // Bottom edge over to the Bottom-Left corner
            lineTo(r, size.height)
            arcTo(
                rect = Rect(left = 0f, top = size.height - 2 * r, right = 2 * r, bottom = size.height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Left edge going back up to Top-Left corner
            lineTo(0f, r)
            arcTo(
                rect = Rect(left = 0f, top = 0f, right = 2 * r, bottom = 2 * r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            close()
        }
        return Outline.Generic(path)
    }
}

class RoundedTopLeftCutShape(
    private val cornerRadius: Float = 24f, // Matches your title rounding
    private val cutOffset: Float = 30f // Sleek cut accent
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val r = cornerRadius

            // Start right after the top-left cut
            moveTo(x = cutOffset, y = 0f)

            // Top edge to Top-Right corner
            lineTo(x = size.width - r, y = 0f)
            arcTo(
                rect = Rect(left = size.width - 2 * r, top = 0f, right = size.width, bottom = 2 * r),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Right edge to Bottom-Right corner
            lineTo(x = size.width, y = size.height - r)
            arcTo(
                rect = Rect(
                    left = size.width - 2 * r,
                    top = size.height - 2 * r,
                    right = size.width,
                    bottom = size.height
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Bottom edge to Bottom-Left corner
            lineTo(r, size.height)
            arcTo(
                rect = Rect(left = 0f, top = size.height - 2 * r, right = 2 * r, bottom = size.height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Left edge up to where the top-left cut begins
            lineTo(x = 0f, y = size.height * 0.3f)

            // THE SHARP CUT: Diagonal line straight back to the top edge
            lineTo(x = cutOffset, y = 0f)

            close()
        }
        return Outline.Generic(path)
    }
}
