package com.example.ntools.material.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PortScannerLan24: ImageVector
    get() {
        if (_PortScannerLan24 != null) {
            return _PortScannerLan24!!
        }
        _PortScannerLan24 = ImageVector.Builder(
            name = "Filled.PortScannerLan24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(13f, 22f)
                lineToRelative(8f, 0f)
                lineToRelative(0f, -7f)
                lineToRelative(-3f, 0f)
                lineToRelative(0f, -4f)
                lineToRelative(-5f, 0f)
                lineToRelative(0f, -2f)
                lineToRelative(3f, 0f)
                lineToRelative(0f, -7f)
                lineToRelative(-8f, 0f)
                lineToRelative(0f, 7f)
                lineToRelative(3f, 0f)
                lineToRelative(0f, 2f)
                lineToRelative(-5f, 0f)
                lineToRelative(0f, 4f)
                lineToRelative(-3f, 0f)
                lineToRelative(0f, 7f)
                lineToRelative(8f, 0f)
                lineToRelative(0f, -7f)
                lineToRelative(-3f, 0f)
                lineToRelative(0f, -2f)
                lineToRelative(8f, 0f)
                lineToRelative(0f, 2f)
                lineToRelative(-3f, 0f)
                close()
            }
        }.build()

        return _PortScannerLan24!!
    }

@Suppress("ObjectPropertyName")
private var _PortScannerLan24: ImageVector? = null
