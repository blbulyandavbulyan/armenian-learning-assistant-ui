package com.blbulyandavbulyan.larm.kmp.core

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class Resource(val resId: StringResource) : UiText

    // Called inside @Composable UI to resolve the actual string
    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is Resource -> stringResource(resId)
    }

    companion object {
        fun from(value: String): UiText = DynamicString(value)
        fun from(value: StringResource): UiText = Resource(value)
        fun from(value: String?, fallbackStringResource: StringResource): UiText =
            value?.let { from(it) } ?: from(fallbackStringResource)
    }
}
