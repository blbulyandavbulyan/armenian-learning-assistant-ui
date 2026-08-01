package com.blbulyandavbulyan.larm.kmp.infrastructure.auth.supabase

import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthState
import com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

fun SessionStatus.toAuthState(): AuthState = when (this) {
    is SessionStatus.Authenticated -> AuthState.AUTHENTICATED
    is SessionStatus.NotAuthenticated -> AuthState.UNAUTHENTICATED
    is SessionStatus.Initializing -> AuthState.LOADING
    else -> AuthState.UNAUTHENTICATED
}

fun SessionStatus.toUserProfile(): UserProfile? = when (this) {
    is SessionStatus.Authenticated -> {
        val user = session.user ?: return null
        val metadata = user.userMetadata
        val displayName = metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("name")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("preferred_username")?.jsonPrimitive?.contentOrNull
            ?: user.email
        val avatarUrl = metadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("picture")?.jsonPrimitive?.contentOrNull

        UserProfile(
            id = user.id,
            email = user.email,
            displayName = displayName,
            avatarUrl = avatarUrl
        )
    }
    else -> null
}
