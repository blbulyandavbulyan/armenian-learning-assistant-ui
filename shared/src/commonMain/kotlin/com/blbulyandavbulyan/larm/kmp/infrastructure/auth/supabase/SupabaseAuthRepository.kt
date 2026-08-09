package com.blbulyandavbulyan.larm.kmp.infrastructure.auth.supabase

import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthRepository
import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthState
import com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SupabaseAuthRepository(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override fun observeAuthState(): Flow<AuthState> {
        return supabaseClient.auth.sessionStatus.map { it.toAuthState() }
    }

    override fun observeUserProfile(): Flow<UserProfile?> {
        return supabaseClient.auth.sessionStatus.map { it.toUserProfile() }
    }

    override suspend fun signInWithGoogle() {
        supabaseClient.auth.signInWith(Google)
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    override fun getCurrentAccessToken(): String? {
        return supabaseClient.auth.currentAccessTokenOrNull()
    }
}
