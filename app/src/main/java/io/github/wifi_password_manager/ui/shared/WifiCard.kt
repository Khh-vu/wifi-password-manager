package io.github.wifi_password_manager.ui.shared

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiCard(modifier: Modifier = Modifier, network: WifiNetwork) {
    ElevatedCard(
        modifier = modifier,
        onClick = {
            // TODO: Handle card click
        },
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = network.ssid,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                if (network.password.isNotEmpty()) {
                    var obscured by rememberSaveable { mutableStateOf(true) }

                    Text(
                        text =
                            if (obscured) {
                                network.password.replace(".".toRegex(), "•")
                            } else {
                                network.password
                            },
                        modifier = Modifier.clickable { obscured = !obscured },
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        letterSpacing = if (obscured) 4.sp else TextUnit.Unspecified,
                    )
                } else {
                    Text(text = "No password")
                }
            },
            trailingContent = {
                if (network.password.isNotEmpty()) {
                    val clipboard = LocalClipboard.current
                    val scope = rememberCoroutineScope()

                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(text = "Copy Wifi Password") } },
                        state = rememberTooltipState(),
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val clipData =
                                        ClipData.newPlainText(network.ssid, network.password)
                                            .apply {
                                                description.extras =
                                                    PersistableBundle().apply {
                                                        if (
                                                            Build.VERSION.SDK_INT >=
                                                                Build.VERSION_CODES.TIRAMISU
                                                        ) {
                                                            putBoolean(
                                                                ClipDescription.EXTRA_IS_SENSITIVE,
                                                                true,
                                                            )
                                                        } else {
                                                            putBoolean(
                                                                "android.content.extra.IS_SENSITIVE",
                                                                true,
                                                            )
                                                        }
                                                    }
                                            }
                                    clipboard.setClipEntry(ClipEntry(clipData))
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Filled.Key, contentDescription = "Lock")
                        }
                    }
                }
            },
        )
    }
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
