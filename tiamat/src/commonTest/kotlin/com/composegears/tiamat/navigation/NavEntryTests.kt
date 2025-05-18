package com.composegears.tiamat.navigation

import kotlin.test.*

class NavEntryTests {

    @Test
    fun `init # initializes with provided values`() {
        val destination = TestDestination
        val navArgs = "test-args"
        val freeArgs = "free-args"
        val navResult = "result"

        val entry = NavEntry(
            destination = destination,
            navArgs = navArgs,
            freeArgs = freeArgs,
            navResult = navResult
        )

        assertEquals(destination, entry.destination)
        assertEquals(navArgs, entry.navArgs)
        assertEquals(freeArgs, entry.freeArgs)
        assertEquals(navResult, entry.navResult)
        assertNotNull(entry.navControllersStorage)
        assertNotNull(entry.viewModelsStorage)
    }

    @Test
    fun `isResolved # returns false for unresolved destination`() {
        val entry = NavEntry(destination = UnresolvedDestination("test"))
        assertFalse(entry.isResolved())
    }

    @Test
    fun `isResolved # returns true for resolved destination`() {
        val entry = NavEntry(destination = TestDestination)
        assertTrue(entry.isResolved())
    }

    @Test
    fun `resolveDestination # finds matching destination by name`() {
        val destinationName = TestDestination.name
        val entry = NavEntry(destination = UnresolvedDestination(destinationName))
        val destinations = arrayOf<NavDestination<*>>(
            AnotherTestDestination,
            TestDestination
        )
        entry.resolveDestination(destinations)
        assertTrue(entry.isResolved())
        assertEquals(destinationName, entry.destination.name)
    }

    @Test
    fun `resolveDestination # throws error when destination not found`() {
        val entry = NavEntry(destination = UnresolvedDestination("non_existent"))
        val destinations = arrayOf<NavDestination<*>>(
            TestDestination,
            AnotherTestDestination
        )
        assertFails { entry.resolveDestination(destinations) }
    }

    @Test
    fun `saveToSavedState # saves all entry properties`() {
        val destination = TestDestination
        val navArgs = "test-args"
        val freeArgs = "free-args"
        val navResult = "result"
        val entry = NavEntry(
            destination = destination,
            navArgs = navArgs,
            freeArgs = freeArgs,
            navResult = navResult
        )
        val savedState = entry.saveToSavedState()
        assertEquals(destination.name, savedState["destination"])
        assertEquals(navArgs, savedState["navArgs"])
        assertEquals(freeArgs, savedState["freeArgs"])
        assertEquals(navResult, savedState["navResult"])
        assertNotNull(savedState["navControllers"])
    }

    @Test
    fun `saveToSavedState # uses savedStateSaver when available`() {
        val destination = TestDestination
        val customSavedState = SavedState("custom" to "value")
        val entry = NavEntry(destination = destination)
        entry.setSavedStateSaver { customSavedState }
        val savedState = entry.saveToSavedState()
        assertEquals(customSavedState, savedState["savedState"])
    }

    @Test
    fun `saveToSavedState # uses savedState when savedStateSaver is null`() {
        val destination = TestDestination
        val customSavedState = SavedState("custom" to "value")
        val entry = NavEntry(destination = destination)
        entry.savedState = customSavedState
        val savedState = entry.saveToSavedState()
        assertEquals(customSavedState, savedState["savedState"])
    }

    @Test
    fun `restoreFromSavedState # creates entry with saved values`() {
        val parentNC = NavController.create("parent", saveable = true)
        val childNC = NavController.create("child", saveable = true)
        val destinationName = "test_destination"
        val navArgs = "test-args"
        val freeArgs = "free-args"
        val navResult = "result"
        val entrySavedState = SavedState("test" to "value")

        val savedState = NavEntry(
            destination = TestDestination,
            navArgs = navArgs,
            freeArgs = freeArgs,
            navResult = navResult
        ).also {
            it.navControllersStorage.add(childNC)
            it.savedState = entrySavedState
        }.saveToSavedState()
        val entry = NavEntry.restoreFromSavedState(parentNC, savedState)
        assertEquals(destinationName, entry.destination.name)
        assertEquals(navArgs, entry.navArgs)
        assertEquals(freeArgs, entry.freeArgs)
        assertEquals(navResult, entry.navResult)
        assertEquals(entrySavedState, entry.savedState)
        assertEquals(1, entry.navControllersStorage.nestedNavControllers.size)
        assertEquals(parentNC, entry.navControllersStorage.nestedNavControllers[0].parent)
        assertFalse(entry.isResolved())
    }

    @Test
    fun `restoreFromSavedState # throws error when destination is null`() {
        val savedState = SavedState()
        assertFails { NavEntry.restoreFromSavedState(null, savedState) }
    }

    @Test
    fun `setSavedStateSaver # uses custom saver if provided`() {
        val destination = TestDestination
        val customSavedState = SavedState()
        val entry = NavEntry(destination = destination)
        entry.savedState = customSavedState
        val savedState = entry.saveToSavedState()
        assertEquals(customSavedState, savedState["savedState"])
    }

    @Test
    fun `attachToNavController # sets isAttachedToNavController to true`() {
        val entry = NavEntry(destination = TestDestination)
        entry.attachToNavController()
        assertTrue(entry.isAttachedToNavController)
    }

    @Test
    fun `detachFromNavController # sets isAttachedToNavController to false`() {
        val entry = NavEntry(destination = TestDestination)
        entry.attachToNavController()
        entry.detachFromNavController()
        assertFalse(entry.isAttachedToNavController)
    }

    @Test
    fun `ensureDetachedAndAttach # attaches if not already attached`() {
        val entry = NavEntry(destination = TestDestination)
        entry.ensureDetachedAndAttach()
        assertTrue(entry.isAttachedToNavController)
    }

    @Test
    fun `ensureDetachedAndAttach # throws error if already attached`() {
        val entry = NavEntry(destination = TestDestination)
        entry.attachToNavController()
        assertFails { entry.ensureDetachedAndAttach() }
    }

    @Test
    fun `attachToUI # sets isAttachedToUI to true`() {
        val entry = NavEntry(destination = TestDestination)
        entry.attachToUI()
        assertTrue(entry.isAttachedToUI)
    }

    @Test
    fun `detachFromUI # sets isAttachedToUI to false`() {
        val entry = NavEntry(destination = TestDestination)
        entry.attachToUI()
        entry.detachFromUI()
        assertFalse(entry.isAttachedToUI)
    }

    @Test
    fun `close # clears viewModelsStorage and navControllersStorage`() {
        val entry = NavEntry(destination = TestDestination)
        val navController = NavController.create("test", true, startEntry = entry)
        entry.navControllersStorage.add(NavController.create("tmp", true))
        entry.viewModelsStorage.get("tmpVM") { object : TiamatViewModel() {} }
        assertEquals(1, entry.viewModelsStorage.viewModels.size)
        assertEquals(1, entry.navControllersStorage.nestedNavControllers.size)
        navController.close()
        assertEquals(0, entry.viewModelsStorage.viewModels.size)
        assertEquals(0, entry.navControllersStorage.nestedNavControllers.size)
    }

    // Test helper objects
    private object TestDestination : NavDestination<String> {
        override val name: String = "test_destination"
    }

    private object AnotherTestDestination : NavDestination<Int> {
        override val name: String = "another_test_destination"
    }
}