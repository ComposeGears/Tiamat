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
import com.composegears.tiamat.compose.*
import composegears.tiamat.sample.content.state.ui.ViewModelScreen1Content
import composegears.tiamat.sample.content.state.ui.ViewModelScreen2Content
import composegears.tiamat.sample.content.state.ui.ViewModelScreen3Content
import composegears.tiamat.sample.ui.AppTheme
import composegears.tiamat.sample.ui.Screen
import composegears.tiamat.sample.ui.ScreenInfo
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val koinModule = module {
    viewModelOf(::SimpleViewModel)
    viewModelOf(::SharedSimpleViewModel)
    viewModelOf(::SavedStateHandleViewModel)
}

object KoinInit {
    fun start() {
        startKoin {
            modules(koinModule)
        }
    }
}

internal val KoinViewModelScreen by navDestination(ScreenInfo()) {
    Screen("Koin ViewModel") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(startDestination = KoinViewModelScreen1)
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    KoinViewModelScreen1,
                    KoinViewModelScreen2,
                    KoinViewModelScreen3,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val KoinViewModelScreen1 by navDestination {
    val nc = navController()

    val sharedViewModel = koinViewModel<SharedSimpleViewModel>(viewModelStoreOwner = nc)

    ViewModelScreen1Content(
        sharedViewModelCounter = sharedViewModel.counter.collectAsState().value,
        onNext = { nc.navigate(KoinViewModelScreen2) }
    )
}

private val KoinViewModelScreen2 by navDestination {
    val nc = navController()

    // this is regular view model bound to the screen
    val viewModel = koinViewModel<SimpleViewModel>()

    // this is shared (bound to navController instead of screen) view model
    val sharedViewModel = koinViewModel<SharedSimpleViewModel>(viewModelStoreOwner = nc)

    // this is saveable view model
    val saveableViewModel = koinViewModel<SavedStateHandleViewModel>()

    ViewModelScreen2Content(
        viewModelCounter = viewModel.counter.collectAsState().value,
        sharedViewModelCounter = sharedViewModel.counter.collectAsState().value,
        saveableViewModelCounter = saveableViewModel.counter.collectAsState().value,
        onBack = { nc.back() },
        onNext = { nc.navigate(KoinViewModelScreen3) }
    )
}

private val KoinViewModelScreen3 by navDestination {
    val nc = navController()

    // this is shared (bound to navController instead of screen) view model
    val sharedViewModel = koinViewModel<SharedSimpleViewModel>(viewModelStoreOwner = nc)

    ViewModelScreen3Content(
        sharedViewModelCounter = sharedViewModel.counter.collectAsState().value,
        onBack = { nc.back() }
    )
}

@Preview
@Composable
private fun KoinViewModelScreenPreview() = AppTheme {
    TiamatPreview(destination = KoinViewModelScreen)
}
