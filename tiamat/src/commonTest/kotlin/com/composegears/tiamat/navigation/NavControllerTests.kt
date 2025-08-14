package com.composegears.tiamat.navigation

import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.createTestNavController
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import kotlin.test.*

class NavControllerTests {

    companion object {
        val Destination1 by navDestination {}
        val Destination2 by navDestination {}
        val Destination3 by navDestination {}
        val Destination4 by navDestination {}
    }

    @Test
    fun `create # initialized empty with default values`() {
        val nc = createTestNavController()
        assertNull(nc.getCurrentNavEntry())
        assertTrue(nc.getBackStack().isEmpty())
        val nc2 = NavController.create(startDestination = Destination1)
        assertNull(nc2.key)
        assertTrue(nc2.saveable)
        val nc3 = NavController.create()
        assertNull(nc3.key)
        assertTrue(nc3.saveable)
        assertNull(nc3.getCurrentNavEntry())
    }

    @Test
    fun `create # navigates to startDestination when provided`() {
        val nc = createTestNavController(startDestination = Destination1)
        val currentEntry = nc.getCurrentNavEntry()
        assertNotNull(currentEntry)
        assertEquals(Destination1, currentEntry.destination)
    }

    @Test
    fun `restoreFromSavedState # restores navController state`() {
        val originalNc = createTestNavController(
            key = "testKey",
            saveable = true,
            startDestination = Destination1
        )
        originalNc.navigate(Destination2.toNavEntry())
        originalNc.navigate(Destination3.toNavEntry())
        val savedState = originalNc.saveToSavedState()
        val restoredNc = NavController.restoreFromSavedState(savedState = savedState)
        assertEquals("testKey", restoredNc.key)
        assertTrue(restoredNc.saveable)
        assertEquals("Destination3", restoredNc.getCurrentNavEntry()?.destination?.name)
        val backStack = restoredNc.getBackStack()
        assertEquals(2, backStack.size)
        assertEquals("Destination1", backStack[0].destination.name)
        assertEquals("Destination2", backStack[1].destination.name)
    }

    @Test
    fun `restoreFromSavedState # restores backstack from empty states`() {
        val restoredNc1 = NavController.restoreFromSavedState(
            savedState = SavedState(
                "saveable" to true,
            )
        )
        val restoredNc2 = NavController.restoreFromSavedState(
            savedState = SavedState(
                "saveable" to true,
                "backStack" to null
            )
        )
        val restoredNc3 = NavController.restoreFromSavedState(
            savedState = SavedState(
                "saveable" to true,
                "backStack" to listOf<SavedState>()
            )
        )
        assertEquals(0, restoredNc1.getBackStack().size)
        assertEquals(0, restoredNc2.getBackStack().size)
        assertEquals(0, restoredNc3.getBackStack().size)
    }

    @Test
    fun `saveToSavedState # saves navController state`() {
        val nc = createTestNavController(
            key = "testKey",
            saveable = true,
            startDestination = Destination1
        )
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        val savedState = nc.saveToSavedState()
        assertEquals("testKey", savedState["key"])
        assertTrue(savedState["saveable"] as Boolean)
        @Suppress("UNCHECKED_CAST")
        val currentSavedState = savedState["current"] as SavedState
        assertEquals("Destination3", currentSavedState["destination"])
        val backStackList = savedState["backStack"] as List<*>
        assertEquals(2, backStackList.size)
    }

    @Test
    fun `setOnNavigationListener # calls listener on navigation events`() {
        val nc = createTestNavController()
        var fromDestination: NavDestination<*>? = null
        var toDestination: NavDestination<*>? = null
        var isForward = false
        nc.setOnNavigationListener { from, to, forward ->
            fromDestination = from?.destination
            toDestination = to?.destination
            isForward = forward
        }
        nc.navigate(Destination1.toNavEntry())
        assertEquals(null, fromDestination)
        assertEquals(Destination1, toDestination)
        assertTrue(isForward)
        nc.navigate(Destination2.toNavEntry())
        assertEquals(Destination1, fromDestination)
        assertEquals(Destination2, toDestination)
        assertTrue(isForward)
        nc.back()
        assertEquals(Destination2, fromDestination)
        assertEquals(Destination1, toDestination)
        assertFalse(isForward)
    }

    @Test
    fun `findParentNavController # finds controller with matching key`() {
        val rootNc = createTestNavController(key = "root")
        val childNc = createTestNavController(key = "child", parent = rootNc)
        val grandchildNc = createTestNavController(key = "grandchild", parent = childNc)
        assertEquals(rootNc, grandchildNc.findParentNavController("root"))
        assertEquals(childNc, grandchildNc.findParentNavController("child"))
        assertEquals(null, grandchildNc.findParentNavController("nonexistent"))
    }

    @Test
    fun `hasBackEntries # returns true when backstack is not empty`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        assertTrue(nc.getBackStack().isNotEmpty())
        assertTrue(nc.hasBackEntries())
    }

    @Test
    fun `hasBackEntries # returns false when backstack is empty`() {
        val nc = createTestNavController(startDestination = Destination1)
        assertTrue(nc.getBackStack().isEmpty())
        assertFalse(nc.hasBackEntries())
    }

    @Test
    fun `editBackStack # add entry adds entry to backstack`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.editBackStack { add(Destination2.toNavEntry()) }
        val backStack = nc.getBackStack()
        assertEquals(1, backStack.size)
        assertEquals(Destination2, backStack[0].destination)
        assertTrue(backStack[0].isAttachedToNavController)
    }

    @Test
    fun `editBackStack # fail to add entry if already attached`() {
        val nc = createTestNavController(startDestination = Destination1)
        val attachedEntry = Destination2.toNavEntry().apply { attachToNavController() }
        assertFails { nc.editBackStack { add(attachedEntry) } }
    }

    @Test
    fun `editBackStack # add destination adds destination to backstack`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.editBackStack { add(Destination2) }
        val backStack = nc.getBackStack()
        assertEquals(1, backStack.size)
        assertEquals(Destination2, backStack[0].destination)
        assertTrue(backStack[0].isAttachedToNavController)
    }

    @Test
    fun `editBackStack # add with index adds entry at specific position`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        nc.navigate(Destination4.toNavEntry())
        nc.editBackStack { add(1, Destination4.toNavEntry()) }
        val backStack = nc.getBackStack()
        assertEquals(4, backStack.size)
        assertEquals(Destination4, backStack[1].destination)
        assertTrue(backStack[1].isAttachedToNavController)
    }

    @Test
    fun `editBackStack # fail to add entry at index if already attached`() {
        val nc = createTestNavController(startDestination = Destination1)
        val attachedEntry = Destination2.toNavEntry().apply { attachToNavController() }
        assertFails { nc.editBackStack { add(0, attachedEntry) } }
    }

    @Test
    fun `editBackStack # add with index and destination adds destination at specific position`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        nc.navigate(Destination4.toNavEntry())
        nc.editBackStack { add(1, Destination4) }
        val backStack = nc.getBackStack()
        assertEquals(4, backStack.size)
        assertEquals(Destination4, backStack[1].destination)
    }

    @Test
    fun `editBackStack # removeAt removes entry at specific index`() {
        val nc = createTestNavController(startDestination = Destination1)
        val entry2 = Destination2.toNavEntry()
        nc.navigate(entry2)
        nc.navigate(Destination3.toNavEntry())
        nc.editBackStack { removeAt(1) }
        val backStack = nc.getBackStack()
        assertEquals(1, backStack.size)
        assertEquals(Destination1, backStack[0].destination)
        assertFalse(entry2.isAttachedToNavController)
    }

    @Test
    fun `editBackStack # removeAt fails index is out of bounds`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        assertFails {
            nc.editBackStack { removeAt(148) }
        }
    }

    @Test
    fun `editBackStack # removeLast removes last entry in backstack till it's not empty`() {
        val nc = createTestNavController(startDestination = Destination1)
        val entry2 = Destination2.toNavEntry()
        nc.navigate(entry2)
        nc.navigate(Destination3.toNavEntry())
        nc.editBackStack {
            assertTrue(removeLast())
        }
        assertEquals(1, nc.getBackStack().size)
        assertEquals(Destination1, nc.getBackStack()[0].destination)
        assertFalse(entry2.isAttachedToNavController)
        nc.editBackStack {
            assertTrue(removeLast())
            assertFalse(removeLast())
        }
        assertEquals(0, nc.getBackStack().size)
    }

    @Test
    fun `editBackStack # removeLast with destination removes last matching destination`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        val entry1 = Destination1.toNavEntry()
        nc.navigate(entry1)
        nc.navigate(Destination3.toNavEntry())
        nc.editBackStack {
            assertTrue(removeLast(Destination1))
        }
        assertEquals(2, nc.getBackStack().size)
        assertEquals(Destination1, nc.getBackStack()[0].destination)
        assertEquals(Destination2, nc.getBackStack()[1].destination)
        assertFalse(entry1.isAttachedToNavController)
        nc.editBackStack {
            assertTrue(removeLast(Destination1))
            assertFalse(removeLast(Destination1))
        }
        assertEquals(1, nc.getBackStack().size)
        assertEquals(Destination2, nc.getBackStack()[0].destination)
    }

    @Test
    fun `editBackStack # removeLast with predicate removes last matching entry`() {
        val nc = createTestNavController(startDestination = Destination1)
        val entry21 = Destination2.toNavEntry()
        val entry22 = Destination2.toNavEntry()
        nc.navigate(entry21)
        nc.navigate(entry22)
        nc.navigate(Destination3.toNavEntry())
        nc.editBackStack {
            assertTrue(removeLast { it.destination == Destination2 })
        }
        val backStack = nc.getBackStack()
        assertEquals(2, backStack.size)
        assertEquals(Destination1, backStack[0].destination)
        assertEquals(Destination2, backStack[1].destination) // First occurrence remains
        assertTrue(entry21.isAttachedToNavController)
        assertFalse(entry22.isAttachedToNavController)
        nc.editBackStack {
            assertTrue(removeLast { it.destination == Destination2 })
            assertFalse(removeLast { it.destination == Destination2 })
        }
    }

    @Test
    fun `editBackStack # removeAll removes all entries matching predicate`() {
        val nc = createTestNavController(startDestination = Destination1)
        val entry21 = Destination2.toNavEntry()
        val entry22 = Destination2.toNavEntry()
        nc.navigate(entry21)
        nc.navigate(entry22)
        nc.navigate(Destination3.toNavEntry())
        nc.editBackStack { removeAll { it.destination == Destination2 } }
        val backStack = nc.getBackStack()
        assertEquals(1, backStack.size)
        assertEquals(Destination1, backStack[0].destination)
        assertFalse(entry21.isAttachedToNavController)
        assertFalse(entry22.isAttachedToNavController)
    }

    @Test
    fun `editBackStack # set with destinations replaces backstack with provided destinations`() {
        val entry1 = Destination1.toNavEntry()
        val entry2 = Destination2.toNavEntry()
        val entry3 = Destination3.toNavEntry()
        val nc = createTestNavController()
        nc.navigate(entry1)
        nc.navigate(entry2)
        nc.navigate(entry3)
        assertTrue(entry1.isAttachedToNavController)
        assertTrue(entry2.isAttachedToNavController)
        assertTrue(entry3.isAttachedToNavController)
        nc.editBackStack { set(Destination3, Destination4) }
        val backStack = nc.getBackStack()
        assertEquals(2, backStack.size)
        assertEquals(Destination3, backStack[0].destination)
        assertEquals(Destination4, backStack[1].destination)
        assertFalse(entry1.isAttachedToNavController)
        assertFalse(entry2.isAttachedToNavController)
        assertTrue(entry3.isAttachedToNavController)
    }

    @Test
    fun `editBackStack # set with entries replaces backstack with provided entries`() {
        val entry1 = Destination1.toNavEntry()
        val entry2 = Destination2.toNavEntry()
        val entry3 = Destination3.toNavEntry()
        val nc = createTestNavController()
        nc.navigate(entry1)
        nc.navigate(entry2)
        nc.navigate(entry3)
        assertTrue(entry1.isAttachedToNavController)
        assertTrue(entry2.isAttachedToNavController)
        assertTrue(entry3.isAttachedToNavController)
        nc.editBackStack { set(Destination3.toNavEntry(), Destination4.toNavEntry()) }
        val backStack = nc.getBackStack()
        assertEquals(2, backStack.size)
        assertEquals(Destination3, backStack[0].destination)
        assertEquals(Destination4, backStack[1].destination)
        assertFalse(entry1.isAttachedToNavController)
        assertFalse(entry2.isAttachedToNavController)
        assertTrue(entry3.isAttachedToNavController)
    }

    @Test
    fun `editBackStack # clear removes all entries from backstack`() {
        val entry1 = Destination1.toNavEntry()
        val entry2 = Destination2.toNavEntry()
        val entry3 = Destination3.toNavEntry()
        val nc = createTestNavController()
        nc.navigate(entry1)
        nc.navigate(entry2)
        nc.navigate(entry3)
        nc.editBackStack { clear() }
        assertTrue(nc.getBackStack().isEmpty())
        assertFalse(entry1.isAttachedToNavController)
        assertFalse(entry2.isAttachedToNavController)
        assertTrue(entry3.isAttachedToNavController)
    }

    @Test
    fun `editBackStack # size returns number of entries in backstack`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        nc.editBackStack { assertEquals(2, size()) }
    }

    @Test
    fun `navigate # updates current entry and adds previous entry to backstack`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
        assertEquals(1, nc.getBackStack().size)
        assertEquals(Destination1, nc.getBackStack()[0].destination)
    }

    @Test
    fun `replace # updates current entry without adding to backstack`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
        assertEquals(1, nc.getBackStack().size)
        nc.replace(Destination3.toNavEntry())
        assertEquals(Destination3, nc.getCurrentNavEntry()?.destination)
        assertEquals(1, nc.getBackStack().size)
        assertEquals(Destination1, nc.getBackStack()[0].destination)
    }

    @Test
    fun `popToTop # navigates to existing entry in backstack`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        nc.popToTop(Destination1)
        assertEquals(Destination1, nc.getCurrentNavEntry()?.destination)
        assertEquals(2, nc.getBackStack().size)
        assertEquals(Destination2, nc.getBackStack()[0].destination)
        assertEquals(Destination3, nc.getBackStack()[1].destination)
    }

    @Test
    fun `popToTop # calls orElse when destination not found`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        var orElseCalled = false
        nc.popToTop(Destination3) {
            orElseCalled = true
        }
        assertTrue(orElseCalled)
    }

    @Test
    fun `popToTop # do not put anything on backstack if current is null`() {
        val nc = createTestNavController()
        nc.editBackStack {
            add(Destination1)
            add(Destination2)
        }
        nc.popToTop(Destination2)
        assertEquals(1, nc.getBackStack().size)
    }

    @Test
    fun `popToTop # navigates to destination if nothing to pop`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.editBackStack {
            add(Destination2)
            add(Destination3)
        }
        nc.popToTop(Destination4)
        assertEquals(Destination4, nc.getCurrentNavEntry()?.destination)
        assertEquals(3, nc.getBackStack().size)
        assertEquals(Destination2, nc.getBackStack()[0].destination)
        assertEquals(Destination3, nc.getBackStack()[1].destination)
        assertEquals(Destination1, nc.getBackStack()[2].destination)
    }

    @Test
    @OptIn(TiamatExperimentalApi::class)
    fun `route # replace current destination`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.route {
            element(Destination2)
        }
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
        assertEquals(0, nc.getBackStack().size)
    }

    @Test
    @OptIn(TiamatExperimentalApi::class)
    fun `route # navigates through multiple destinations`() {
        val nc = createTestNavController()
        nc.route {
            destination("Destination1")
            element(Destination2)
            element(Destination3.toNavEntry())
            navController("controller")
            destination("Destination4")
            destination("Destination1")
        }

        assertEquals(2, nc.getBackStack().size)
        assertEquals("Destination1", nc.getBackStack()[0].destination.name)
        assertEquals("Destination2", nc.getBackStack()[1].destination.name)
        val currentNavEntry = nc.getCurrentNavEntry()
        assertEquals("Destination3", currentNavEntry?.destination?.name)
        assertEquals(1, currentNavEntry?.navControllerStore?.navControllers?.size)
        val nestedNc = currentNavEntry?.navControllerStore?.get("controller")
        assertNotNull(nestedNc)
        assertEquals("Destination1", nestedNc.getCurrentNavEntry()?.destination?.name)
        assertEquals(1, nestedNc.getBackStack().size)
        assertEquals("Destination4", nestedNc.getBackStack().first().destination.name)
    }

    @Test
    @OptIn(TiamatExperimentalApi::class)
    fun `route # resolve saveable state of nested nav controller`() {
        val nc1 = createTestNavController(saveable = true)
        val nc2 = createTestNavController(saveable = false)
        fun NavController.subNcIsSaveable() = this
            .getCurrentNavEntry()
            ?.navControllerStore
            ?.navControllers[0]
            ?.saveable
        nc1.route {
            destination("1")
            navController("controller")
            destination("2")
        }
        assertEquals(true, nc1.subNcIsSaveable())
        nc2.route {
            destination("1")
            navController("controller")
            destination("2")
        }
        assertEquals(false, nc2.subNcIsSaveable())
        nc1.route {
            destination("1")
            navController("controller", true)
            destination("2")
        }
        assertEquals(true, nc1.subNcIsSaveable())
        nc1.route {
            destination("1")
            navController("controller", false)
            destination("2")
        }
        assertEquals(false, nc1.subNcIsSaveable())
    }

    @Test
    @OptIn(TiamatExperimentalApi::class)
    fun `route # fails on edge conditions`() {
        val nc = createTestNavController()
        assertFails {
            nc.route {
                // empty route
            }
        }
        assertFails {
            nc.route {
                // can not route when current destination is null
                navController("controller")
            }
        }
        assertFails {
            nc.route {
                // can not route when current destination is null (nested nc)
                destination("1")
                navController("controller")
                navController("controller2")
            }
        }
    }

    @Test
    fun `back # navigates to previous entry in backstack`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        val result = nc.back()
        assertTrue(result)
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
        assertEquals(1, nc.getBackStack().size)
        assertEquals(Destination1, nc.getBackStack()[0].destination)
    }

    @Test
    fun `back # returns false when backstack is empty`() {
        val nc = createTestNavController(startDestination = Destination1)
        val result = nc.back()
        assertFalse(result)
        assertEquals(Destination1, nc.getCurrentNavEntry()?.destination)
    }

    @Test
    fun `back # with result sets navResult on target entry`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        val result = "testResult"
        nc.back(result = result)
        assertEquals(result, nc.getCurrentNavEntry()?.getNavResult())
    }

    @Test
    fun `back # with "to" parameter navigates to specific destination`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        nc.navigate(Destination4.toNavEntry())
        val result = nc.back(to = Destination2)
        assertTrue(result)
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
        assertEquals(1, nc.getBackStack().size)
        assertEquals(Destination1, nc.getBackStack()[0].destination)
    }

    @Test
    fun `back # works when "current" is null`() {
        val nc = createTestNavController()
        nc.editBackStack {
            add(Destination1)
            add(Destination2)
            add(Destination3)
        }
        val result = nc.back()
        assertTrue(result)
        assertEquals(Destination3, nc.getCurrentNavEntry()?.destination)
        assertEquals(2, nc.getBackStack().size)
        assertEquals(Destination1, nc.getBackStack()[0].destination)
        assertEquals(Destination2, nc.getBackStack()[1].destination)
    }

    @Test
    fun `back # with inclusive=true removes target destination`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        val result = nc.back(to = Destination2, inclusive = true)
        assertTrue(result)
        assertEquals(Destination1, nc.getCurrentNavEntry()?.destination)
        assertTrue(nc.getBackStack().isEmpty())
    }

    @Test
    fun `back # recursive=true used when back not possible`() {
        val nc1 = createTestNavController(startDestination = Destination1)
        val nc2 = createTestNavController(startDestination = Destination1, parent = nc1)
        val nc3 = createTestNavController(startDestination = Destination1, parent = nc2)
        nc1.navigate(Destination2.toNavEntry())
        assertEquals(Destination2, nc1.getCurrentNavEntry()?.destination)
        nc3.back()
        assertEquals(Destination1, nc1.getCurrentNavEntry()?.destination)
        assertEquals(Destination1, nc2.getCurrentNavEntry()?.destination)
        assertEquals(Destination1, nc3.getCurrentNavEntry()?.destination)
    }

    @Test
    fun `back # redirect called to parent when "to" is not found`() {
        val parentNC = createTestNavController(startDestination = Destination1)
        parentNC.navigate(Destination2.toNavEntry())
        parentNC.navigate(Destination3.toNavEntry())
        parentNC.navigate(Destination4.toNavEntry())
        val childNC = createTestNavController(parent = parentNC, startDestination = Destination1)
        childNC.navigate(Destination3.toNavEntry())
        childNC.back(to = Destination2)
        assertEquals(Destination2, parentNC.getCurrentNavEntry()?.destination)
    }

    @Test
    fun `close # clears all navigation state`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        nc.close()
        assertNull(nc.getCurrentNavEntry())
        assertTrue(nc.getBackStack().isEmpty())
        assertNull(nc.currentTransitionFlow.value)
    }
}