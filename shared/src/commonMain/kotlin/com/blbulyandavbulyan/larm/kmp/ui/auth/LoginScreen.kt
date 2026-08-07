package com.blbulyandavbulyan.larm.kmp.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.auth_app_title
import armenianlearningassistant_kmp.shared.generated.resources.auth_sign_in_with_google
import armenianlearningassistant_kmp.shared.generated.resources.auth_welcome_subtitle
import armenianlearningassistant_kmp.shared.generated.resources.google_g_logo
import armenianlearningassistant_kmp.shared.generated.resources.google_sans
import com.blbulyandavbulyan.larm.kmp.presentation.auth.LoginViewModel
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    modifier: Modifier = Modifier
) {
    val isLoading by viewModel.isLoading.collectAsState()
    LoginScreen(
        onSignInWithGoogle = viewModel::signInWithGoogle,
        isLoading = isLoading,
        modifier = modifier
    )
}

@Composable
fun LoginScreen(
    onSignInWithGoogle: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("loginScreen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .testTag("loginCard"),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.auth_app_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("authAppTitleText")
                )
                Text(
                    text = stringResource(Res.string.auth_welcome_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("authWelcomeSubtitleText")
                )
                Spacer(modifier = Modifier.height(8.dp))
                GoogleSignInButton(isLoading = isLoading, onSignInWithGoogle)
            }
        }
    }
}

@Composable
fun GoogleSignInButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    // Official Google Brand Colors
    val backgroundColor = if (isDarkTheme) Color(color = 0xFF131314) else Color(color = 0xFFFFFFFF)
    val textColor = if (isDarkTheme) Color(color = 0xFFE3E3E3) else Color(color = 0xFF1F1F1F)
    val borderColor = if (isDarkTheme) Color(color = 0xFF8E918F) else Color(color = 0xFF747775)
    val spinnerColor = if (isDarkTheme) Color(color = 0xFF8AB4F8) else Color(color = 0xFF1A73E8) // Google Blue
    val googleSansFamily = FontFamily(
        Font(Res.font.google_sans, FontWeight.Medium)
    )

    Surface(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.height(40.dp).testTag("signInWithGoogleButton"),
        shape = RoundedCornerShape(20.dp), // Use 4.dp for a rectangular button
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .testTag("signInLoadingIndicator"),
                    color = spinnerColor,
                    strokeWidth = 2.dp
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // 1. The standalone SVG Google "G" Icon
                Icon(
                    painter = painterResource(Res.drawable.google_g_logo),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified // Extremely important: preserves the multi-color G logo
                )

                Spacer(modifier = Modifier.width(10.dp))

                // 2. Localized native text instead of SVG path text
                Text(
                    text = stringResource(Res.string.auth_sign_in_with_google),
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = googleSansFamily,
                    maxLines = 1
                )
            }
        }
    }
}
