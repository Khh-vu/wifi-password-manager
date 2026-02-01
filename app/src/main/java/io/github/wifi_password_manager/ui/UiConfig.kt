package io.github.wifi_password_manager.ui

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.RectangleShape

object UiConfig {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
}
