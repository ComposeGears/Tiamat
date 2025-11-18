package composegears.tiamat.sample.platform.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import composegears.tiamat.sample.icons.Icons

val Icons.FlipCameraAndroid: ImageVector
    get() {
        if (_FlipCameraAndroid != null) {
            return _FlipCameraAndroid!!
        }
        _FlipCameraAndroid = ImageVector.Builder(
            name = "FlipCameraAndroid",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(480f, 880f)
                quadToRelative(-143f, 0f, -253f, -90f)
                reflectiveQuadTo(88f, 560f)
                horizontalLineToRelative(82f)
                quadToRelative(28f, 106f, 114f, 173f)
                reflectiveQuadToRelative(196f, 67f)
                quadToRelative(86f, 0f, 160f, -42.5f)
                reflectiveQuadTo(756f, 640f)
                lineTo(640f, 640f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(240f)
                verticalLineToRelative(240f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(-80f)
                quadToRelative(-57f, 76f, -141f, 118f)
                reflectiveQuadTo(480f, 880f)
                close()
                moveTo(480f, 600f)
                quadToRelative(-50f, 0f, -85f, -35f)
                reflectiveQuadToRelative(-35f, -85f)
                quadToRelative(0f, -50f, 35f, -85f)
                reflectiveQuadToRelative(85f, -35f)
                quadToRelative(50f, 0f, 85f, 35f)
                reflectiveQuadToRelative(35f, 85f)
                quadToRelative(0f, 50f, -35f, 85f)
                reflectiveQuadToRelative(-85f, 35f)
                close()
                moveTo(80f, 400f)
                verticalLineToRelative(-240f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(80f)
                quadToRelative(57f, -76f, 141f, -118f)
                reflectiveQuadToRelative(179f, -42f)
                quadToRelative(143f, 0f, 253f, 90f)
                reflectiveQuadToRelative(139f, 230f)
                horizontalLineToRelative(-82f)
                quadToRelative(-28f, -106f, -114f, -173f)
                reflectiveQuadToRelative(-196f, -67f)
                quadToRelative(-86f, 0f, -160f, 42.5f)
                reflectiveQuadTo(204f, 320f)
                horizontalLineToRelative(116f)
                verticalLineToRelative(80f)
                lineTo(80f, 400f)
                close()
            }
        }.build()

        return _FlipCameraAndroid!!
    }

@Suppress("ObjectPropertyName")
private var _FlipCameraAndroid: ImageVector? = null
