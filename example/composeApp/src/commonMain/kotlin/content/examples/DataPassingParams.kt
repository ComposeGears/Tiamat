package content.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.navArgs
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import content.examples.common.BackButton
import content.examples.common.CircleButton
import content.examples.common.SimpleScreen

data class NavArgsData(val counter: Int)

val DataPassingParamsRoot by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Data passing: Params") {
        var counter by rememberSaveable { mutableIntStateOf(0) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircleButton("-") { counter-- }
                Text(text = "Value: $counter", style = MaterialTheme.typography.bodyMedium)
                CircleButton("+") { counter++ }
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Received data: $args", style = MaterialTheme.typography.bodyMedium)
            BackButton(onClick = { navController.back() })
        }
    }
}