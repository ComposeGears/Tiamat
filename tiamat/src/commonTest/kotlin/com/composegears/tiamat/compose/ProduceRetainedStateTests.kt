@file:OptIn(ExperimentalTestApi::class, TiamatExperimentalApi::class, ExperimentalCoroutinesApi::class)

package com.composegears.tiamat.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.act
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.test.*

class ProduceRetainedStateTests {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    companion object {

        private class Recorder {
            val events = mutableListOf<String>()
            val values = Channel<Int>(Channel.UNLIMITED)
            var producerContext: CoroutineContext? = null
            var producerChildJob: Job? = null
        }

        private val LocalRecorder = staticCompositionLocalOf<Recorder> { error("Recorder not initialized") }
        private val LocalKey = staticCompositionLocalOf { mutableIntStateOf(0) }

        private val RootScreen by navDestination<Unit> {
            Text("Root", modifier = Modifier.testTag("Root"))
        }

        private val OtherScreen by navDestination<Unit> {
            Text("Other", modifier = Modifier.testTag("Other"))
        }

        private val ProducerScreen by navDestination<Unit> {
            val recorder = LocalRecorder.current
            val state by produceRetainedState(0) {
                recorder.events += "start"
                recorder.producerContext = coroutineContext
                recorder.producerChildJob = launch {
                    try {
                        for (v in recorder.values) value = v
                    } finally {
                        recorder.events += "child end"
                    }
                }
                awaitDispose { recorder.events += "dispose" }
            }
            Text("Value: $state", modifier = Modifier.testTag("Value"))
        }

        private val KeyedProducerScreen by navDestination<Unit> {
            val recorder = LocalRecorder.current
            val key = LocalKey.current.intValue
            val state by produceRetainedState(0, key) {
                recorder.events += "start $key"
                launch {
                    try {
                        for (v in recorder.values) value = v
                    } finally {
                        recorder.events += "child end $key"
                    }
                }
                awaitDispose { recorder.events += "dispose $key" }
            }
            Text("Value: $state", modifier = Modifier.testTag("Value"))
        }

        private val AllDestinations = arrayOf<NavDestination<*>>(
            RootScreen,
            OtherScreen,
            ProducerScreen,
            KeyedProducerScreen,
        )
    }

    private fun ComposeUiTest.setNavContent(
        recorder: Recorder,
        key: MutableIntState = mutableIntStateOf(0),
    ): NavController {
        lateinit var navController: NavController
        setContent {
            CompositionLocalProvider(
                LocalRecorder provides recorder,
                LocalKey provides key,
            ) {
                navController = rememberNavController(startDestination = RootScreen)
                Navigation(
                    navController = navController,
                    destinations = AllDestinations,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        waitForIdle()
        return navController
    }

    private fun ComposeUiTest.assertValue(expected: Int) {
        assertEquals("Value: $expected", onNodeWithTag("Value").readText())
    }

    @Test
    fun `produceRetainedState - starts with initial value and renders produced values`() = runComposeUiTest {
        val recorder = Recorder()
        val nc = setNavContent(recorder)

        act { nc.navigate(ProducerScreen) }
        assertEquals(listOf("start"), recorder.events)
        assertValue(0)

        act { recorder.values.trySend(1) }
        assertValue(1)
        act { recorder.values.trySend(2) }
        assertValue(2)
        assertEquals(listOf("start"), recorder.events)
    }

    @Test
    fun `produceRetainedState - keeps producing while entry is in backstack and disposes on pop`() =
        runComposeUiTest {
            val recorder = Recorder()
            val nc = setNavContent(recorder)

            act { nc.navigate(ProducerScreen) }
            act { recorder.values.trySend(1) }
            assertValue(1)

            // produce value while the entry is in backstack
            act { nc.navigate(OtherScreen) }
            onNodeWithTag("Value").assertDoesNotExist()
            act { recorder.values.trySend(5) }

            // return back - produced value is shown, producer is not restarted
            act { nc.back() }
            assertValue(5)
            assertEquals(listOf("start"), recorder.events, "Expected: producer is not restarted or disposed")

            // pop - producer and its children are canceled
            act { nc.back() }
            assertEquals(listOf("start", "child end", "dispose"), recorder.events)

            // open again - new producer starts from initial value
            act { nc.navigate(ProducerScreen) }
            assertEquals(listOf("start", "child end", "dispose", "start"), recorder.events)
            assertValue(0)
        }

    @Test
    fun `produceRetainedState with keys - restarts producer on key change and keeps the last value`() =
        runComposeUiTest {
            val recorder = Recorder()
            val key = mutableIntStateOf(0)
            val nc = setNavContent(recorder, key)

            act { nc.navigate(KeyedProducerScreen) }
            act { recorder.values.trySend(7) }
            assertValue(7)
            assertEquals(listOf("start 0"), recorder.events)

            // previous producer (with its children) is canceled immediately, the state is not reset
            act { key.intValue = 1 }
            assertEquals(
                listOf("child end 0", "dispose 0", "start 0", "start 1"),
                recorder.events.sorted()
            )
            assertValue(7)

            // only the new producer receives values
            act { recorder.values.trySend(8) }
            assertValue(8)

            // pop - the current producer is canceled
            act { nc.back() }
            assertEquals(1, recorder.events.count { it == "dispose 1" }, "Log: ${recorder.events}")
            assertEquals(1, recorder.events.count { it == "dispose 0" }, "Log: ${recorder.events}")
        }

    @Test
    fun `produceRetainedState - runs on main dispatcher and owns launched coroutines`() = runComposeUiTest {
        val recorder = Recorder()
        val nc = setNavContent(recorder)

        act { nc.navigate(ProducerScreen) }
        val context = assertNotNull(recorder.producerContext)
        assertEquals<Any?>(Dispatchers.Main.immediate, context[ContinuationInterceptor])
        assertSame(context[Job], recorder.producerChildJob?.parent, "Expected: child is owned by producer")
    }
}
