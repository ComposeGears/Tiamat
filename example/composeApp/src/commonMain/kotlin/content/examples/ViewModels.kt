package content.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.TiamatViewModel
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberViewModel
import content.examples.common.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ViewModelsRootViewModel : TiamatViewModel() {
    private val _timer = MutableStateFlow(0)
    private val _counter = MutableStateFlow(1)
    val counter = _counter.asStateFlow()
    val timer = _timer.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _timer.value++
                delay(1000)
            }
        }
    }

    fun inc() {
        _counter.update { _counter.value + 1 }
    }

    fun dec() {
        _counter.update { _counter.value - 1 }
    }
}

val ViewModelsRoot by navDestination<Unit> {
    val navController = navController()
    val viewModel = rememberViewModel { ViewModelsRootViewModel() }
    SimpleScreen("View Model's") {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val timerValue by viewModel.timer.collectAsState()
            TextBody("ViewModel address:\n$viewModel")
            TextBody("Timer Value:$timerValue")
            Spacer()

            val count by viewModel.counter.collectAsState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircleButton("-") { viewModel.dec() }
                Text(text = "Value: $count", style = MaterialTheme.typography.bodyMedium)
                CircleButton("+") { viewModel.inc() }
            }

            Spacer()
            NextButton(onClick = { navController.navigate(ViewModelsScreen) })
            TextCaption("You can open another screen and go back to verify")
            TextCaption("that there is same instance of ViewModel")
        }
    }
}

val ViewModelsScreen by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("View Model's - Screen") {
        BackButton(onClick = navController::back)
    }
}
