package com.blbulyandavbulyan.larm.kmp.domain.auth

data class UserProfile(
    val id: String,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?
) {
    val displayEmail: String? = if (email != displayName) email else null
}
