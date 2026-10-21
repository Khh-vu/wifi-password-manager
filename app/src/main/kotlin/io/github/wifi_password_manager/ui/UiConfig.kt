package io.github.wifi_password_manager.ui

import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape

object UiConfig {
    @Composable
    fun listItemShapes(): ListItemShapes =
        ListItemDefaults.shapes(
            shape = RectangleShape,
            selectedShape = RectangleShape,
            pressedShape = RectangleShape,
            focusedShape = RectangleShape,
            hoveredShape = RectangleShape,
            draggedShape = RectangleShape,
        )

    @Composable
    fun listItemColors(): ListItemColors =
        ListItemDefaults.colors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        )
}
