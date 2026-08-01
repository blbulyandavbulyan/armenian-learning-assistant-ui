package com.blbulyandavbulyan.larm.kmp.infrastructure.auth.supabase

import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthState
import com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test

class SessionStatusMappersTest {

    @Test
    fun sessionStatus_toAuthState_mapsAllVariantsCorrectly() {
        val dummySession = UserSession(
            accessToken = "token",
            refreshToken = "refresh",
            expiresIn = 3600,
            tokenType = "bearer",
            user = null
        )
        val authenticated: SessionStatus = SessionStatus.Authenticated(dummySession)
        val notAuthenticated: SessionStatus = SessionStatus.NotAuthenticated()
        val initializing: SessionStatus = SessionStatus.Initializing
        val refreshFailure: SessionStatus = SessionStatus.RefreshFailure(
            RefreshFailureCause.NetworkError(
                @Suppress("TooGenericExceptionThrown")
                Exception("test")
            )
        )

        authenticated.toAuthState() shouldBe AuthState.AUTHENTICATED
        notAuthenticated.toAuthState() shouldBe AuthState.UNAUTHENTICATED
        initializing.toAuthState() shouldBe AuthState.LOADING
        refreshFailure.toAuthState() shouldBe AuthState.UNAUTHENTICATED
    }

    @Test
    fun sessionStatus_toUserProfile_mapsFullProfileWithFullNameAndAvatarUrl() {
        val metadata = buildJsonObject {
            put("full_name", "David Bul")
            put("avatar_url", "https://example.com/avatar.jpg")
        }
        val user = UserInfo(
            id = "user_123",
            aud = "authenticated",
            email = "david@example.com",
            userMetadata = metadata
        )
        val session = UserSession(
            accessToken = "token",
            refreshToken = "refresh",
            expiresIn = 3600L,
            tokenType = "bearer",
            user = user
        )
        val status = SessionStatus.Authenticated(session)

        status.toUserProfile() shouldBe UserProfile(
            id = "user_123",
            email = "david@example.com",
            displayName = "David Bul",
            avatarUrl = "https://example.com/avatar.jpg"
        )
    }

    @Test
    fun sessionStatus_toUserProfile_mapsFallbackNameAndPicture() {
        val metadata = buildJsonObject {
            put("name", "Jane Doe")
            put("picture", "https://example.com/picture.png")
        }
        val user = UserInfo(
            id = "user_456",
            aud = "authenticated",
            email = "jane@example.com",
            userMetadata = metadata
        )
        val session = UserSession(
            accessToken = "token",
            refreshToken = "refresh",
            expiresIn = 3600L,
            tokenType = "bearer",
            user = user
        )
        val status = SessionStatus.Authenticated(session)

        status.toUserProfile() shouldBe UserProfile(
            id = "user_456",
            email = "jane@example.com",
            displayName = "Jane Doe",
            avatarUrl = "https://example.com/picture.png"
        )
    }

    @Test
    fun sessionStatus_toUserProfile_fallsBackToEmailWhenNoNameInMetadata() {
        val metadata = buildJsonObject {
            put("custom_key", "value")
        }
        val user = UserInfo(
            id = "user_789",
            aud = "authenticated",
            email = "anon@example.com",
            userMetadata = metadata
        )
        val session = UserSession(
            accessToken = "token",
            refreshToken = "refresh",
            expiresIn = 3600L,
            tokenType = "bearer",
            user = user
        )
        val status = SessionStatus.Authenticated(session)

        status.toUserProfile() shouldBe UserProfile(
            id = "user_789",
            email = "anon@example.com",
            displayName = "anon@example.com",
            avatarUrl = null
        )
    }

    @Test
    fun sessionStatus_toUserProfile_returnsNullForNonAuthenticatedOrNullUser() {
        val sessionWithoutUser = UserSession(
            accessToken = "token",
            refreshToken = "refresh",
            expiresIn = 3600L,
            tokenType = "bearer",
            user = null
        )
        SessionStatus.Authenticated(sessionWithoutUser).toUserProfile() shouldBe null
        SessionStatus.NotAuthenticated().toUserProfile() shouldBe null
        SessionStatus.Initializing.toUserProfile() shouldBe null
    }
}
