package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.action_sign_out
import armenianlearningassistant_kmp.shared.generated.resources.ic_logout_24px
import armenianlearningassistant_kmp.shared.generated.resources.nav_dialogue_generator
import armenianlearningassistant_kmp.shared.generated.resources.profile_anonymous_user
import armenianlearningassistant_kmp.shared.generated.resources.profile_no_email
import com.blbulyandavbulyan.larm.kmp.domain.auth.UserProfile
import com.blbulyandavbulyan.larm.kmp.presentation.global.ScreenState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppDrawerContent(
    userProfile: UserProfile?,
    currentScreen: ScreenState,
    onNavigateToGenerator: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .testTag("app_drawer_sheet")
    ) {
        // Top Header: User Profile
        DrawerProfileHeader(
            userProfile = userProfile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("drawer_profile_header")
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Items
        NavigationDrawerItem(
            label = { Text(stringResource(Res.string.nav_dialogue_generator)) },
            selected = currentScreen is ScreenState.Generator,
            onClick = onNavigateToGenerator,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .testTag("drawer_nav_generator")
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // Footer: Sign Out
        SignOutDrawerItem(onSignOut)
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun SignOutDrawerItem(onSignOut: () -> Unit) {
    NavigationDrawerItem(
        icon = {
            Icon(
                painter = painterResource(Res.drawable.ic_logout_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        label = {
            Text(
                text = stringResource(Res.string.action_sign_out),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        },
        selected = false,
        onClick = onSignOut,
        modifier = Modifier
            .padding(NavigationDrawerItemDefaults.ItemPadding)
            .testTag("drawer_sign_out_item")
    )
}

@Composable
private fun DrawerProfileHeader(
    userProfile: UserProfile?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(
            avatarUrl = userProfile?.avatarUrl,
            displayName = userProfile?.displayName,
            size = 48.dp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            val name = userProfile?.displayName
                ?: stringResource(Res.string.profile_anonymous_user)
            val email = userProfile?.email
                ?: stringResource(Res.string.profile_no_email)

            Text(
                text = name,
                modifier = Modifier.testTag("drawer_profile_name"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = email,
                modifier = Modifier.testTag("drawer_profile_email"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
