package content.examples

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
                .padding(32.dp)
                .border(4.dp, MaterialTheme.colorScheme.onSurface)
                .padding(4.dp)
        ) {
            val nestedNavController = rememberNavController(
                key = "nestedNavController",
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