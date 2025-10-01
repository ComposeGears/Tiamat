package com.composegears.tiamat.navigation

import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.editNavStack
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
        assertTrue(nc.getNavStack().isEmpty())
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
        val restoredNavStack = restoredNc.getNavStack()
        assertTrue(restoredNc.saveable)
        assertEquals("testKey", restoredNc.key)
        assertEquals(3, restoredNavStack.size)
        assertEquals("Destination1", restoredNavStack[0].destination.name)
        assertEquals("Destination2", restoredNavStack[1].destination.name)
        assertEquals("Destination3", restoredNavStack[2].destination.name)
    }

    @Test
    fun `restoreFromSavedState # restores navStack from empty states`() {
        val restoredNc1 = NavController.restoreFromSavedState(
            savedState = SavedState(
                "saveable" to true,
            )
        )
        val restoredNc2 = NavController.restoreFromSavedState(
            savedState = SavedState(
                "saveable" to true,
                "navStack" to null
            )
        )
        val restoredNc3 = NavController.restoreFromSavedState(
            savedState = SavedState(
                "saveable" to true,
                "navStack" to listOf<SavedState>()
            )
        )
        assertEquals(0, restoredNc1.getNavStack().size)
        assertEquals(0, restoredNc2.getNavStack().size)
        assertEquals(0, restoredNc3.getNavStack().size)
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
        val navStackList = savedState["navStack"] as List<*>
        assertEquals(3, navStackList.size)
        assertEquals("Destination1", navStackList[0].let { it as Map<*, *> }["destination"])
        assertEquals("Destination2", navStackList[1].let { it as Map<*, *> }["destination"])
        assertEquals("Destination3", navStackList[2].let { it as Map<*, *> }["destination"])
    }

    @Test
    fun `setOnNavigationListener # calls listener on navigation events`() {
        val nc = createTestNavController()
        var fromDestination: NavDestination<*>? = null
        var toDestination: NavDestination<*>? = null
        var navType: NavController.TransitionType? = null
        nc.setOnNavigationListener { from, to, type ->
            fromDestination = from?.destination
            toDestination = to?.destination
            navType = type
        }
        assertEquals(0, nc.navStateFlow.value.stack.size)
        assertEquals(NavController.TransitionType.Instant, nc.navStateFlow.value.transitionType)
        nc.navigate(Destination1.toNavEntry())
        assertEquals(null, fromDestination)
        assertEquals(Destination1, toDestination)
        assertEquals(NavController.TransitionType.Forward, navType)
        nc.navigate(Destination2.toNavEntry())
        assertEquals(Destination1, fromDestination)
        assertEquals(Destination2, toDestination)
        assertEquals(NavController.TransitionType.Forward, navType)
        nc.back()
        assertEquals(Destination2, fromDestination)
        assertEquals(Destination1, toDestination)
        assertEquals(NavController.TransitionType.Backward, navType)
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
    fun `getCurrentNavEntry # returns current entry or null`() {
        val nc = createTestNavController()
        assertNull(nc.getCurrentNavEntry())
        nc.navigate(Destination1.toNavEntry())
        assertEquals(Destination1, nc.getCurrentNavEntry()?.destination)
        nc.navigate(Destination2.toNavEntry())
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
        nc.back()
        assertEquals(Destination1, nc.getCurrentNavEntry()?.destination)
    }

    @Test
    fun `getNavStack # returns full navigation stack`() {
        val nc = createTestNavController()
        assertTrue(nc.getNavStack().isEmpty())
        nc.navigate(Destination1.toNavEntry())
        val stack1 = nc.getNavStack()
        assertEquals(1, stack1.size)
        assertEquals(Destination1, stack1[0].destination)
        nc.navigate(Destination2.toNavEntry())
        val stack2 = nc.getNavStack()
        assertEquals(2, stack2.size)
        assertEquals(Destination1, stack2[0].destination)
        assertEquals(Destination2, stack2[1].destination)
        nc.back()
        val stack3 = nc.getNavStack()
        assertEquals(1, stack3.size)
        assertEquals(Destination1, stack3[0].destination)
    }

    @Test
    fun `canNavigateBack # returns true when there is entries to back to`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        assertTrue(nc.getNavStack().size > 1)
        assertTrue(nc.canNavigateBack())
    }

    @Test
    fun `canNavigateBack # returns false when no entries to go back to`() {
        val nc1 = createTestNavController()
        assertTrue(nc1.getNavStack().size <= 1)
        assertFalse(nc1.canNavigateBack())
        val nc2 = createTestNavController(startDestination = Destination1)
        assertTrue(nc2.getNavStack().size <= 1)
        assertFalse(nc2.canNavigateBack())
    }

    @Test
    fun `editNavStack # attaches new entries`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.editNavStack(null, NavController.TransitionType.Forward) { old ->
            old + listOf(Destination3.toNavEntry(), Destination4.toNavEntry())
        }
        val stack = nc.getNavStack()
        assertEquals(4, stack.size)
        assertEquals(Destination1, stack[0].destination)
        assertEquals(Destination2, stack[1].destination)
        assertEquals(Destination3, stack[2].destination)
        assertTrue(stack[2].isAttachedToNavController)
        assertEquals(Destination4, stack[3].destination)
        assertTrue(stack[3].isAttachedToNavController)
    }

    @Test
    fun `editNavStack # detaches removed entries`() {
        val removedEntry = Destination1.toNavEntry()
        val nc = createTestNavController()
        nc.navigate(removedEntry)
        nc.navigate(Destination2.toNavEntry())
        nc.editNavStack(null, NavController.TransitionType.Forward) { old ->
            old - removedEntry + listOf(Destination3.toNavEntry(), Destination4.toNavEntry())
        }
        val stack = nc.getNavStack()
        assertEquals(3, stack.size)
        assertEquals(Destination2, stack[0].destination)
        assertEquals(Destination3, stack[1].destination)
        assertEquals(Destination4, stack[2].destination)
        assertFalse(removedEntry.isAttachedToNavController)
    }

    @Test
    fun `editNavStack # do nothing with existed entries`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.editNavStack(null, NavController.TransitionType.Forward) { old ->
            old + listOf(Destination3.toNavEntry(), Destination4.toNavEntry())
        }
        val stack = nc.getNavStack()
        assertEquals(4, stack.size)
        assertEquals(Destination1, stack[0].destination)
        assertTrue(stack[0].isAttachedToNavController)
        assertEquals(Destination2, stack[1].destination)
        assertTrue(stack[1].isAttachedToNavController)
        assertEquals(Destination3, stack[2].destination)
        assertEquals(Destination4, stack[3].destination)
    }

    @Test
    fun `editNavStack # apply transition data & type`() {
        val transitionData = "testData"
        val nc = createTestNavController(startDestination = Destination1)
        nc.editNavStack(transitionData, NavController.TransitionType.Backward) { _ ->
            listOf(Destination2.toNavEntry())
        }
        assertEquals(transitionData, nc.navStateFlow.value.transitionData)
        assertEquals(NavController.TransitionType.Backward, nc.navStateFlow.value.transitionType)
        assertEquals(1, nc.getNavStack().size)
        assertEquals(Destination2, nc.getNavStack()[0].destination)
    }

    @Test
    fun `editNavStack # nothing happen if same stack provided`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        val stack = nc.navStateFlow.value.stack
        nc.editNavStack { old -> old }
        assertSame(nc.navStateFlow.value.stack, stack)
    }

    @Test
    fun `navigate # updates current entry and adds previous entry to stack`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        assertEquals(2, nc.getNavStack().size)
        assertEquals(Destination1, nc.getNavStack()[0].destination)
        assertEquals(Destination2, nc.getNavStack()[1].destination)
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
    }

    @Test
    fun `replace # updates current entry without adding to stack`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
        assertEquals(2, nc.getNavStack().size)
        val toBeReplaced = nc.getCurrentNavEntry()
        nc.replace(Destination3.toNavEntry())
        assertEquals(false, toBeReplaced?.isAttachedToNavController)
        assertEquals(Destination3, nc.getCurrentNavEntry()?.destination)
        assertEquals(2, nc.getNavStack().size)
        assertEquals(Destination1, nc.getNavStack()[0].destination)
        assertEquals(Destination3, nc.getNavStack()[1].destination)
    }

    @Test
    fun `popToTop # navigates to existing entry from stack`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        nc.popToTop(Destination1) {}
        assertEquals(3, nc.getNavStack().size)
        assertEquals(Destination2, nc.getNavStack()[0].destination)
        assertEquals(Destination3, nc.getNavStack()[1].destination)
        assertEquals(Destination1, nc.getNavStack()[2].destination)
        assertEquals(Destination1, nc.getCurrentNavEntry()?.destination)
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
    fun `popToTop # do nothing if pop-ing last element`() {
        val nc = createTestNavController()
        val lastEntry = Destination2.toNavEntry()
        nc.navigate(Destination1.toNavEntry())
        nc.navigate(lastEntry)
        nc.popToTop(Destination2) {}
        assertEquals(2, nc.getNavStack().size)
        assertEquals(Destination1, nc.getNavStack()[0].destination)
        assertEquals(Destination2, nc.getNavStack()[1].destination)
        assertEquals(lastEntry, nc.getNavStack()[1])
    }

    @Test
    @OptIn(TiamatExperimentalApi::class)
    fun `route # replace current destination`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.route {
            element(Destination2)
        }
        assertEquals(1, nc.getNavStack().size)
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
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

        assertEquals(3, nc.getNavStack().size)
        assertEquals("Destination1", nc.getNavStack()[0].destination.name)
        assertEquals("Destination2", nc.getNavStack()[1].destination.name)
        assertEquals("Destination3", nc.getNavStack()[2].destination.name)
        val currentNavEntry = nc.getCurrentNavEntry()
        assertEquals("Destination3", currentNavEntry?.destination?.name)
        assertEquals(1, currentNavEntry?.navControllerStore?.navControllers?.size)
        val nestedNc = currentNavEntry?.navControllerStore?.get("controller")
        assertNotNull(nestedNc)
        assertEquals(2, nestedNc.getNavStack().size)
        assertEquals("Destination4", nestedNc.getNavStack()[0].destination.name)
        assertEquals("Destination1", nestedNc.getNavStack()[1].destination.name)
        assertEquals("Destination1", nestedNc.getCurrentNavEntry()?.destination?.name)
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
    fun `back # navigates to previous entry in stack`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        val result = nc.back()
        assertTrue(result)
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
        assertEquals(2, nc.getNavStack().size)
        assertEquals(Destination1, nc.getNavStack()[0].destination)
        assertEquals(Destination2, nc.getNavStack()[1].destination)
    }

    @Test
    fun `back # returns false when nothing to back to`() {
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
        val stack = nc.getNavStack()
        val result = nc.back(to = Destination2)
        assertTrue(result)
        assertEquals(2, nc.getNavStack().size)
        assertEquals(false, stack[2].isAttachedToNavController)
        assertEquals(false, stack[3].isAttachedToNavController)
        assertEquals(Destination1, nc.getNavStack()[0].destination)
        assertEquals(Destination2, nc.getNavStack()[1].destination)
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
    }

    @Test
    fun `back # with inclusive=true removes target destination`() {
        val nc = createTestNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        val result = nc.back(to = Destination2, inclusive = true)
        assertTrue(result)
        assertEquals(1, nc.getNavStack().size)
        assertEquals(Destination1, nc.getNavStack()[0].destination)
        assertEquals(Destination1, nc.getCurrentNavEntry()?.destination)
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
    fun `back # return false when "to" is not found and it is not recursive`() {
        val parentNC = createTestNavController(startDestination = Destination1)
        parentNC.navigate(Destination2.toNavEntry())
        parentNC.navigate(Destination3.toNavEntry())
        parentNC.navigate(Destination4.toNavEntry())
        val childNC = createTestNavController(parent = parentNC, startDestination = Destination1)
        childNC.navigate(Destination3.toNavEntry())
        val result = childNC.back(to = Destination2, recursive = false)
        assertFalse(result)
        assertEquals(Destination4, parentNC.getCurrentNavEntry()?.destination)
        assertEquals(Destination3, childNC.getCurrentNavEntry()?.destination)
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
    fun `resolveNavDestinations # resolve known destinations`() {
        val destinations = listOf(Destination1, Destination2, Destination3)
        val nc = createTestNavController()
        nc.navigate(NavDestination.Unresolved(Destination1.name).toNavEntry())
        nc.navigate(NavDestination.Unresolved(Destination2.name).toNavEntry())
        nc.resolveNavDestinations { name -> destinations.find { it.name == name } }
        assertTrue(nc.getNavStack()[0].isResolved)
        assertTrue(nc.getNavStack()[1].isResolved)
        nc.navigate(NavDestination.Unresolved("KeptUnresolved").toNavEntry())
        assertFails {
            nc.resolveNavDestinations { name -> destinations.find { it.name == name } }
        }
        assertFalse(nc.getNavStack()[2].isResolved)
    }

    @Test
    fun `close # clears all navigation state`() {
        val items = listOf(
            Destination1.toNavEntry(),
            Destination2.toNavEntry(),
            Destination3.toNavEntry(),
        )
        val nc = createTestNavController()
        items.onEach { nc.navigate(it) }
        nc.close()
        assertNull(nc.getCurrentNavEntry())
        assertTrue(nc.getNavStack().isEmpty())
        items.onEach {
            assertFalse(it.isAttachedToNavController)
        }
    }
}