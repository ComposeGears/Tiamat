@file:OptIn(ExperimentalTestApi::class)

package com.composegears.tiamat.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ViewModelLifecycleTests {

    companion object {

        private class Logger {
            val messages = mutableListOf<String>()

            fun log(message: String) {
                messages.add(message)
            }

            fun clear() {
                messages.clear()
            }
        }

        private val LocalLogger = staticCompositionLocalOf<Logger> { error("Logger not initialized") }

        private class VM(val tag: String, val logger: Logger) : ViewModel() {
            init {
                logger.log("$tag -> init")
            }

            override fun onCleared() {
                logger.log("$tag -> onCleared")
                super.onCleared()
            }
        }

        private val RootScreen by navDestination<Unit> {
            val rootNavController = navController()
            Column {
                Text("Root", modifier = Modifier.testTag("root"))
                Button(
                    onClick = { rootNavController.navigate(SubTree) },
                    modifier = Modifier.testTag("RootToSubTree")
                ) {
                    Text("Go to subtree")
                }
            }
        }

        private val SubTree by navDestination<Unit> {
            val nc = rememberNavController(
                key = "subtree",
                startDestination = SubScreen1,
            )
            Navigation(
                navController = nc,
                destinations = arrayOf(SubScreen1, SubScreen2),
                modifier = Modifier.fillMaxSize(),
            )
        }

        @Suppress("unused")
        private val SubScreen1 by navDestination<Unit> {
            val nc = navController()
            val logger = LocalLogger.current
            val vm1 = viewModel { VM("vm1", logger) }
            val vm2 = viewModel(nc) { VM("vm2", logger) }
            Column {
                Text("SubScreen1")
                Button(
                    onClick = { nc.navigate(SubScreen2) },
                    modifier = Modifier.testTag("Sub1ToSub2")
                ) {
                    Text("Replace with sub2")
                }
                Button(
                    onClick = { nc.back() },
                    modifier = Modifier.testTag("Sub1Back")
                ) {
                    Text("Back")
                }
            }
        }

        @Suppress("unused")
        private val SubScreen2 by navDestination<Unit> {
            val nc = navController()
            val logger = LocalLogger.current
            val vm2 = viewModel(nc) { VM("vm2", logger) }
            val vm3 = viewModel { VM("vm3", logger) }
            Column {
                Text("SubScreen2")
                Button(
                    onClick = { nc.back() },
                    modifier = Modifier.testTag("Sub2Back")
                ) {
                    Text("Back")
                }
            }
        }

        @Suppress("unused")
        private val RootVmScreen by navDestination<Unit> {
            val nc = navController()
            val logger = LocalLogger.current
            val rootSharedVm = viewModel(nc) { VM("root-shared", logger) }
            Text(
                text = "Root VM screen",
                modifier = Modifier.testTag("RootVmScreen")
            )
        }

        private val ParentEntryWithNestedController by navDestination<Unit> {
            val rootNc = navController()
            var showNested by remember { mutableStateOf(true) }
            Column {
                Button(
                    onClick = { rootNc.navigate(SiblingScreen) },
                    modifier = Modifier.testTag("ParentToSibling")
                ) {
                    Text("To sibling")
                }
                Button(
                    onClick = { showNested = !showNested },
                    modifier = Modifier.testTag("ToggleNested")
                ) {
                    Text("Toggle nested")
                }
                if (showNested) {
                    val nestedNc = rememberNavController(
                        key = "lifecycle-nested",
                        startDestination = NestedScreen,
                    )
                    Navigation(
                        navController = nestedNc,
                        destinations = arrayOf(NestedScreen),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        @Suppress("unused")
        private val NestedScreen by navDestination<Unit> {
            val nestedNc = navController()
            val logger = LocalLogger.current
            val nestedSharedVm = viewModel(nestedNc) { VM("nested-shared", logger) }
            Text(
                text = "Nested screen",
                modifier = Modifier.testTag("NestedScreen")
            )
        }

        private val SiblingScreen by navDestination<Unit> {
            val rootNc = navController()
            Button(
                onClick = { rootNc.back() },
                modifier = Modifier.testTag("SiblingBack")
            ) {
                Text("Back")
            }
        }
    }

    @Test
    fun `screen scoped clears on exit and navController scoped clears on navController dispose`() = runComposeUiTest {
        val logger = Logger()

        setContent {
            CompositionLocalProvider(LocalLogger provides logger) {
                val rootNavController = rememberNavController(startDestination = RootScreen)
                Navigation(
                    navController = rootNavController,
                    destinations = arrayOf(RootScreen, SubTree),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        awaitIdle()

        // Step 1: Navigate to SubTree - both VMs created
        onNodeWithTag("RootToSubTree").performClick()
        awaitIdle()
        assertContains(logger.messages, "vm1 -> init")
        assertContains(logger.messages, "vm2 -> init")
        assertEquals(2, logger.messages.size, "Expected: 2 models initialized\nLog: ${logger.messages}")
        logger.clear()

        // Step 2: Go to SubScreen2
        onNodeWithTag("Sub1ToSub2").performClick()
        awaitIdle()
        assertContains(logger.messages, "vm3 -> init")
        assertEquals(1, logger.messages.size, "Expected: 1 model initialized\nLog: ${logger.messages}")
        logger.clear()

        // Step 3: Back to SubScreen1
        onNodeWithTag("Sub2Back").performClick()
        awaitIdle()
        assertContains(logger.messages, "vm3 -> onCleared")
        assertEquals(1, logger.messages.size, "Expected: 1 model cleared\nLog: ${logger.messages}")
        logger.clear()

        // Step 4: Back to RootScreen - both VMs should clear
        onNodeWithTag("Sub1Back").performClick()
        awaitIdle()
        assertContains(logger.messages, "vm2 -> onCleared")
        assertContains(logger.messages, "vm1 -> onCleared")
        assertEquals(2, logger.messages.size, "Expected: 2 models cleared\nLog: ${logger.messages}")

        // Step 5: verify we are at Root
        onNodeWithTag("root").assertExists()
    }

    @Test
    fun `rememberNavController dispose # clears root shared viewModel`() = runComposeUiTest {
        val logger = Logger()

        setContent {
            CompositionLocalProvider(LocalLogger provides logger) {
                var showRoot by remember { mutableStateOf(true) }
                if (showRoot) {
                    Column {
                        Button(
                            onClick = { showRoot = false },
                            modifier = Modifier.testTag("DisposeRoot")
                        ) {
                            Text("Dispose root")
                        }
                        val rootNavController = rememberNavController(startDestination = RootVmScreen)
                        Navigation(
                            navController = rootNavController,
                            destinations = arrayOf(RootVmScreen),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        awaitIdle()
        assertContains(logger.messages, "root-shared -> init")
        logger.clear()

        onNodeWithTag("DisposeRoot").performClick()
        awaitIdle()

        assertContains(logger.messages, "root-shared -> onCleared")
        assertEquals(1, logger.messages.size, "Expected: root shared VM cleared\nLog: ${logger.messages}")
    }

    @Test
    fun `rememberNavController dispose # clears nested shared viewModel`() = runComposeUiTest {
        val logger = Logger()

        setContent {
            CompositionLocalProvider(LocalLogger provides logger) {
                val rootNavController = rememberNavController(startDestination = ParentEntryWithNestedController)
                Navigation(
                    navController = rootNavController,
                    destinations = arrayOf(ParentEntryWithNestedController, SiblingScreen),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        awaitIdle()
        assertContains(logger.messages, "nested-shared -> init")
        logger.clear()

        onNodeWithTag("ToggleNested").performClick()
        awaitIdle()

        assertContains(logger.messages, "nested-shared -> onCleared")
        assertEquals(1, logger.messages.size, "Expected: nested shared VM cleared\nLog: ${logger.messages}")
    }

    @Test
    fun `rememberNavController restore # keeps nested shared viewModel while parent entry is detached`() =
        runComposeUiTest {
            val logger = Logger()

            setContent {
                CompositionLocalProvider(LocalLogger provides logger) {
                    val rootNavController = rememberNavController(startDestination = ParentEntryWithNestedController)
                    Navigation(
                        navController = rootNavController,
                        destinations = arrayOf(ParentEntryWithNestedController, SiblingScreen),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            awaitIdle()
            assertContains(logger.messages, "nested-shared -> init")
            assertEquals(
                1,
                logger.messages.size,
                "Expected: nested shared VM initialized once\nLog: ${logger.messages}"
            )
            logger.clear()

            onNodeWithTag("ParentToSibling").performClick()
            awaitIdle()
            assertEquals(
                0,
                logger.messages.size,
                "Expected: nested shared VM retained while parent is detached\nLog: ${logger.messages}"
            )

            onNodeWithTag("SiblingBack").performClick()
            awaitIdle()
            onNodeWithTag("NestedScreen").assertExists()
            assertEquals(
                0,
                logger.messages.size,
                "Expected: no nested shared VM re-init or clear after restore\nLog: ${logger.messages}"
            )
        }
}