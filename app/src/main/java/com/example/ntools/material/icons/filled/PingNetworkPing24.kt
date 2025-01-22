package com.example.ntools.material.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PingNetworkPing24: ImageVector
    get() {
        if (_PingNetworkPing24 != null) {
            return _PingNetworkPing24!!
        }
        _PingNetworkPing24 = ImageVector.Builder(
            name = "Filled.PingNetworkPing24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(12f, 14.67f)
                lineTo(3.41f, 6.09f)
                lineTo(2f, 7.5f)
                lineToRelative(8.5f, 8.5f)
                horizontalLineTo(4f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(16f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(-6.5f)
                lineToRelative(5.15f, -5.15f)
                curveTo(18.91f, 10.95f, 19.2f, 11f, 19.5f, 11f)
                curveToRelative(1.38f, 0f, 2.5f, -1.12f, 2.5f, -2.5f)
                reflectiveCurveTo(20.88f, 6f, 19.5f, 6f)
                reflectiveCurveTo(17f, 7.12f, 17f, 8.5f)
                curveToRelative(0f, 0.35f, 0.07f, 0.67f, 0.2f, 0.97f)
                lineTo(12f, 14.67f)
                close()
            }
        }.build()

        return _PingNetworkPing24!!
    }

@Suppress("ObjectPropertyName")
private var _PingNetworkPing24: ImageVector? = null
