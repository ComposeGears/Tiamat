package composegears.tiamat.example.extra.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.koin.koinSaveableTiamatViewModel
import com.composegears.tiamat.koin.koinSharedTiamatViewModel
import com.composegears.tiamat.koin.koinTiamatViewModel
import com.composegears.tiamat.koin.tiamatViewModelOf
import com.composegears.tiamat.navigation.Saveable
import com.composegears.tiamat.navigation.SavedState
import com.composegears.tiamat.navigation.TiamatViewModel
import composegears.tiamat.example.ui.core.Screen
import composegears.tiamat.example.ui.core.ScreenInfo
import composegears.tiamat.example.ui.core.VSpacer
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val a3rdPKoinModule = module {
    tiamatViewModelOf(::A3rdPKoinSimpleViewModel)
    tiamatViewModelOf(::A3rdPKoinParamViewModel)
    tiamatViewModelOf(::A3rdPKoinSaveableViewModel)
    tiamatViewModelOf(::A3rdPKoinSharedViewModel)

    /**
     * or with old Koin syntax
     *
     * ```
     * tiamatViewModel { param -> DetailViewModel(params = param.get()) }
     * ```
     */
}

val A3rdPKoin by navDestination<Unit>(ScreenInfo()) {
    val simpleViewModel = koinTiamatViewModel<A3rdPKoinSimpleViewModel>()
    val paramViewModel = koinTiamatViewModel<A3rdPKoinParamViewModel> { parametersOf("params") }
    val saveableViewModel = koinSaveableTiamatViewModel<A3rdPKoinSaveableViewModel>()
    val sharedViewModel = koinSharedTiamatViewModel<A3rdPKoinSharedViewModel>()
    fun <T : Any> T.modelInfo() = this::class.simpleName + "@" + this.hashCode()
    Screen("Koin ViewModel") {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "This is koin view-models example, see code for more details",
                    textAlign = TextAlign.Center
                )
                VSpacer()
                Text(
                    text = """
                        simpleViewModel -> ${simpleViewModel.modelInfo()}
                        —————
                        paramViewModel -> ${paramViewModel.modelInfo()}
                        —————
                        saveableViewModel -> ${saveableViewModel.modelInfo()}
                        —————
                        sharedViewModel -> ${sharedViewModel.modelInfo()}
                    """.trimIndent(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ------------ view models -------------------

// simple view model
private class A3rdPKoinSimpleViewModel : TiamatViewModel()

// view model with params
@Suppress("RedundantVisibilityModifier", "unused")
private class A3rdPKoinParamViewModel(public val param: String) : TiamatViewModel()

// see ViewModels example in order to see save-load state demo
@Suppress("UNUSED_PARAMETER")
internal class A3rdPKoinSaveableViewModel(savedState: SavedState?) : TiamatViewModel(), Saveable {
    override fun saveToSaveState(): SavedState = emptyMap()
}

// see ViewModels example in order to see how to use shared view model
internal class A3rdPKoinSharedViewModel : TiamatViewModel()