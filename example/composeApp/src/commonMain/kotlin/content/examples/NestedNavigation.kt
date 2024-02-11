package content.examples

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberNavController
import content.examples.common.SimpleScreen

val NestedNavigationRoot by navDestination<Unit> {
    SimpleScreen("Nested navigation") {
        Box(
            Modifier
                .padding(64.dp)
                .border(8.dp, MaterialTheme.colors.onSurface)
        ) {
            val nestedNavController = rememberNavController(
                "nestedNavController",
                startDestination = SimpleForwardBackRoot,
                destinations = arrayOf(
                    SimpleForwardBackRoot,
                    SimpleForwardBackRootScreen1,
                    SimpleForwardBackRootScreen2,
                    SimpleForwardBackRootScreen3,
                )
            )
            Navigation(nestedNavController)
        }
    }
}