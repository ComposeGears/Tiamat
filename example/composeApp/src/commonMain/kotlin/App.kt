import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composegears.navigation.tiamat.Navigation
import com.composegears.navigation.tiamat.navController
import com.composegears.navigation.tiamat.navDestination
import com.composegears.navigation.tiamat.rememberNavController


val s1 by navDestination<Unit> {
    val navC = navController()
    Column {
        Text("Screen1")
        Button(onClick = { navC.navigate(s2) }) {
            Text("Go Screens")
        }
    }
}

val s2 by navDestination<Unit> {
    val navC = navController()
    Column {
        Text("Screen2")
        Button(onClick = { navC.back() }) {
            Text("Go Back")
        }
    }
}


@Composable
fun App() {
    MaterialTheme {
        val nc = rememberNavController(
            key = "Root",
            startDestination = s1,
            destinations = arrayOf(s1, s2)
        )
        Navigation(nc, Modifier.fillMaxSize())
    }
}