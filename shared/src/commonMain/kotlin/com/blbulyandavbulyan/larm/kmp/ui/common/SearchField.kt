package com.blbulyandavbulyan.larm.kmp.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.ic_search_24px
import armenianlearningassistant_kmp.shared.generated.resources.search_button_text
import com.blbulyandavbulyan.larm.kmp.ui.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchField(
    query: String,
    textFieldModifier: Modifier,
    searchButtonTestTag: String = "searchSubmitButton",
    onSearch: () -> Unit,
    onValueChange: (String) -> Unit,
    placeholder: @Composable (() -> Unit)
) {
    OutlinedTextField(
        value = query,
        onValueChange = onValueChange,
        modifier = textFieldModifier,
        placeholder = placeholder,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = AppTheme.colors.unfocusedBorder,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(24.dp),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        trailingIcon = {
            Icon(
                painter = painterResource(Res.drawable.ic_search_24px),
                contentDescription = stringResource(Res.string.search_button_text),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxHeight()
                    .padding(4.dp)
                    .clickable { onSearch() }
                    .testTag(searchButtonTestTag)
            )
        }
    )
}
