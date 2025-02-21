package com.example.ntools.material.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HostsDiscoveryDevices24: ImageVector
    get() {
        if (_HostsDiscoveryDevices24 != null) {
            return _HostsDiscoveryDevices24!!
        }
        _HostsDiscoveryDevices24 = ImageVector.Builder(
            name = "Filled.HostsDiscoveryDevices24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(4f, 6f)
                horizontalLineToRelative(18f)
                lineTo(22f, 4f)
                lineTo(4f, 4f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(11f)
                lineTo(0f, 17f)
                verticalLineToRelative(3f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(-3f)
                lineTo(4f, 17f)
                lineTo(4f, 6f)
                close()
                moveTo(23f, 8f)
                horizontalLineToRelative(-6f)
                curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
                verticalLineToRelative(10f)
                curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
                horizontalLineToRelative(6f)
                curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
                lineTo(24f, 9f)
                curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
                close()
                moveTo(22f, 17f)
                horizontalLineToRelative(-4f)
                verticalLineToRelative(-7f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(7f)
                close()
            }
        }.build()

        return _HostsDiscoveryDevices24!!
    }

@Suppress("ObjectPropertyName")
private var _HostsDiscoveryDevices24: ImageVector? = null
