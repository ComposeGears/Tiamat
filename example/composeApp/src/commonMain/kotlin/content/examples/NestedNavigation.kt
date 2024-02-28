package content.examples

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberNavController
import content.examples.common.SimpleScreen

val NestedNavigationRoot by navDestination<Unit> {
    SimpleScreen("Nested navigation") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            val nestedNavController1 = rememberNavController(
                startDestination = SimpleForwardBackRoot,
                destinations = arrayOf(
                    SimpleForwardBackRoot,
                    SimpleForwardBackRootScreen1,
                    SimpleForwardBackRootScreen2,
                    SimpleForwardBackRootScreen3,
                )
            )
            val nestedNavController2 = rememberNavController(
                startDestination = SimpleForwardBackRoot,
                destinations = arrayOf(
                    SimpleForwardBackRoot,
                    SimpleForwardBackRootScreen1,
                    SimpleForwardBackRootScreen2,
                    SimpleForwardBackRootScreen3,
                )
            )
            Navigation(
                nestedNavController1,
                Modifier
                    .weight(1f)
                    .padding(32.dp)
                    .border(4.dp, MaterialTheme.colorScheme.onSurface)
                    .padding(4.dp)
            )
            Navigation(
                nestedNavController2,
                Modifier
                    .weight(1f)
                    .padding(32.dp)
                    .border(4.dp, MaterialTheme.colorScheme.onSurface)
                    .padding(4.dp)
            )
        }
    }
}