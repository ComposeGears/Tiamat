package composegears.tiamat.sample.koin

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
import com.composegears.tiamat.koin.koinSaveableTiamatViewModel
import com.composegears.tiamat.koin.koinSharedTiamatViewModel
import com.composegears.tiamat.koin.koinTiamatViewModel
import composegears.tiamat.example.ui.core.*
import composegears.tiamat.sample.koin.viewmodel.KoinDetailViewModel
import composegears.tiamat.sample.koin.viewmodel.KoinDetailViewModel.Companion.KoinDetailState.Loading
import composegears.tiamat.sample.koin.viewmodel.KoinDetailViewModel.Companion.KoinDetailState.Success
import composegears.tiamat.sample.koin.viewmodel.SaveableViewModel
import composegears.tiamat.sample.koin.viewmodel.SharedViewModel
import org.koin.core.parameter.parametersOf

val KoinIntegrationScreen by navDestination<Unit>(webPathExtension()) {
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
    val saveableViewModel = koinSaveableTiamatViewModel<SaveableViewModel>()
    val sharedViewModel = koinSharedTiamatViewModel<SharedViewModel>()

    SimpleScreen("Koin integration") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val timer1 by sharedViewModel.timer.collectAsState()
            val timer2 by saveableViewModel.timer.collectAsState()
            ViewModelInfo("SharedViewModel", hashCode = sharedViewModel.hashCode(), timer = timer1)
            Spacer(8.dp)
            ViewModelInfo("SaveableViewModel", hashCode = saveableViewModel.hashCode(), timer = timer2)
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
                    ViewModelInfo("SharedViewModel", hashCode = sharedViewModel.hashCode(), timer = timer)
                    TextBody(text = detailState.result)
                    BackButton(onClick = navController::back)
                }
            }
        }
    }
}
