package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.action_open_navigation_drawer
import armenianlearningassistant_kmp.shared.generated.resources.ic_menu_24px
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppTopBar(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    centerContent: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        navigationIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("hamburger_button")
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_menu_24px),
                        contentDescription = stringResource(Res.string.action_open_navigation_drawer)
                    )
                }

                if (onBack != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    GoBackButton(
                        width = 44.dp,
                        height = 44.dp,
                        onClick = onBack,
                        testTag = "top_bar_back_button"
                    )
                }
            }
        },
        title = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                centerContent()
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier.testTag("app_top_bar")
    )
}
