package composegears.tiamat.sample.content.state

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composegears.tiamat.compose.*
import composegears.tiamat.sample.content.state.ui.ViewModelScreen1Content
import composegears.tiamat.sample.content.state.ui.ViewModelScreen2Content
import composegears.tiamat.sample.content.state.ui.ViewModelScreen3Content
import composegears.tiamat.sample.ui.AppTheme
import composegears.tiamat.sample.ui.Screen
import composegears.tiamat.sample.ui.ScreenInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

val StateViewModel by navDestination(ScreenInfo()) {
    Screen("ViewModel") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "View Models nav controller",
                startDestination = StateViewModelScreen1,
            )
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    StateViewModelScreen1,
                    StateViewModelScreen2,
                    StateViewModelScreen3,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val StateViewModelScreen1 by navDestination {
    val nc = navController()

    // this is shared (bound to navController instead of screen) view model
    val sharedViewModel = viewModel(nc) { SharedSimpleViewModel() }

    ViewModelScreen1Content(
        sharedViewModelCounter = sharedViewModel.counter.collectAsState().value,
        onNext = { nc.navigate(StateViewModelScreen2) }
    )
}

private val StateViewModelScreen2 by navDestination {
    val nc = navController()
    // this is regular view model bound to the screen
    val viewModel = viewModel { SimpleViewModel() }
    // this is shared (bound to navController instead of screen) view model
    val sharedViewModel = viewModel(nc) { SharedSimpleViewModel() }
    // this is saveable view model
    val saveableViewModel = viewModel { SavedStateHandleViewModel(createSavedStateHandle()) }

    ViewModelScreen2Content(
        viewModelCounter = viewModel.counter.collectAsState().value,
        sharedViewModelCounter = sharedViewModel.counter.collectAsState().value,
        saveableViewModelCounter = saveableViewModel.counter.collectAsState().value,
        onBack = { nc.back() },
        onNext = { nc.navigate(StateViewModelScreen3) }
    )
}

private val StateViewModelScreen3 by navDestination {
    val nc = navController()

    // this is shared (bound to navController instead of screen) view model
    val sharedViewModel = viewModel(nc) { SharedSimpleViewModel() }

    ViewModelScreen3Content(
        sharedViewModelCounter = sharedViewModel.counter.collectAsState().value,
        onBack = { nc.back() }
    )
}

// ---------------------- view models ---------------------------

class SimpleViewModel : ViewModel() {
    private val _counter = MutableStateFlow(0)
    val counter = _counter.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _counter.value++
                delay(1000)
            }
        }
    }
}

class SharedSimpleViewModel : ViewModel() {
    private val _counter = MutableStateFlow(0)
    val counter = _counter.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _counter.value++
                delay(1000)
            }
        }
    }
}

class SavedStateHandleViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var counter = savedStateHandle.getStateFlow("counter", 0)

    init {
        viewModelScope.launch {
            while (isActive) {
                savedStateHandle["counter"] = counter.value + 1
                delay(1000)
            }
        }
    }
}

@Preview
@Composable
private fun StateViewModelPreview() = AppTheme {
    TiamatPreview(destination = StateViewModel)
}
