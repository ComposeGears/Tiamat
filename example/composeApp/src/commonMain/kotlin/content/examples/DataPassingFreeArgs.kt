@file:Suppress("MatchingDeclarationName")

package content.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.freeArgs
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import content.examples.common.BackButton
import content.examples.common.SimpleScreen

class FreeArgsData

val DataPassingFreeArgsRoot by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Data passing: free args") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button({ navController.navigate(DataPassingFreeArgsScreen, freeArgs = 1) }) {
                Text("Pass `Int` to next screen")
            }
            Button({ navController.navigate(DataPassingFreeArgsScreen, freeArgs = 1f) }) {
                Text("Pass `Float` to next screen")
            }
            Button({ navController.navigate(DataPassingFreeArgsScreen, freeArgs = "String") }) {
                Text("Pass `String` to next screen")
            }
            Button({ navController.navigate(DataPassingFreeArgsScreen, freeArgs = FreeArgsData()) }) {
                Text("Pass `Class` to next screen")
            }
        }
    }
}

val DataPassingFreeArgsScreen by navDestination<Unit> {
    val navController = navController()
    val args = freeArgs<Any?>()
    SimpleScreen("Data passing: free args - args") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Received free args data: $args", style = MaterialTheme.typography.bodyMedium)
            BackButton(onClick = { navController.back() })
        }
    }
}