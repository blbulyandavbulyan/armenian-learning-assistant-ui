package com.blbulyandavbulyan.larm.kmp.domain.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    fun observeUserProfile(): Flow<UserProfile?>
    suspend fun signInWithGoogle()
    suspend fun signOut()
    fun getCurrentAccessToken(): String?
}
