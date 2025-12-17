package composegears.tiamat.sample.content.state

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.MutableSavedState
import com.composegears.tiamat.navigation.asStateFlow
import com.composegears.tiamat.navigation.recordOf
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.ui.*
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
    val sharedViewModel = viewModel<StateViewModelSharedSimpleViewModel>(nc)

    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text(
                text = "sharedViewModel \$counter = ${sharedViewModel.counter.collectAsState().value}",
                textAlign = TextAlign.Center
            )
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.KeyboardArrowRight,
                onClick = { nc.navigate(StateViewModelScreen2) }
            )
        }
    }
}

private val StateViewModelScreen2 by navDestination {
    val nc = navController()
    // this is regular view model bound to the screen
    val viewModel = viewModel<StateViewModelSimpleViewModel>()
    // this is shared (bound to navController instead of screen) view model
    val sharedViewModel = viewModel<StateViewModelSharedSimpleViewModel>(nc)
    // this is saveable view model
    val saveableViewModel = saveableViewModel { StateViewModelSaveableViewModel(it) }

    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text(
                text = "viewModel \$counter = ${viewModel.counter.collectAsState().value}\n" +
                    "sharedViewModel \$counter = ${sharedViewModel.counter.collectAsState().value}\n" +
                    "saveableViewModel \$counter = ${saveableViewModel.counter.collectAsState().value}",
                textAlign = TextAlign.Center
            )
            VSpacer()
            Text(
                text = """
                    Go next screen, then go back -> you will see that view models are restored
                    —————
                    Go back and reopen this screen -> view models will be recreated (except shared)
                    —————
                    Android: hide & re-open app -> saveableViewModel will restore it's saved state
                """.trimIndent(),
                textAlign = TextAlign.Center
            )
            VSpacer()
            Row {
                AppButton(
                    "Back",
                    startIcon = Icons.KeyboardArrowLeft,
                    onClick = { nc.back() }
                )
                HSpacer()
                AppButton(
                    "Next",
                    endIcon = Icons.KeyboardArrowRight,
                    onClick = { nc.navigate(StateViewModelScreen3) }
                )
            }
        }
    }
}

private val StateViewModelScreen3 by navDestination {
    val nc = navController()

    // this is shared (bound to navController instead of screen) view model
    val sharedViewModel = viewModel<StateViewModelSharedSimpleViewModel>(nc)

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 3", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text(
                text = "sharedViewModel \$counter = ${sharedViewModel.counter.collectAsState().value}",
                textAlign = TextAlign.Center
            )
            VSpacer()
            AppButton(
                "Back",
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

// ---------------------- view models ---------------------------

class StateViewModelSimpleViewModel : ViewModel() {
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

class StateViewModelSharedSimpleViewModel : ViewModel() {
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

private class StateViewModelSaveableViewModel(
    savedState: MutableSavedState
) : ViewModel() {

    private var _counter = savedState.recordOf("counter", 0)
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

@Preview
@Composable
private fun StateViewModelPreview() = AppTheme {
    TiamatPreview(destination = StateViewModel)
}
