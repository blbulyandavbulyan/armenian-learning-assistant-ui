package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize().testTag("loadingIndicator"), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
