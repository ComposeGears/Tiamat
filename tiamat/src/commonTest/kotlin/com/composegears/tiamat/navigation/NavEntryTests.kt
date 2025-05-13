package com.composegears.tiamat.navigation

import com.composegears.tiamat.navigation.NavDestination.Companion.UnresolvedDestination
import kotlin.test.*

class NavEntryTests {
    private object TestDestination : NavDestination<String> {
        override val name: String = "test_destination"
    }

    private object AnotherTestDestination : NavDestination<String> {
        override val name: String = "another_test_destination"
    }

    private class TestViewModel : TiamatViewModel() {
        var isClosed = false

        override fun onClosed() {
            isClosed = true
        }
    }

    @Test
    fun `NavEntry | saveState | saved to state`() {
        val destination = TestDestination
        val navArgs = "test_args"
        val freeArgs = 42
        val navResult = "test_result"
        val entry = NavEntry(destination, navArgs, freeArgs, navResult)

        val savedState = entry.saveToSavedState()

        assertEquals(destination.name, savedState["destination"])
        assertEquals(navArgs, savedState["navArgs"])
        assertEquals(freeArgs, savedState["freeArgs"])
        assertEquals(navResult, savedState["navResult"])
        assertNotNull(savedState)
    }

    @Test
    fun `NavEntry | saveState | restored from state`() {
        val destination = TestDestination
        val navArgs = "test_args"
        val freeArgs = 42
        val navResult = "test_result"
        val originalEntry = NavEntry(destination, navArgs, freeArgs, navResult)
        val savedState = originalEntry.saveToSavedState()

        val restoredEntry = NavEntry.restoreFromSavedState(savedState)

        assertEquals(destination.name, restoredEntry.destination.name)
        assertEquals(navArgs, restoredEntry.navArgs)
        assertEquals(freeArgs, restoredEntry.freeArgs)
        assertEquals(navResult, restoredEntry.navResult)
    }

    @Test
    fun `NavEntry | destination | destination is unresolved if loaded from saved state`() {
        val destination = TestDestination
        val originalEntry = NavEntry(destination)
        val savedState = originalEntry.saveToSavedState()

        val restoredEntry = NavEntry.restoreFromSavedState(savedState)

        assertTrue(restoredEntry.destination is UnresolvedDestination)
        assertFalse(restoredEntry.isResolved())
        assertEquals(destination.name, restoredEntry.destination.name)
    }

    @Test
    fun `NavEntry | destination | destination is resolved if created from real destination`() {
        val destination = TestDestination
        val entry = NavEntry(destination)
        assertTrue(entry.isResolved())
        assertEquals(destination, entry.destination)
    }

    @Test
    fun `NavEntry | destination | destination is resolved after calling "resolveDestination"`() {
        val destination = TestDestination
        val originalEntry = NavEntry(destination)
        val savedState = originalEntry.saveToSavedState()
        val restoredEntry = NavEntry.restoreFromSavedState(savedState)
        assertFalse(restoredEntry.isResolved())

        restoredEntry.resolveDestination(arrayOf(destination))

        assertTrue(restoredEntry.isResolved())
        assertEquals(destination.name, restoredEntry.destination.name)
        assertEquals("test_destination", restoredEntry.destination.name)
    }

    @Test
    fun `NavEntry | destination | error is thrown if destination is not found when calling "resolveDestination"`() {
        val destination = TestDestination
        val originalEntry = NavEntry(destination)
        val savedState = originalEntry.saveToSavedState()
        val restoredEntry = NavEntry.restoreFromSavedState(savedState)
        assertFalse(restoredEntry.isResolved())

        assertFailsWith<Exception> {
            restoredEntry.resolveDestination(arrayOf(AnotherTestDestination))
        }
    }

    @Test
    fun `NavEntry | close | clears viewModelsStorage`() {
        val entry = NavEntry(TestDestination)
        val viewModel1 = entry.viewModelsStorage["vm1", { TestViewModel() }]
        val viewModel2 = entry.viewModelsStorage["vm2", { TestViewModel() }]

        assertEquals(2, entry.viewModelsStorage.viewModels.size)
        entry.close()

        assertTrue(viewModel1.isClosed, "ViewModel1 should be closed")
        assertTrue(viewModel2.isClosed, "ViewModel2 should be closed")
        assertEquals(0, entry.viewModelsStorage.viewModels.size, "ViewModels storage should be empty after close")
    }

    @Test
    fun `NavEntry | close | works with empty viewModelsStorage`() {
        val entry = NavEntry(TestDestination)
        entry.viewModelsStorage["vm1", { TestViewModel() }]
        assertEquals(1, entry.viewModelsStorage.viewModels.size)
        entry.close()
        assertEquals(0, entry.viewModelsStorage.viewModels.size)
    }
}