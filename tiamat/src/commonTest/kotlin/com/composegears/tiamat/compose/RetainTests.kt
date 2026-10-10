@file:OptIn(ExperimentalTestApi::class)

package com.composegears.tiamat.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.retain.RetainObserver
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.composegears.tiamat.act
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.utils.extensions.OverlaysExtension
import kotlin.test.Test
import kotlin.test.assertEquals

class RetainTests {

    companion object {

        private class Logger {
            val messages = mutableListOf<String>()
        }

        private class Tracker(val name: String, val logger: Logger) : RetainObserver {
            override fun onRetained() {
                logger.messages += "retained $name"
            }

            override fun onEnteredComposition() = Unit
            override fun onExitedComposition() = Unit

            override fun onRetired() {
                logger.messages += "retired $name"
            }

            override fun onUnused() {
                logger.messages += "unused $name"
            }
        }

        private val LocalLogger = staticCompositionLocalOf<Logger> { error("Logger not initialized") }
        private val LocalKey = staticCompositionLocalOf { mutableIntStateOf(0) }
        private val LocalFlag = staticCompositionLocalOf { mutableStateOf(true) }

        private val RootScreen by navDestination<Unit> {
            Text("Root", modifier = Modifier.testTag("Root"))
        }

        private val OtherScreen by navDestination<Unit> {
            Text("Other", modifier = Modifier.testTag("Other"))
        }

        private val RetainScreen by navDestination<Unit> {
            val logger = LocalLogger.current
            retain { Tracker("A", logger) }
            Text("Retain", modifier = Modifier.testTag("Retain"))
        }

        private val KeyedRetainScreen by navDestination<Unit> {
            val logger = LocalLogger.current
            val key = LocalKey.current.intValue
            // explicit type: as the last expression `retain` would infer `Unit` from the lambda return type
            retain<Tracker>(key) { Tracker("key $key", logger) }
        }

        private val ConditionalRetainScreen by navDestination<Unit> {
            val logger = LocalLogger.current
            if (LocalFlag.current.value) {
                retain<Tracker> { Tracker("C", logger) }
            }
        }

        private val NestedHostScreen by navDestination<Unit> {
            val nc = rememberNavController(
                key = "nested",
                startDestination = RetainScreen,
            )
            Navigation(
                navController = nc,
                destinations = arrayOf(RetainScreen),
                modifier = Modifier.fillMaxSize(),
            )
        }

        private val OverlayScreen: NavDestination<Unit> by navDestination {
            val logger = LocalLogger.current
            val overlayNavController = navController()
            retain { Tracker("O", logger) }
            Button(
                onClick = { overlayNavController.back() },
                modifier = Modifier.testTag("CloseOverlay"),
            ) {
                Text("Close overlay")
            }
        }

        private val OverlayHostScreen: NavDestination<Unit> by navDestination(
            OverlaysExtension(destinations = arrayOf(OverlayScreen))
        ) {
            val logger = LocalLogger.current
            val overlayNavController = ext<OverlaysExtension>()!!.overlayNavController()
            retain { Tracker("H", logger) }
            Column {
                Text("Host", modifier = Modifier.testTag("Host"))
                Button(
                    onClick = { overlayNavController.navigate(OverlayScreen) },
                    modifier = Modifier.testTag("OpenOverlay"),
                ) {
                    Text("Open overlay")
                }
            }
        }

        private val AllDestinations = arrayOf<NavDestination<*>>(
            RootScreen,
            OtherScreen,
            RetainScreen,
            KeyedRetainScreen,
            ConditionalRetainScreen,
            NestedHostScreen,
            OverlayHostScreen,
        )
    }

    private fun ComposeUiTest.setNavContent(
        logger: Logger,
        startDestination: NavDestination<*> = RootScreen,
        key: MutableIntState = mutableIntStateOf(0),
        flag: MutableState<Boolean> = mutableStateOf(true),
    ): NavController {
        lateinit var navController: NavController
        setContent {
            CompositionLocalProvider(
                LocalLogger provides logger,
                LocalKey provides key,
                LocalFlag provides flag,
            ) {
                navController = rememberNavController(startDestination = startDestination)
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

    @Test
    fun `retain - keeps value while entry is in backstack and retires it when entry is popped`() = runComposeUiTest {
        val logger = Logger()
        val nc = setNavContent(logger)

        act { nc.navigate(RetainScreen) }
        assertEquals(listOf("retained A"), logger.messages)

        // forward & back - value is kept, not re-created
        act { nc.navigate(OtherScreen) }
        act { nc.back() }
        onNodeWithTag("Retain").assertExists()
        assertEquals(listOf("retained A"), logger.messages)

        // pop - value is retired
        act { nc.back() }
        assertEquals(listOf("retained A", "retired A"), logger.messages)

        // open again - new value
        act { nc.navigate(RetainScreen) }
        assertEquals(listOf("retained A", "retired A", "retained A"), logger.messages)
    }

    @Test
    fun `retain with key - retires previous value on key change and does not restore it`() = runComposeUiTest {
        val logger = Logger()
        val key = mutableIntStateOf(0)
        setNavContent(logger, startDestination = KeyedRetainScreen, key = key)
        assertEquals(listOf("retained key 0"), logger.messages)

        act { key.intValue = 1 }
        assertEquals(listOf("retained key 0", "retained key 1", "retired key 0"), logger.messages.sorted())

        // returning to the previous key creates a new value
        act { key.intValue = 0 }
        assertEquals(2, logger.messages.count { it == "retained key 0" }, "Log: ${logger.messages}")
        assertEquals(1, logger.messages.count { it == "retired key 1" }, "Log: ${logger.messages}")
    }

    @Test
    fun `retain - retires value removed from composition while entry is displayed`() = runComposeUiTest {
        val logger = Logger()
        val flag = mutableStateOf(true)
        val nc = setNavContent(logger, startDestination = ConditionalRetainScreen, flag = flag)
        assertEquals(listOf("retained C"), logger.messages)

        act { flag.value = false }
        assertEquals(listOf("retained C", "retired C"), logger.messages)

        act { flag.value = true }
        assertEquals(listOf("retained C", "retired C", "retained C"), logger.messages)

        // the same value is kept when entry goes to backstack
        act { nc.navigate(OtherScreen) }
        act { nc.back() }
        assertEquals(listOf("retained C", "retired C", "retained C"), logger.messages)
    }

    @Test
    fun `retain - retires values of backstack entry removed by editNavStack`() = runComposeUiTest {
        val logger = Logger()
        val nc = setNavContent(logger)

        act { nc.navigate(RetainScreen) }
        act { nc.navigate(OtherScreen) }
        assertEquals(listOf("retained A"), logger.messages)

        act { nc.editNavStack { stack -> stack.filter { it.destination != RetainScreen } } }
        onNodeWithTag("Other").assertExists()
        assertEquals(listOf("retained A", "retired A"), logger.messages)
    }

    @Test
    fun `retain - retires values of replaced entry`() = runComposeUiTest {
        val logger = Logger()
        val nc = setNavContent(logger)

        act { nc.navigate(RetainScreen) }
        act { nc.replace(OtherScreen) }
        onNodeWithTag("Other").assertExists()
        assertEquals(listOf("retained A", "retired A"), logger.messages)
    }

    @Test
    fun `retain - nested navController keeps values while parent entry is in backstack`() = runComposeUiTest {
        val logger = Logger()
        val nc = setNavContent(logger)

        act { nc.navigate(NestedHostScreen) }
        assertEquals(listOf("retained A"), logger.messages)

        act { nc.navigate(OtherScreen) }
        act { nc.back() }
        onNodeWithTag("Retain").assertExists()
        assertEquals(listOf("retained A"), logger.messages)

        // pop parent entry - nested values are retired
        act { nc.back() }
        assertEquals(listOf("retained A", "retired A"), logger.messages)
    }

    @Test
    fun `retain - retires values of popped entry only after exit transition ends`() = runComposeUiTest {
        val logger = Logger()
        val nc = setNavContent(logger)
        act { nc.navigate(RetainScreen) }

        mainClock.autoAdvance = false
        runOnIdle { nc.back() }
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeByFrame()
        onNodeWithTag("Retain").assertExists()
        assertEquals(listOf("retained A"), logger.messages, "Expected: value is alive during transition")

        mainClock.advanceTimeBy(1_000)
        onNodeWithTag("Retain").assertDoesNotExist()
        assertEquals(listOf("retained A", "retired A"), logger.messages)
    }

    @Test
    fun `retain - overlay does not affect host values and its own values are retired on close`() = runComposeUiTest {
        val logger = Logger()
        setNavContent(logger, startDestination = OverlayHostScreen)
        assertEquals(listOf("retained H"), logger.messages)

        onNodeWithTag("OpenOverlay").performClick()
        waitForIdle()
        onNodeWithTag("Host").assertExists()
        assertEquals(listOf("retained H", "retained O"), logger.messages)

        onNodeWithTag("CloseOverlay").performClick()
        waitForIdle()
        onNodeWithTag("Host").assertExists()
        assertEquals(listOf("retained H", "retained O", "retired O"), logger.messages)
    }
}
