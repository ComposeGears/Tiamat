package com.composegears.tiamat.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import kotlinx.serialization.Serializable
import kotlin.test.*
import androidx.savedstate.SavedState as SavedStateX

class NavEntryTests {

    companion object {
        val TestDestination by navDestination<String> { }
        val AnotherTestDestination by navDestination<Unit> { }
        val SerializedNavArgsDestination by navDestination<TestData> { }
        val AnotherSerializedNavArgsDestination by navDestination<Int> { }

        @Serializable
        data class TestData(val data: String) : NavData
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
        val entry = NavEntry(destination = NavDestination.Unresolved("test"))
        assertFalse(entry.isResolved)
    }

    @Test
    fun `isResolved # returns true for resolved destination`() {
        val entry = NavEntry(destination = TestDestination)
        assertTrue(entry.isResolved)
    }

    @Test
    fun `navArgs # nav args provides value when set`() {
        val navArgs = "new-args"
        val entry = NavEntry(destination = TestDestination, navArgs = navArgs)
        assertEquals(navArgs, entry.getNavArgs())
    }

    @Test
    fun `navArgs # cleared`() {
        val navArgs = "new-args"
        val entry = NavEntry(destination = TestDestination, navArgs = navArgs)
        entry.clearNavArgs()
        assertEquals(null, entry.getNavArgs())
    }

    @Test
    fun `freeArgs # provides value with appropriate type when set`() {
        val freeArgs = "free-args"
        val entry = NavEntry(destination = TestDestination, freeArgs = freeArgs)
        assertEquals(freeArgs, entry.getFreeArgs<String>())
    }

    @Test
    fun `freeArgs # provides null with incorrect type when set`() {
        val freeArgs = "free-args"
        val entry = NavEntry(destination = TestDestination, freeArgs = freeArgs)
        assertEquals(null, entry.getFreeArgs<Int>())
    }

    @Test
    fun `freeArgs # restores from serializable with correct type`() {
        val freeArgs = TestData("test")
        val entry = NavEntry(destination = TestDestination, freeArgs = freeArgs)
        val saved = entry.saveToSavedState()
        val restored = NavEntry.restoreFromSavedState(null, saved)
        assertEquals(null, restored.getFreeArgs<Any>())
        assertEquals(freeArgs, restored.getFreeArgs<TestData>())
    }

    @Test
    fun `freeArgs # cleared`() {
        val navResult = "free-args"
        val entry = NavEntry(destination = TestDestination, navResult = navResult)
        entry.clearFreeArgs()
        assertEquals(null, entry.getFreeArgs<Any>())
    }

    @Test
    fun `navResult # provides value with appropriate type when set`() {
        val navResult = "nav-result"
        val entry = NavEntry(destination = TestDestination, navResult = navResult)
        assertEquals(navResult, entry.getNavResult<String>())
    }

    @Test
    fun `navResult # provides null with incorrect type when set`() {
        val navResult = "nav-result"
        val entry = NavEntry(destination = TestDestination, navResult = navResult)
        assertEquals(null, entry.getNavResult<Int>())
    }

    @Test
    fun `navResult # restores from serializable with correct type`() {
        val navResult = TestData("test")
        val entry = NavEntry(destination = TestDestination, navResult = navResult)
        val saved = entry.saveToSavedState()
        val restored = NavEntry.restoreFromSavedState(null, saved)
        assertEquals(null, restored.getNavResult<Any>())
        assertEquals(navResult, restored.getNavResult<TestData>())
    }

    @Test
    fun `navResult # cleared`() {
        val navResult = "nav-result"
        val entry = NavEntry(destination = TestDestination, navResult = navResult)
        entry.clearNavResult()
        assertEquals(null, entry.getNavResult<Any>())
    }

    @Test
    fun `resolveDestination # finds matching destination by name`() {
        val destinationName = TestDestination.name
        val entry = NavEntry(destination = NavDestination.Unresolved(destinationName))
        val destinations = arrayOf<NavDestination<*>>(
            AnotherTestDestination,
            TestDestination
        )
        entry.resolveDestination(destinations)
        assertTrue(entry.isResolved)
        assertEquals(destinationName, entry.destination.name)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `resolveDestination # deserialized nav args`() {
        val entry = SerializedNavArgsDestination.toNavEntry(navArgs = TestData("test"))
        val saved = entry.saveToSavedState()
        val restored = NavEntry.restoreFromSavedState(null, saved)
        restored.resolveDestination(arrayOf(SerializedNavArgsDestination))
        assertTrue(restored.isResolved)
        assertEquals(SerializedNavArgsDestination.name, restored.destination.name)
        assertEquals("test", (restored as? NavEntry<TestData>)?.getNavArgs()?.data)
    }

    @Test
    fun `resolveDestination # throws error when destination not found`() {
        val entry = NavEntry(destination = NavDestination.Unresolved("non_existent"))
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
    fun `saveToSavedState # serialize NavData fields`() {
        val entry = SerializedNavArgsDestination.toNavEntry(
            navArgs = TestData("args"),
            freeArgs = TestData("free"),
            navResult = TestData("result")
        )
        val savedState = entry.saveToSavedState()
        assertTrue(savedState["navArgs"] is SavedStateX)
        assertTrue(savedState["freeArgs"] is SavedStateX)
        assertTrue(savedState["navResult"] is SavedStateX)
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
    fun `restoreFromSavedState # restore serializable fields`() {
        val entry = SerializedNavArgsDestination.toNavEntry(
            navArgs = TestData("args"),
            freeArgs = TestData("free"),
            navResult = TestData("result")
        )
        val savedState = entry.saveToSavedState()
        val result = NavEntry.restoreFromSavedState(null, savedState)
        result.resolveDestination(arrayOf(SerializedNavArgsDestination))
        assertEquals("args", result.getNavArgs().let { it as? TestData }?.data)
        assertEquals("free", result.getFreeArgs<TestData>()?.data)
        assertEquals("result", result.getNavResult<TestData>()?.data)
    }

    @Test
    fun `restoreFromSavedState # fails to restore serializable fields with wrong destination`() {
        val entry = SerializedNavArgsDestination.toNavEntry(
            navArgs = TestData("args"),
            freeArgs = TestData("free"),
            navResult = TestData("result")
        )
        val savedState = entry.saveToSavedState()
        val result = NavEntry.restoreFromSavedState(null, savedState)
        assertFails {
            result.resolveDestination { _ -> AnotherSerializedNavArgsDestination }
        }
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