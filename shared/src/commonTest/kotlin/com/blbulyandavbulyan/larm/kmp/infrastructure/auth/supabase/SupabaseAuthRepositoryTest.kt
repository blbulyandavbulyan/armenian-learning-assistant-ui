package com.blbulyandavbulyan.larm.kmp.infrastructure.auth.supabase

import com.blbulyandavbulyan.larm.kmp.domain.auth.AuthState
import com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.logging.SupabaseLoggingProcessor
import io.github.jan.supabase.plugins.PluginManager
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test

class SupabaseAuthRepositoryTest {
    private val supabaseClient = mock<SupabaseClient>()
    private val auth = mock<Auth>()
    private val sessionStatusFlow = MutableStateFlow<SessionStatus>(SessionStatus.Initializing)
    private lateinit var repository: SupabaseAuthRepository

    @BeforeTest
    fun setUp() {
        every { auth.config } returns AuthConfig()
        val pluginManager = PluginManager(
            mapOf(Auth.key to auth),
            SupabaseLogger(LogLevel.NONE, "test") {
                object : SupabaseLoggingProcessor {
                    override fun isEnabled(level: LogLevel): Boolean = false
                    override fun processLog(
                        level: LogLevel,
                        tag: String,
                        throwable: Throwable?,
                        message: String
                    ) = Unit
                }
            }
        )
        every { supabaseClient.pluginManager } returns pluginManager
        every { auth.sessionStatus } returns sessionStatusFlow
        repository = SupabaseAuthRepository(supabaseClient)
    }

    @Test
    fun observeAuthState_emits_LOADING_when_status_is_Initializing() = runTest {
        sessionStatusFlow.value = SessionStatus.Initializing
        repository.observeAuthState().first() shouldBe AuthState.LOADING
    }

    @Test
    fun observeAuthState_emits_AUTHENTICATED_when_status_is_Authenticated() = runTest {
        val session = UserSession(
            accessToken = "token-123",
            refreshToken = "refresh-123",
            tokenType = "Bearer",
            expiresIn = 3600L,
            user = null
        )
        sessionStatusFlow.value = SessionStatus.Authenticated(session)
        repository.observeAuthState().first() shouldBe AuthState.AUTHENTICATED
    }

    @Test
    fun observeAuthState_emits_UNAUTHENTICATED_when_status_is_NotAuthenticated() = runTest {
        sessionStatusFlow.value = SessionStatus.NotAuthenticated(isSignOut = false)
        repository.observeAuthState().first() shouldBe AuthState.UNAUTHENTICATED
    }

    @Test
    fun observeUserProfile_emits_UserProfile_when_status_is_Authenticated() = runTest {
        val userMetadata = buildJsonObject {
            put("full_name", JsonPrimitive("John Doe"))
            put("avatar_url", JsonPrimitive("https://example.com/avatar.png"))
        }
        val userInfo = UserInfo(
            id = "user-id-42",
            aud = "authenticated",
            email = "john@example.com",
            userMetadata = userMetadata
        )
        val session = UserSession(
            accessToken = "token-123",
            refreshToken = "refresh-123",
            tokenType = "Bearer",
            expiresIn = 3600L,
            user = userInfo
        )
        sessionStatusFlow.value = SessionStatus.Authenticated(session)

        repository.observeUserProfile().first() shouldBe UserProfile(
            id = "user-id-42",
            email = "john@example.com",
            displayName = "John Doe",
            avatarUrl = "https://example.com/avatar.png"
        )
    }

    @Test
    fun observeUserProfile_emits_null_when_status_is_NotAuthenticated() = runTest {
        sessionStatusFlow.value = SessionStatus.NotAuthenticated(isSignOut = true)
        repository.observeUserProfile().first() shouldBe null
    }

    @Test
    fun signInWithGoogle_delegates_to_auth_signInWith() = runTest {
        everySuspend { auth.signInWith(Google, redirectUrl = any()) } returns Unit

        repository.signInWithGoogle()

        verifySuspend { auth.signInWith(Google, redirectUrl = any()) }
    }

    @Test
    fun signOut_delegates_to_auth_signOut() = runTest {
        everySuspend { auth.signOut(any()) } returns Unit

        repository.signOut()

        verifySuspend { auth.signOut(SignOutScope.LOCAL) }
    }

    @Test
    fun getCurrentAccessToken_delegates_to_auth_currentAccessTokenOrNull() {
        every { auth.currentAccessTokenOrNull() } returns "access-token-abc"

        repository.getCurrentAccessToken() shouldBe "access-token-abc"
    }

    @Test
    fun getCurrentAccessToken_returns_null_when_auth_returns_null() {
        every { auth.currentAccessTokenOrNull() } returns null

        repository.getCurrentAccessToken() shouldBe null
    }
}
