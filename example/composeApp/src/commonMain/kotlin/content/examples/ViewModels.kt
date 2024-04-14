package content.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import content.examples.common.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

val ViewModelsRoot by navDestination<Unit> {
    val viewModelsNavController = rememberNavController(
        destinations = arrayOf(ViewModelsScreen1, ViewModelsScreen2),
        startDestination = ViewModelsScreen1
    )
    Navigation(
        navController = viewModelsNavController,
        modifier = Modifier.fillMaxSize()
    )
}

val ViewModelsScreen1 by navDestination<Unit> {
    val navController = navController()
    val screenViewModel = rememberViewModel { ScreenViewModel() }
    val sharedViewModel = rememberSharedViewModel { SharedViewModel() }
    SimpleScreen("ViewModel's - Screen 1") {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp), RoundedCornerShape(10.dp))
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val timerValue by screenViewModel.timer.collectAsState()
                TextBodyLarge("ScreenViewModel")
                TextCaption("hashCode: ${screenViewModel.hashCode()}")
                TextCaption("timer: $timerValue")
                Spacer()

                val sharedTimerValue by sharedViewModel.timer.collectAsState()
                TextBody("SharedViewModel")
                TextCaption("hashCode: ${sharedViewModel.hashCode()}")
                TextCaption("timer: $sharedTimerValue")
            }
            Spacer()

            val count by screenViewModel.counter.collectAsState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircleButton(text = "-", onClick = screenViewModel::dec)
                Text(text = "Value: $count", style = MaterialTheme.typography.bodyMedium)
                CircleButton(text = "+", onClick = screenViewModel::inc)
            }

            Spacer()
            NextButton(onClick = { navController.navigate(ViewModelsScreen2) })
            TextCaption(
                "You can open another screen and go back to verify that there is same " +
                    "instance of ScreenViewModel and SharedViewModel"
            )
        }
    }
}

val ViewModelsScreen2 by navDestination<Unit> {
    val navController = navController()
    val sharedViewModel = rememberSharedViewModel { SharedViewModel() }

    SimpleScreen("ViewModel's - Screen 2") {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp), RoundedCornerShape(10.dp))
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val sharedTimerValue by sharedViewModel.timer.collectAsState()
                TextBodyLarge("SharedViewModel")
                TextCaption("hashCode: ${sharedViewModel.hashCode()}")
                TextCaption("timer: $sharedTimerValue")
            }
            Spacer()
            BackButton(onClick = navController::back)
        }
    }
}

private class ScreenViewModel : TiamatViewModel() {
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

// Will be attached to ViewModelsRoot navController
private class SharedViewModel : TiamatViewModel() {
    private val _timer = MutableStateFlow(0)
    val timer = _timer.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _timer.value++
                delay(1000)
            }
        }
    }
}