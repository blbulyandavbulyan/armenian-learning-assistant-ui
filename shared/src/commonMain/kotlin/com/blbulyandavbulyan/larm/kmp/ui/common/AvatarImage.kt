package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage

fun extractInitials(displayName: String?): String {
    if (displayName.isNullOrBlank()) return "U"
    val parts = displayName.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "U"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

@Composable
fun AvatarImage(
    avatarUrl: String?,
    displayName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val initials = remember(displayName) { extractInitials(displayName) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (avatarUrl.isNullOrBlank()) {
            InitialsAvatar(
                initials = initials,
                size = size,
                modifier = Modifier.testTag("avatar_fallback_initials")
            )
        } else {
            SubcomposeAsyncImage(
                model = avatarUrl,
                contentDescription = displayName,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .testTag("avatar_coil_image"),
                contentScale = ContentScale.Crop,
                loading = {
                    InitialsAvatar(
                        initials = initials,
                        size = size,
                        modifier = Modifier.testTag("avatar_loading_initials")
                    )
                },
                error = {
                    InitialsAvatar(
                        initials = initials,
                        size = size,
                        modifier = Modifier.testTag("avatar_error_initials")
                    )
                }
            )
        }
    }
}

@Composable
private fun InitialsAvatar(
    initials: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .semantics(mergeDescendants = true) { }
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontSize = (size.value * 0.4).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
