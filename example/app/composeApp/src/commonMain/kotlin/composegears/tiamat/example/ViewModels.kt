package composegears.tiamat.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import composegears.tiamat.example.ui.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

val ViewModelsRoot by navDestination<Unit>(webPathExtension()) {
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
    val saveableViewModel = rememberSaveableViewModel { SaveableViewModel(it) }
    val sharedViewModel = rememberSharedViewModel { SharedViewModel() }
    SimpleScreen("ViewModel's - Screen 1") {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewModelInfoCard {
                val timer by screenViewModel.timer.collectAsState()
                ViewModelInfoBody(
                    name = "ScreenViewModel",
                    hashCode = screenViewModel.hashCode(),
                    timer = timer
                )
                Spacer()
                val saveableTimer by saveableViewModel.timer.collectAsState()
                ViewModelInfoBody(
                    name = "SaveableViewModel",
                    hashCode = saveableViewModel.hashCode(),
                    timer = saveableTimer
                )
                Spacer()
                val sharedTimer by sharedViewModel.timer.collectAsState()
                ViewModelInfoBody(
                    name = "SharedViewModel",
                    hashCode = sharedViewModel.hashCode(),
                    timer = sharedTimer
                )
            }
            Spacer()
            TextCaption(
                "You can open another screen and go back to verify that there is same " +
                    "instance of ScreenViewModel and SharedViewModel"
            )
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
        }
    }
}

val ViewModelsScreen2 by navDestination<Unit> {
    val navController = navController()
    val sharedViewModel = rememberSharedViewModel(provider = ::SharedViewModel)

    SimpleScreen("ViewModel's - Screen 2") {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val sharedTimerValue by sharedViewModel.timer.collectAsState()
            ViewModelInfo("SharedViewModel", hashCode = sharedViewModel.hashCode(), timer = sharedTimerValue)
            Spacer()
            BackButton(onClick = navController::back)
        }
    }
}

internal class SharedViewModel : TiamatViewModel() {
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

internal class SaveableViewModel(savedState: SavedState?) : TiamatViewModel(), Saveable {

    private val _timer = MutableStateFlow(0)
    val timer = _timer.asStateFlow()

    init {
        _timer.value = savedState?.get("count") as? Int? ?: 0
        viewModelScope.launch {
            while (isActive) {
                _timer.value++
                delay(1000)
            }
        }
    }

    override fun saveToSaveState() = mapOf("count" to timer.value)
}

// Will be attached to NavController
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