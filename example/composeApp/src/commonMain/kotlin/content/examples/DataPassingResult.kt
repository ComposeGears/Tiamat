package content.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.navResult
import content.examples.common.SimpleScreen

data class NavResult(val counter: Int)

val DataPassingResultRoot by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Data passing: Result") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val result = navResult<NavResult>() // use <Any?> + when(result)... if there multiple results types
            Text("Last result: $result")
            Button(onClick = { navController.navigate(DataPassingResultScreen) }) {
                Text("Open for result ->")
            }
        }

    }
}

val DataPassingResultScreen by navDestination<Unit> {
    val navController = navController()
    var counter by remember { mutableIntStateOf(0) }
    SimpleScreen("Data passing: Result - Create result") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button({ counter-- }) { Text("-") }
                Text("Value: $counter")
                Button({ counter++ }) { Text("+") }
            }
            Button({ navController.back(NavResult(counter)) }) {
                Text("Back with result")
            }
            Button({ navController.back() }) {
                Text("Back without result")
            }
        }
    }
}