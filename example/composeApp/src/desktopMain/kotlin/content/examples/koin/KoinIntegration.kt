package content.examples.koin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import com.composegears.tiamat.koin.koinSharedTiamatViewModel
import com.composegears.tiamat.koin.koinTiamatViewModel
import content.examples.SharedViewModel
import content.examples.common.*
import content.examples.koin.KoinDetailViewModel.Companion.KoinDetailState.Loading
import content.examples.koin.KoinDetailViewModel.Companion.KoinDetailState.Success
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

val KoinIntegration by navDestination<Unit> {
    val navController = rememberNavController(
        key = "KoinNavController",
        startDestination = KoinListScreen,
        destinations = arrayOf(KoinListScreen, KoinDetailScreen)
    )
    Navigation(
        navController = navController,
        modifier = Modifier.fillMaxSize()
    )
}

private val KoinListScreen by navDestination<Unit> {
    val navController = navController()
    val sharedViewModel = koinSharedTiamatViewModel<SharedViewModel>()

    SimpleScreen("Koin integration") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val timer by sharedViewModel.timer.collectAsState()
            ViewModelInfo(hashCode = sharedViewModel.hashCode(), timer = timer)
            Spacer(8.dp)
            TextCaption("Pass value to detail screen")
            NextButton(
                text = "Open detail",
                onClick = {
                    navController.navigate(
                        dest = KoinDetailScreen,
                        navArgs = "dynamic_argument"
                    )
                }
            )
            BackButton(onClick = navController::back)
        }
    }
}

private val KoinDetailScreen by navDestination<String> {
    val params = navArgs()
    val navController = navController()
    val viewModel = koinTiamatViewModel<KoinDetailViewModel> { parametersOf(params) }
    val sharedViewModel = koinSharedTiamatViewModel<SharedViewModel>()
    val state by viewModel.state.collectAsState()

    SimpleScreen("KoinDetail Screen") {
        when (val detailState = state) {
            is Loading -> CircularProgressIndicator()
            is Success -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val timer by sharedViewModel.timer.collectAsState()
                    ViewModelInfo(hashCode = sharedViewModel.hashCode(), timer = timer)
                    TextBody(text = detailState.result)
                    BackButton(onClick = navController::back)
                }
            }
        }
    }
}

internal class KoinDetailViewModel(private val params: String) : TiamatViewModel() {

    companion object {
        internal sealed interface KoinDetailState {
            data object Loading : KoinDetailState
            data class Success(val result: String) : KoinDetailState
        }
    }

    private val _state = MutableStateFlow<KoinDetailState>(Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(500)

            val result = "$params: ${hashCode()}"
            _state.update { Success(result) }
        }
    }
}