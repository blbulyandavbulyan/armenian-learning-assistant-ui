package com.blbulyandavbulyan.larm.kmp.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blbulyandavbulyan.larm.kmp.ui.theme.AppTheme

@Composable
internal fun AnimatedSaveButton(
    isSaving: Boolean,
    isSaved: Boolean,
    savedText: String,
    saveText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        val buttonShape = RoundedCornerShape(24.dp)

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
        ) {
            if (isSaving) {
                animateSaving(buttonShape, AppTheme.colors.saveButtonLoadingRing)
            }

            SaveStateAwareButton(
                onClick = onClick,
                isSaving = isSaving,
                isSaved = isSaved,
                buttonShape = buttonShape,
                savedText = savedText,
                saveText = saveText
            )
        }
    }
}

@Composable
private fun SaveStateAwareButton(
    onClick: () -> Unit,
    isSaving: Boolean,
    isSaved: Boolean,
    buttonShape: RoundedCornerShape,
    savedText: String,
    saveText: String
) {
    Button(
        onClick = onClick,
        enabled = !isSaving && !isSaved,
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isSaving) 3.dp else 2.dp)
            .semantics {
                if (isSaving) {
                    progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                }
            }.testTag("saveButton"),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.saveButton,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = if (isSaved) AppTheme.colors.saveButton.copy(alpha = 0.5f) else AppTheme.colors.saveButton,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        ),
        shape = buttonShape
    ) {
        DisableSelection {
            val text = if (isSaved) savedText else saveText
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun BoxScope.animateSaving(buttonShape: RoundedCornerShape, ringColor: Color) {
    val transition = rememberInfiniteTransition()
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(buttonShape)
            .drawBehind {
                rotate(angle) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                ringColor.copy(alpha = 0.8f),
                                ringColor,
                                Color.Transparent
                            )
                        ),
                        radius = size.width
                    )
                }
            }
    )
}
