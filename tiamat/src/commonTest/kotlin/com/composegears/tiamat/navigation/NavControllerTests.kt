package com.composegears.tiamat.navigation

import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import kotlin.test.*

class NavControllerTests {

    @Test
    fun `create # initialized empty`() {
        val nc = createSimpleNavController()
        assertNull(nc.getCurrentNavEntry())
        assertTrue(nc.getBackStack().isEmpty())
    }

    @Test
    fun `create # navigates to startDestination when provided`() {
        val nc = createSimpleNavController(startDestination = Destination1)
        val currentEntry = nc.getCurrentNavEntry()
        assertNotNull(currentEntry)
        assertEquals(Destination1, currentEntry.destination)
    }

    @Test
    fun `restoreFromSavedState # restores navController state`() {
        val originalNc = createSimpleNavController(
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
        assertEquals("3", restoredNc.getCurrentNavEntry()?.destination?.name)
        val backStack = restoredNc.getBackStack()
        assertEquals(2, backStack.size)
        assertEquals("1", backStack[0].destination.name)
        assertEquals("2", backStack[1].destination.name)
    }

    @Test
    fun `saveToSavedState # saves navController state`() {
        val nc = createSimpleNavController(
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
        assertEquals("3", currentSavedState["destination"])
        val backStackList = savedState["backStack"] as List<*>
        assertEquals(2, backStackList.size)
    }

    @Test
    fun `setOnNavigationListener # calls listener on navigation events`() {
        val nc = createSimpleNavController()
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
        val rootNc = createSimpleNavController(key = "root")
        val childNc = createSimpleNavController(key = "child", parent = rootNc)
        val grandchildNc = createSimpleNavController(key = "grandchild", parent = childNc)
        assertEquals(rootNc, grandchildNc.findParentNavController("root"))
        assertEquals(childNc, grandchildNc.findParentNavController("child"))
        assertEquals(null, grandchildNc.findParentNavController("nonexistent"))
    }

    @Test
    fun `canGoBack # returns true when backstack is not empty`() {
        val nc = createSimpleNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        assertTrue(nc.getBackStack().isNotEmpty())
        assertTrue(nc.canGoBack())
    }

    @Test
    fun `canGoBack # returns false when backstack is empty`() {
        val nc = createSimpleNavController(startDestination = Destination1)
        assertTrue(nc.getBackStack().isEmpty())
        assertFalse(nc.canGoBack())
    }

    // todo add more cases, test all nested methods
    @Test
    fun `editBackStack # allows modifying the backstack`() {
        /* val nc = createSimpleNavController(startDestination = Destination1)
         nc.navigate(Destination2.toNavEntry())
         nc.editBackStack {
             add(Destination3)
             add(Destination4)
         }
         val backStack = nc.getBackStack()
         assertEquals(3, backStack.size)
         assertEquals(Destination1, backStack[0].destination)
         assertEquals(Destination3, backStack[1].destination)
         assertEquals(Destination4, backStack[2].destination)*/
    }

    @Test
    fun `navigate # updates current entry and adds previous entry to backstack`() {
        val nc = createSimpleNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
        assertEquals(1, nc.getBackStack().size)
        assertEquals(Destination1, nc.getBackStack()[0].destination)
    }

    @Test
    fun `replace # updates current entry without adding to backstack`() {
        val nc = createSimpleNavController(startDestination = Destination1)
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
        val nc = createSimpleNavController(startDestination = Destination1)
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
        val nc = createSimpleNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        var orElseCalled = false
        nc.popToTop(Destination3) {
            orElseCalled = true
        }
        assertTrue(orElseCalled)
    }

    @Test
    @OptIn(TiamatExperimentalApi::class)
    fun `route # replace current destination`() {
        val nc = createSimpleNavController(startDestination = Destination1)
        nc.route {
            element(Destination2)
        }
        assertEquals(Destination2, nc.getCurrentNavEntry()?.destination)
        assertEquals(0, nc.getBackStack().size)
    }

    @Test
    @OptIn(TiamatExperimentalApi::class)
    fun `route # navigates through multiple destinations`() {
        val nc = createSimpleNavController()
        nc.route {
            destination("1")
            element(Destination2)
            element(Destination3.toNavEntry())
            navController("controller")
            destination("4")
            destination("1")
        }

        assertEquals(2, nc.getBackStack().size)
        assertEquals("1", nc.getBackStack()[0].destination.name)
        assertEquals("2", nc.getBackStack()[1].destination.name)
        val currentNavEntry = nc.getCurrentNavEntry()
        assertEquals("3", currentNavEntry?.destination?.name)
        assertEquals(1, currentNavEntry?.navControllersStorage?.nestedNavControllers?.size)
        val nestedNc = currentNavEntry?.navControllersStorage?.get("controller")
        assertNotNull(nestedNc)
        assertEquals("1", nestedNc.getCurrentNavEntry()?.destination?.name)
        assertEquals(1, nestedNc.getBackStack().size)
        assertEquals("4", nestedNc.getBackStack().first().destination.name)
    }

    @Test
    @OptIn(TiamatExperimentalApi::class)
    fun `route # fails on edge conditions`() {
        val nc = createSimpleNavController()
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
        val nc = createSimpleNavController(startDestination = Destination1)
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
        val nc = createSimpleNavController(startDestination = Destination1)
        val result = nc.back()
        assertFalse(result)
        assertEquals(Destination1, nc.getCurrentNavEntry()?.destination)
    }

    @Test
    fun `back # with result sets navResult on target entry`() {
        val nc = createSimpleNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        val result = "testResult"
        nc.back(result = result)
        assertEquals(result, nc.getCurrentNavEntry()?.navResult)
    }

    @Test
    fun `back # with "to" parameter navigates to specific destination`() {
        val nc = createSimpleNavController(startDestination = Destination1)
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
    fun `back # with inclusive=true removes target destination`() {
        val nc = createSimpleNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        val result = nc.back(to = Destination2, inclusive = true)
        assertTrue(result)
        assertEquals(Destination1, nc.getCurrentNavEntry()?.destination)
        assertTrue(nc.getBackStack().isEmpty())
    }

    @Test
    fun `back # orElse called when back not possible`() {
        val nc = createSimpleNavController(startDestination = Destination1)
        var orElseCalled = false
        nc.back(orElse = { orElseCalled = true; true })
        assertTrue(orElseCalled)
        orElseCalled = false
        nc.navigate(Destination2.toNavEntry())
        nc.back(to = Destination3, orElse = { orElseCalled = true; true })
        assertTrue(orElseCalled)
    }

    @Test
    fun `back # redirect called to parent when "to" is not found`() {
        val parentNC = createSimpleNavController(startDestination = Destination1)
        parentNC.navigate(Destination2.toNavEntry())
        parentNC.navigate(Destination3.toNavEntry())
        parentNC.navigate(Destination4.toNavEntry())
        val childNC = createSimpleNavController(parent = parentNC, startDestination = Destination1)
        childNC.navigate(Destination3.toNavEntry())
        childNC.back(to = Destination2)
        assertEquals(Destination2, parentNC.getCurrentNavEntry()?.destination)
    }

    @Test
    fun `close # clears all navigation state`() {
        val nc = createSimpleNavController(startDestination = Destination1)
        nc.navigate(Destination2.toNavEntry())
        nc.navigate(Destination3.toNavEntry())
        nc.close()
        assertNull(nc.getCurrentNavEntry())
        assertTrue(nc.getBackStack().isEmpty())
        assertNull(nc.currentTransitionFlow.value)
    }

    // ----------- helpers ---------------------------------------------------------------------------------------------

    abstract class SimpleNavDestination(override val name: String) : NavDestination<Unit> {
        override fun toString(): String = "NavDestination($name)"
    }

    object Destination1 : SimpleNavDestination("1")
    object Destination2 : SimpleNavDestination("2")
    object Destination3 : SimpleNavDestination("3")
    object Destination4 : SimpleNavDestination("4")

    fun createSimpleNavController(
        key: String? = null,
        saveable: Boolean = true,
        parent: NavController? = null,
        startDestination: NavDestination<*>? = null,
    ) = NavController.create(
        key = key,
        saveable = saveable,
        parent = parent,
        startEntry = startDestination?.toNavEntry(),
        config = {}
    )
}