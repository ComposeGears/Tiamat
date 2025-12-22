package composegears.tiamat.sample.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Web: ImageVector
    get() {
        if (_Web != null) {
            return _Web!!
        }
        _Web = ImageVector.Builder(
            name = "Web",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(160f, 800f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(80f, 720f)
                verticalLineToRelative(-480f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(160f, 160f)
                horizontalLineToRelative(640f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(880f, 240f)
                verticalLineToRelative(480f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(800f, 800f)
                lineTo(160f, 800f)
                close()
                moveTo(160f, 720f)
                horizontalLineToRelative(420f)
                verticalLineToRelative(-140f)
                lineTo(160f, 580f)
                verticalLineToRelative(140f)
                close()
                moveTo(660f, 720f)
                horizontalLineToRelative(140f)
                verticalLineToRelative(-360f)
                lineTo(660f, 360f)
                verticalLineToRelative(360f)
                close()
                moveTo(160f, 500f)
                horizontalLineToRelative(420f)
                verticalLineToRelative(-140f)
                lineTo(160f, 360f)
                verticalLineToRelative(140f)
                close()
            }
        }.build()

        return _Web!!
    }

@Suppress("ObjectPropertyName")
private var _Web: ImageVector? = null
