package content.examples.koin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import com.composegears.tiamat.koin.koinSharedTiamatViewModel
import com.composegears.tiamat.koin.koinTiamatViewModel
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
    val sharedViewModel = koinSharedTiamatViewModel<KoinSharedViewModel>()
    val launchCount by sharedViewModel.launchCount.collectAsState()

    LaunchedEffect(Unit) {
        sharedViewModel.increment()
    }

    SimpleScreen("Koin integration") {
        TextBody(
            text = "Launch count: $launchCount",
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
    val sharedViewModel = koinSharedTiamatViewModel<KoinSharedViewModel>()
    val launchCount by sharedViewModel.launchCount.collectAsState()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        sharedViewModel.increment()
    }

    SimpleScreen("KoinDetail Screen") {
        TextBody(
            text = "Launch count: $launchCount",
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
        )
        when (val detailState = state) {
            is Loading -> CircularProgressIndicator()
            is Success -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = detailState.result)
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
            delay(1000)

            val result = "$params:${hashCode()}"
            _state.update { Success(result) }
        }
    }
}

internal class KoinSharedViewModel : TiamatViewModel() {

    private val _launchCount = MutableStateFlow(0)
    val launchCount = _launchCount.asStateFlow()

    fun increment() {
        _launchCount.value++
    }
}