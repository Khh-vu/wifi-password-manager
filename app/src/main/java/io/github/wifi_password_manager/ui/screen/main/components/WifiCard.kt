package io.github.wifi_password_manager.ui.screen.main.components

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.MOCK
import kotlinx.coroutines.launch

@Composable
fun WifiCard(modifier: Modifier = Modifier, network: WifiNetwork, expanded: Boolean = false) {
    ElevatedCard(modifier = modifier) {
        SSIDItem(network = network)

        if (network.password.isNotEmpty() || expanded) {
            HorizontalDivider(
                modifier =
                    Modifier.background(color = ListItemDefaults.containerColor)
                        .padding(horizontal = 16.dp)
            )

            PasswordItem(network = network)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SSIDItem(modifier: Modifier = Modifier, network: WifiNetwork) {
    val context = LocalContext.current

    val trailingContent =
        @Composable {
            TooltipBox(
                positionProvider =
                    TooltipDefaults.rememberTooltipPositionProvider(
                        positioning = TooltipAnchorPosition.Above
                    ),
                tooltip = {
                    PlainTooltip { Text(text = stringResource(R.string.hidden_network_tooltip)) }
                },
                state = rememberTooltipState(),
            ) {
                Icon(
                    imageVector = Icons.Filled.VisibilityOff,
                    contentDescription = stringResource(R.string.hidden_network_tooltip),
                )
            }
        }

    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                text = network.ssid,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = { Text(text = network.getSecurity(context)) },
        trailingContent = trailingContent.takeIf { network.hidden },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PasswordItem(modifier: Modifier = Modifier, network: WifiNetwork) {
    val trailingContent =
        @Composable {
            val clipboard = LocalClipboard.current
            val scope = rememberCoroutineScope()

            Button(
                onClick = {
                    scope.launch {
                        val clipData =
                            ClipData.newPlainText(network.ssid, network.password).apply {
                                description.extras =
                                    PersistableBundle().apply {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                                        } else {
                                            putBoolean("android.content.extra.IS_SENSITIVE", true)
                                        }
                                    }
                            }
                        clipboard.setClipEntry(clipData.toClipEntry())
                    }
                },
                shapes = ButtonDefaults.shapes(),
                contentPadding = ButtonDefaults.ContentPadding,
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = stringResource(R.string.copy_description),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.copy_action))
            }
        }

    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                text = stringResource(R.string.password_label),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            if (network.password.isNotEmpty()) {
                var obscured by rememberSaveable { mutableStateOf(true) }

                Text(
                    text =
                        if (obscured) {
                            stringResource(R.string.password_mask_character)
                                .repeat(network.password.length)
                        } else {
                            network.password
                        },
                    modifier = Modifier.clickable { obscured = !obscured },
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    letterSpacing = if (obscured) 4.sp else TextUnit.Unspecified,
                )
            } else {
                Text(text = stringResource(R.string.no_password))
            }
        },
        trailingContent = trailingContent.takeIf { network.password.isNotEmpty() },
    )
}

@PreviewLightDark
@Composable
private fun WifiCardPreview() {
    WiFiPasswordManagerTheme {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(WifiNetwork.MOCK) { WifiCard(network = it) }
        }
    }
}

@PreviewLightDark
@Composable
private fun ExpandedWifiCardPreview() {
    WiFiPasswordManagerTheme {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(WifiNetwork.MOCK) { WifiCard(network = it, expanded = true) }
        }
    }
}
