package content.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import com.composegears.tiamat.ComposeViewModel
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberViewModel
import content.examples.common.SimpleScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ViewModelsRootViewModel : ComposeViewModel() {
    private val _counter = MutableStateFlow(1)
    val counter = _counter.asStateFlow()

    fun inc() {
        _counter.tryEmit(_counter.value + 1)
    }

    fun dec() {
        _counter.tryEmit(_counter.value - 1)
    }
}

val ViewModelsRoot by navDestination<Unit> {
    val navController = navController()
    val viewModel = rememberViewModel { ViewModelsRootViewModel() }
    SimpleScreen("View Model's") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ViewModel address:\n$viewModel")
            val count by viewModel.counter.collectAsState()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button({ viewModel.dec() }) { Text("-") }
                Text("Value: $count")
                Button({ viewModel.inc() }) { Text("+") }
            }
            Text("\nYou can open another screen and go back to verify")
            Text("that there is same instance of ViewModel")
            Button({ navController.navigate(ViewModelsScreen) }) {
                Text("Go forward -> ")
            }
        }
    }
}

val ViewModelsScreen by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("View Model's - Screen") {
        Button(onClick = navController::back) {
            Text(" <- Go back")
        }
    }
}
