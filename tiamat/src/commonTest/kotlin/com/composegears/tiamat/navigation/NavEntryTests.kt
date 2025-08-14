package com.composegears.tiamat.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import com.composegears.tiamat.compose.navDestination
import kotlin.test.*

class NavEntryTests {

    companion object {
        val TestDestination by navDestination<String> { }
        val AnotherTestDestination by navDestination<Unit> { }
    }

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
        assertEquals(navArgs, entry.getNavArgs())
        assertEquals(freeArgs, entry.getFreeArgs())
        assertEquals(navResult, entry.getNavResult())
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
        val destinationName = "TestDestination"
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
            it.navControllerStore.add(childNC)
            it.savedState = entrySavedState
        }
        val savedState = savedEntry.saveToSavedState()
        val entry = NavEntry.restoreFromSavedState(parentNC, savedState)
        assertEquals(destinationName, entry.destination.name)
        assertEquals(savedEntry.uuid, entry.uuid)
        assertEquals(navArgs, entry.getNavArgs())
        assertEquals(freeArgs, entry.getFreeArgs())
        assertEquals(navResult, entry.getNavResult())
        assertEquals(entrySavedState, entry.savedState)
        assertEquals(1, entry.navControllerStore.navControllers.size)
        assertEquals(parentNC, entry.navControllerStore.navControllers[0].parent)
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
        assertEquals(0, entry.navControllerStore.navControllers.size)
        assertEquals(0, entry.viewModelStore.keys().size)
    }

    @Test
    fun `navArgs # nav args updates when set`() {
        val newNavArgs = "new-args"
        val entry = NavEntry(destination = TestDestination, navArgs = newNavArgs)
        assertEquals(newNavArgs, entry.getNavArgs())
    }

    @Test
    fun `freeArgs # free args updates when set`() {
        val newFreeArgs = "new-free-args"
        val entry = NavEntry(destination = TestDestination, freeArgs = newFreeArgs)
        assertEquals(newFreeArgs, entry.getFreeArgs())
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
    fun `lifecycle # provides appropriate state base on conditions`() {
        val entry = NavEntry(TestDestination)
        assertEquals(Lifecycle.State.INITIALIZED, entry.lifecycle.currentState)
        entry.attachToNavController()
        entry.attachToUI()
        assertEquals(Lifecycle.State.RESUMED, entry.lifecycle.currentState)
        entry.detachFromUI()
        assertEquals(Lifecycle.State.STARTED, entry.lifecycle.currentState)
        entry.detachFromNavController()
        assertEquals(Lifecycle.State.CREATED, entry.lifecycle.currentState)
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
        entry.navControllerStore.add(NavController.create("tmp", true))
        entry.viewModelStore.put("tmpVM", object : ViewModel() {})
        entry.detachFromUI()
        entry.detachFromNavController()
        assertEquals(0, entry.viewModelStore.keys().size)
        assertEquals(0, entry.navControllerStore.navControllers.size)
        entry.attachToUI()
        entry.attachToNavController()
        entry.navControllerStore.add(NavController.create("tmp", true))
        entry.viewModelStore.put("tmpVM", object : ViewModel() {})
        entry.detachFromNavController()
        entry.detachFromUI()
        assertEquals(0, entry.viewModelStore.keys().size)
        assertEquals(0, entry.navControllerStore.navControllers.size)
    }

    @Test
    fun `attachToUI + detachFromUI # not closes when any is true`() {
        val entry = NavEntry(destination = TestDestination)
        entry.attachToUI()
        entry.attachToNavController()
        entry.navControllerStore.add(NavController.create("tmp", true))
        entry.viewModelStore.put("tmpVM", object : ViewModel() {})
        entry.detachFromUI()
        assertEquals(1, entry.viewModelStore.keys().size)
        assertEquals(1, entry.navControllerStore.navControllers.size)
        entry.attachToUI()
        entry.detachFromNavController()
        assertEquals(1, entry.viewModelStore.keys().size)
        assertEquals(1, entry.navControllerStore.navControllers.size)
    }

    @Test
    fun `close # clears viewModelStore and navControllersStorage`() {
        val entry = NavEntry(destination = TestDestination)
        val navController = NavController.create("test", true, startEntry = entry)
        entry.navControllerStore.add(NavController.create("tmp", true))
        entry.viewModelStore.put("tmpVM", object : ViewModel() {})
        assertEquals(1, entry.viewModelStore.keys().size)
        assertEquals(1, entry.navControllerStore.navControllers.size)
        navController.close()
        assertEquals(0, entry.viewModelStore.keys().size)
        assertEquals(0, entry.navControllerStore.navControllers.size)
    }

    @Test
    fun `contentKey # returns correct key`() {
        val entry = NavEntry(destination = TestDestination)
        assertEquals("TestDestination-${entry.uuid}", entry.contentKey())
    }

    fun NavEntry<*>.resolveDestination(destinations: Array<NavDestination<*>>) {
        resolveDestination { name -> destinations.firstOrNull { it.name == name } }
    }
}