package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    state: NetworkListViewModel.State,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.showingSearch) {
        if (state.showingSearch) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    Surface(modifier = modifier, color = MaterialTheme.colorScheme.secondaryContainer) {
        TextField(
            modifier =
                Modifier.fillMaxWidth()
                    .statusBarsPadding()
                    .height(TopAppBarDefaults.TopAppBarExpandedHeight)
                    .focusRequester(focusRequester),
            value = state.searchText,
            onValueChange = { onAction(NetworkListViewModel.Action.SearchTextChanged(it)) },
            singleLine = true,
            placeholder = { Text(text = stringResource(R.string.search_hint)) },
            keyboardOptions =
                KeyboardOptions(imeAction = ImeAction.Search, keyboardType = KeyboardType.Text),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
            leadingIcon = {
                TooltipIconButton(
                    onClick = { onAction(NetworkListViewModel.Action.ToggleSearch) },
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tooltip = stringResource(R.string.back),
                    positioning = TooltipAnchorPosition.Below,
                )
            },
            trailingIcon = {
                Row {
                    TooltipIconButton(
                        onClick = { onAction(NetworkListViewModel.Action.SearchTextChanged("")) },
                        imageVector = Icons.Filled.Clear,
                        tooltip = stringResource(R.string.clear),
                        positioning = TooltipAnchorPosition.Below,
                    )
                }
            },
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SearchBarPreview() {
    WiFiPasswordManagerTheme {
        Scaffold(topBar = { SearchBar(state = NetworkListViewModel.State(), onAction = {}) }) {
            Box(modifier = Modifier.padding(it))
        }
    }
}
