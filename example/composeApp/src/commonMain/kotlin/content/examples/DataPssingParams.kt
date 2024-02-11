package content.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.composegears.tiamat.navArgs
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import content.examples.common.SimpleScreen

data class NavArgsData(val counter: Int)

val DataPassingParamsRoot by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Data passing: Params") {
        var counter by rememberSaveable { mutableIntStateOf(0) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button({ counter-- }) { Text("-") }
                Text("Value: $counter")
                Button({ counter++ }) { Text("+") }
            }
            Button({ navController.navigate(DataPassingParamsScreen, NavArgsData(counter)) }) {
                Text("Pass data to next screen")
            }
        }
    }
}

val DataPassingParamsScreen by navDestination<NavArgsData> {
    val navController = navController()
    val args = navArgs()
    SimpleScreen("Data passing: Params - Data") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Received data: $args")
            Button(onClick = { navController.back() }) {
                Text(" <- Go back")
            }
        }
    }
}