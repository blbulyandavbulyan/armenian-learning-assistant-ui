package com.blbulyandavbulyan.larm.kmp.domain.auth

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooGenericExceptionThrown")
class FakeAuthRepository : AuthRepository {
    val authStateFlow = MutableStateFlow(AuthState.UNAUTHENTICATED)
    val userProfileFlow = MutableStateFlow<UserProfile?>(null)
    var currentToken: String? = null
    var signInCalled = false
    var signOutCalled = false

    var shouldThrowOnSignIn = false
    var signInDelayMs: Long = 0L

    override fun observeAuthState(): Flow<AuthState> = authStateFlow

    override fun observeUserProfile(): Flow<UserProfile?> = userProfileFlow

    override suspend fun signInWithGoogle() {
        if (shouldThrowOnSignIn) {
            throw Exception("Google Sign In Failed")
        }
        if (signInDelayMs > 0) {
            delay(signInDelayMs)
        }
        signInCalled = true
        authStateFlow.value = AuthState.AUTHENTICATED
        currentToken = "fake_google_token"
    }

    override suspend fun signOut() {
        signOutCalled = true
        authStateFlow.value = AuthState.UNAUTHENTICATED
        userProfileFlow.value = null
        currentToken = null
    }

    override fun getCurrentAccessToken(): String? = currentToken
}
