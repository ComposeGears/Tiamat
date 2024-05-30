@file:Suppress("MatchingDeclarationName")

package composegears.tiamat.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.navResult
import composegears.tiamat.example.ui.core.*

data class NavResult(val counter: Int)

val DataPassingResultRoot by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Data passing: Result") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val result =
                navResult<NavResult>() // use <Any?> + when(result)... if there multiple results types
            Text(text = "Last result: $result", style = MaterialTheme.typography.bodyMedium)

            NextButton(
                text = "Open for result",
                onClick = { navController.navigate(DataPassingResultScreen) }
            )
        }
    }
}

val DataPassingResultScreen by navDestination<Unit> {
    val navController = navController()
    var counter by remember { mutableIntStateOf(0) }
    SimpleScreen("Data passing: Result - Create result") {
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
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ExitButton(
                    text = "Close",
                    onClick = navController::back
                )
                BackButton(
                    text = "Back with result",
                    onClick = { navController.back(NavResult(counter)) }
                )
            }
        }
    }
}