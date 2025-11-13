package composegears.tiamat.sample.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.KeyboardArrowLeft: ImageVector
    get() {
        if (_KeyboardArrowLeft != null) {
            return _KeyboardArrowLeft!!
        }
        _KeyboardArrowLeft = ImageVector.Builder(
            name = "KeyboardArrowLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(560f, 720f)
                lineTo(320f, 480f)
                lineToRelative(240f, -240f)
                lineToRelative(56f, 56f)
                lineToRelative(-184f, 184f)
                lineToRelative(184f, 184f)
                lineToRelative(-56f, 56f)
                close()
            }
        }.build()

        return _KeyboardArrowLeft!!
    }

@Suppress("ObjectPropertyName")
private var _KeyboardArrowLeft: ImageVector? = null
