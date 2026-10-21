package io.github.wifi_password_manager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider

@Composable
fun WiFiPasswordManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    pureBlackTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> darkColorScheme()
            else -> expressiveLightColorScheme()
        }.let {
            if (pureBlackTheme && darkTheme) {
                it.copy(background = Color.Black, surface = Color.Black)
            } else {
                it
            }
        }

    MaterialExpressiveTheme(colorScheme = colorScheme, content = content)
}

class ThemeWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        WiFiPasswordManagerTheme { content() }
    }
}

class ScaffoldWrapper : PreviewWrapperProvider {
    private val themeWrapper = ThemeWrapper()

    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        themeWrapper.Wrap {
            Scaffold { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) { content() }
            }
        }
    }
}

class SurfaceWrapper : PreviewWrapperProvider {
    private val themeWrapper = ThemeWrapper()

    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        themeWrapper.Wrap { Surface(modifier = Modifier.fillMaxSize()) { content() } }
    }
}
