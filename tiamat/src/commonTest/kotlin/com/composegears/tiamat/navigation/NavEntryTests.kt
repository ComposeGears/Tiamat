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
        assertFalse(entry.isResolved)
    }

    @Test
    fun `isResolved # returns true for resolved destination`() {
        val entry = NavEntry(destination = TestDestination)
        assertTrue(entry.isResolved)
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
        assertTrue(entry.isResolved)
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
        val savedState = SavedState()
        val entry = NavEntry(
            destination = destination,
            navArgs = navArgs,
            freeArgs = freeArgs,
            navResult = navResult
        )
        entry.savedState = savedState
        val state = entry.saveToSavedState()
        assertEquals(destination.name, state["destination"])
        assertEquals(navArgs, state["navArgs"])
        assertEquals(freeArgs, state["freeArgs"])
        assertEquals(navResult, state["navResult"])
        assertEquals(savedState, state["savedState"])
        assertNotNull(state["navControllers"])
    }

    @Test
    fun `saveToSavedState # if savedStateSaver is not null - updates savedState`() {
        val destination = TestDestination
        val customSavedState = SavedState("custom" to "value")
        val entry = NavEntry(destination = destination)
        entry.setSavedStateSaver { customSavedState }
        val savedState = entry.saveToSavedState()
        assertEquals(entry.savedState, customSavedState)
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
        val savedEntry = NavEntry(
            destination = TestDestination,
            navArgs = navArgs,
            freeArgs = freeArgs,
            navResult = navResult
        ).also {
            it.navControllersStorage.add(childNC)
            it.savedState = entrySavedState
        }
        val savedState = savedEntry.saveToSavedState()
        val entry = NavEntry.restoreFromSavedState(parentNC, savedState)
        assertEquals(destinationName, entry.destination.name)
        assertEquals(savedEntry.uuid, entry.uuid)
        assertEquals(navArgs, entry.navArgs)
        assertEquals(freeArgs, entry.freeArgs)
        assertEquals(navResult, entry.navResult)
        assertEquals(entrySavedState, entry.savedState)
        assertEquals(1, entry.navControllersStorage.nestedNavControllers.size)
        assertEquals(parentNC, entry.navControllersStorage.nestedNavControllers[0].parent)
        assertFalse(entry.isResolved)
    }

    @Test
    fun `restoreFromSavedState # throws error when destination is null`() {
        val savedState = SavedState(
            "uuid" to "some-uuid",
        )
        assertFails { NavEntry.restoreFromSavedState(null, savedState) }
    }

    @Test
    fun `restoreFromSavedState # throws error when uuid is null`() {
        val savedState = SavedState(
            "destination" to "some-destination",
        )
        assertFails { NavEntry.restoreFromSavedState(null, savedState) }
    }

    @Test
    fun `restoreFromSavedState # restores from minimal saved state`() {
        val savedState = SavedState(
            "uuid" to "some-uuid",
            "destination" to "some-destination",
        )
        val entry = NavEntry.restoreFromSavedState(null, savedState)
        assertEquals(0, entry.navControllersStorage.nestedNavControllers.size)
        assertEquals(0, entry.viewModelsStorage.viewModels.size)
    }

    @Test
    fun `navArgs # nav args updates when set`() {
        val entry = NavEntry(destination = TestDestination)
        val newNavArgs = "new-args"
        entry.navArgs = newNavArgs
        assertEquals(newNavArgs, entry.navArgs)
    }

    @Test
    fun `freeArgs # free args updates when set`() {
        val entry = NavEntry(destination = TestDestination)
        val newFreeArgs = "new-free-args"
        entry.freeArgs = newFreeArgs
        assertEquals(newFreeArgs, entry.freeArgs)
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
    fun `attachToUI + detachFromUI # closes when both are false`() {
        val entry = NavEntry(destination = TestDestination)
        entry.attachToUI()
        entry.attachToNavController()
        entry.navControllersStorage.add(NavController.create("tmp", true))
        entry.viewModelsStorage.get("tmpVM") { object : TiamatViewModel() {} }
        entry.detachFromUI()
        entry.detachFromNavController()
        assertEquals(0, entry.viewModelsStorage.viewModels.size)
        assertEquals(0, entry.navControllersStorage.nestedNavControllers.size)
        entry.attachToUI()
        entry.attachToNavController()
        entry.navControllersStorage.add(NavController.create("tmp", true))
        entry.viewModelsStorage.get("tmpVM") { object : TiamatViewModel() {} }
        entry.detachFromNavController()
        entry.detachFromUI()
        assertEquals(0, entry.viewModelsStorage.viewModels.size)
        assertEquals(0, entry.navControllersStorage.nestedNavControllers.size)
    }

    @Test
    fun `attachToUI + detachFromUI # not closes when any is true`() {
        val entry = NavEntry(destination = TestDestination)
        entry.attachToUI()
        entry.attachToNavController()
        entry.navControllersStorage.add(NavController.create("tmp", true))
        entry.viewModelsStorage.get("tmpVM") { object : TiamatViewModel() {} }
        entry.detachFromUI()
        assertEquals(1, entry.viewModelsStorage.viewModels.size)
        assertEquals(1, entry.navControllersStorage.nestedNavControllers.size)
        entry.attachToUI()
        entry.detachFromNavController()
        assertEquals(1, entry.viewModelsStorage.viewModels.size)
        assertEquals(1, entry.navControllersStorage.nestedNavControllers.size)
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

    @Test
    fun `contentKey # returns correct key`() {
        val entry = NavEntry(destination = TestDestination)
        assertEquals("test_destination-${entry.uuid}", entry.contentKey())
    }

    // Test helper objects
    private object TestDestination : NavDestination<String> {
        override val name: String = "test_destination"
    }

    private object AnotherTestDestination : NavDestination<Int> {
        override val name: String = "another_test_destination"
    }
}