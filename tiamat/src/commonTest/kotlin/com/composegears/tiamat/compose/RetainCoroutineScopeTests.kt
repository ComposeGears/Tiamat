@file:OptIn(ExperimentalTestApi::class, TiamatExperimentalApi::class, ExperimentalCoroutinesApi::class)

package com.composegears.tiamat.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.*

class RetainCoroutineScopeTests {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    companion object {

        private class ScopeRecorder {
            val scopes = mutableListOf<CoroutineScope>()
        }

        private val LocalScopeRecorder = staticCompositionLocalOf<ScopeRecorder> {
            error("ScopeRecorder not initialized")
        }

        private val RootScreen by navDestination<Unit> {
            Text("Root", modifier = Modifier.testTag("Root"))
        }

        private val OtherScreen by navDestination<Unit> {
            Text("Other", modifier = Modifier.testTag("Other"))
        }

        private val ScopeScreen by navDestination<Unit> {
            LocalScopeRecorder.current.scopes += retainCoroutineScope()
            Text("Scope", modifier = Modifier.testTag("Scope"))
        }

        private val AllDestinations = arrayOf<NavDestination<*>>(RootScreen, OtherScreen, ScopeScreen)
    }

    private fun ComposeUiTest.setNavContent(recorder: ScopeRecorder): NavController {
        lateinit var navController: NavController
        setContent {
            CompositionLocalProvider(LocalScopeRecorder provides recorder) {
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

    @Test
    fun `retainCoroutineScope - keeps scope while entry is in backstack and cancels on pop`() = runComposeUiTest {
        val recorder = ScopeRecorder()
        val nc = setNavContent(recorder)

        act { nc.navigate(ScopeScreen) }
        val scope = assertNotNull(recorder.scopes.lastOrNull(), "Expected: scope created")
        assertTrue(scope.isActive, "Expected: scope active")
        val job = scope.launch { awaitCancellation() }

        // forward - entry stays in backstack, scope & job are alive
        act { nc.navigate(OtherScreen) }
        assertTrue(scope.isActive, "Expected: scope active while entry is in backstack")
        assertTrue(job.isActive, "Expected: job active while entry is in backstack")

        // back - the same scope instance is reused
        act { nc.back() }
        onNodeWithTag("Scope").assertExists()
        assertTrue(recorder.scopes.all { it === scope }, "Expected: same scope instance after return")
        assertTrue(job.isActive, "Expected: job active after return")

        // pop - scope & job are canceled
        act { nc.back() }
        assertFalse(scope.isActive, "Expected: scope canceled after entry is popped")
        assertTrue(job.isCancelled, "Expected: job canceled after entry is popped")
    }

    @Test
    fun `retainCoroutineScope - new entry gets a new scope`() = runComposeUiTest {
        val recorder = ScopeRecorder()
        val nc = setNavContent(recorder)

        act { nc.navigate(ScopeScreen) }
        val firstScope = recorder.scopes.last()
        act { nc.back() }
        act { nc.navigate(ScopeScreen) }
        val secondScope = recorder.scopes.last()

        assertNotSame(firstScope, secondScope, "Expected: new scope for a new entry")
        assertFalse(firstScope.isActive, "Expected: first scope canceled")
        assertTrue(secondScope.isActive, "Expected: second scope active")
    }

    @Test
    fun `retainCoroutineScope - child failure does not cancel scope`() = runComposeUiTest {
        val recorder = ScopeRecorder()
        val nc = setNavContent(recorder)

        act { nc.navigate(ScopeScreen) }
        val scope = recorder.scopes.last()
        val sibling = scope.launch { awaitCancellation() }
        val failing = scope.launch(CoroutineExceptionHandler { _, _ -> }) { error("boom") }
        failing.join()

        assertTrue(failing.isCancelled, "Expected: failing job completed exceptionally")
        assertTrue(scope.isActive, "Expected: scope still active after child failure")
        assertTrue(sibling.isActive, "Expected: sibling job still active after child failure")
        sibling.cancel()
    }

    @Test
    fun `retainCoroutineScope - uses main dispatcher`() = runComposeUiTest {
        val recorder = ScopeRecorder()
        val nc = setNavContent(recorder)

        act { nc.navigate(ScopeScreen) }
        val scope = recorder.scopes.last()
        assertEquals<Any?>(Dispatchers.Main.immediate, scope.coroutineContext[ContinuationInterceptor])
    }
}
