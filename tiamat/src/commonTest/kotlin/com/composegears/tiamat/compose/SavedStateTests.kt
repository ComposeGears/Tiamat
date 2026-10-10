@file:OptIn(ExperimentalTestApi::class)

package com.composegears.tiamat.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composegears.tiamat.act
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.SavedState
import com.composegears.tiamat.readText
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

class SavedStateTests {

    companion object {

        private class HandleViewModel(handle: SavedStateHandle) : ViewModel() {
            val value: String = handle.get<String>("value")
                ?: "handle-${Random.nextInt(999_999)}".also { handle["value"] = it }
        }

        private val viewModels = mutableListOf<ViewModel>()

        private val StateScreen by navDestination<Unit> {
            val probe = rememberSaveable { "probe-${Random.nextInt(999_999)}" }
            val vm = viewModel { HandleViewModel(createSavedStateHandle()) }
            viewModels += vm
            Column {
                Text("Probe: $probe", modifier = Modifier.testTag("Probe"))
                Text("Handle: ${vm.value}", modifier = Modifier.testTag("Handle"))
            }
        }

        private val OtherScreen by navDestination<Unit> {
            Text("Other", modifier = Modifier.testTag("Other"))
        }
    }

    /**
     * Sets content with a navController that is saved on dispose and restored on show,
     * simulating the process death.
     */
    private fun ComposeUiTest.setRestorableNavContent(show: MutableState<Boolean>): () -> NavController {
        var navController: NavController? = null
        var savedState: SavedState? = null
        setContent {
            if (show.value) {
                val nc = rememberNavController(
                    key = "saved-state-test",
                    startDestination = StateScreen,
                    savedState = savedState,
                )
                navController = nc
                Navigation(
                    navController = nc,
                    destinations = arrayOf(StateScreen, OtherScreen),
                    modifier = Modifier.fillMaxSize(),
                )
                DisposableEffect(nc) {
                    onDispose { savedState = nc.saveToSavedState() }
                }
            }
        }
        waitForIdle()
        return { navController!! }
    }

    private fun ComposeUiTest.readState(): Pair<String, String> = Pair(
        assertNotNull(onNodeWithTag("Probe").readText()),
        assertNotNull(onNodeWithTag("Handle").readText()),
    )

    @Test
    fun `saved state - restores rememberSaveable and SavedStateHandle of displayed entry`() = runComposeUiTest {
        viewModels.clear()
        val show = mutableStateOf(true)
        setRestorableNavContent(show)
        val state = readState()
        val vm = viewModels.last()

        act { show.value = false }
        act { show.value = true }

        assertEquals(state, readState())
        assertNotSame(vm, viewModels.last(), "Expected: view model is re-created and restored from saved state")
    }

    @Test
    fun `saved state - restores rememberSaveable and SavedStateHandle of backstack entry`() = runComposeUiTest {
        viewModels.clear()
        val show = mutableStateOf(true)
        val navController = setRestorableNavContent(show)
        val state = readState()
        val vm = viewModels.last()

        act { navController().navigate(OtherScreen) }
        act { show.value = false }
        act { show.value = true }
        onNodeWithTag("Other").assertExists()

        act { navController().back() }
        assertEquals(state, readState())
        assertNotSame(vm, viewModels.last(), "Expected: view model is re-created and restored from saved state")
    }
}
