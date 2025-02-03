package composegears.tiamat.example.content.content.architecture

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import composegears.tiamat.example.ui.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

val ArchViewModel by navDestination<Unit>(ScreenInfo()) {
    Screen("ViewModel") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "View Models nav controller",
                startDestination = ArchViewModelScreen1,
                destinations = arrayOf(
                    ArchViewModelScreen1,
                    ArchViewModelScreen2,
                    ArchViewModelScreen3,
                )
            )
            Navigation(
                nc,
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val ArchViewModelScreen1 by navDestination<Unit> {
    // this is shared (bound to navController instead of screen) view model
    val sharedViewModel = rememberSharedViewModel { ArchViewModelSimpleViewModel() }

    val nc = navController()
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
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(ArchViewModelScreen2) }
            )
        }
    }
}

private val ArchViewModelScreen2 by navDestination<Unit> {
    // this is regular view model bound to the screen
    val viewModel = rememberViewModel { ArchViewModelSimpleViewModel() }
    // this is shared (bound to navController instead of screen) view model
    val sharedViewModel = rememberSharedViewModel { ArchViewModelSimpleViewModel() }
    // this is saveable view model, the state will be saved via saveState logic similar to `rememberSaveable`
    val saveableViewModel = rememberSaveableViewModel { savedState -> ArchViewModelSaveableViewModel(savedState) }

    val nc = navController()
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
                    startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    onClick = { nc.back() }
                )
                HSpacer()
                AppButton(
                    "Next",
                    endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    onClick = { nc.navigate(ArchViewModelScreen3) }
                )
            }
        }
    }
}

private val ArchViewModelScreen3 by navDestination<Unit> {
    // this is shared (bound to navController instead of screen) view model
    val sharedViewModel = rememberSharedViewModel { ArchViewModelSimpleViewModel() }

    val nc = navController()
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
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

// ---------------------- view models ---------------------------

private class ArchViewModelSimpleViewModel : TiamatViewModel() {
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

private class ArchViewModelSaveableViewModel(savedState: SavedState?) : TiamatViewModel(), Saveable {

    private val _counter = MutableStateFlow(0)
    val counter = _counter.asStateFlow()

    init {
        _counter.value = savedState?.get("count") as? Int? ?: 0
        viewModelScope.launch {
            while (isActive) {
                _counter.value++
                delay(1000)
            }
        }
    }

    override fun saveToSaveState() = mapOf("count" to counter.value)
}