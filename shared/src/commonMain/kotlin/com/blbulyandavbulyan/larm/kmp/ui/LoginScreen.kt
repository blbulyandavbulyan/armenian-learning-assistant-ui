package com.blbulyandavbulyan.larm.kmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton

@Composable
fun LoginScreen(onTokenReceived: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "Sign in to continue your learning journey",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            GoogleButtonUiContainer(
                onGoogleSignInResult = { googleUser ->
                    val jwtToken = googleUser?.idToken
                    println("Got the following google user: $googleUser")
                    if (!jwtToken.isNullOrBlank()) {
                        onTokenReceived(jwtToken)
                    } else {
                        println("Warning: Received a blank idToken from Google. AccessToken: ${googleUser?.accessToken}")
                    }
                },
                isAutoSelectEnabled = false,
                scopes = listOf("openid")
            ) {
                GoogleSignInButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Sign in with Google",
                    onClick = { this.onClick() }
                )
            }
        }
    }
}
