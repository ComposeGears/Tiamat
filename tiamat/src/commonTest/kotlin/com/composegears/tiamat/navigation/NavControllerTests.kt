package com.composegears.tiamat.navigation

import kotlin.collections.get
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class NavControllerTests {
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
        parent: NavController? = null,
        startDestination: NavDestination<*>? = null,
    ) = NavController.create(
        key = key,
        parent = parent,
        startDestination = startDestination,
    )
    // ----------- tests -----------------------------------------------------------------------------------------------

    @Test
    fun `NavController | init | navigates to 'startDestination'`() {
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        assertEquals(Destination1, nc.current)
    }

    @Test
    fun `NavController | properties | 'current*' fields updates on navigation`() {
        // stack: 1
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        nc.navigate(Destination2)
        // stack: 1 2
        assertEquals(Destination2, nc.current)
        nc.navigate(Destination3)
        // stack: 1 2 3
        assertEquals(Destination3, nc.current)
        nc.replace(Destination4)
        // stack: 1 2 4
        assertEquals(Destination4, nc.current)
        nc.popToTop(Destination2)
        // stack: 1 4 2
        assertEquals(Destination2, nc.current)
        nc.popToTop(Destination3)
        // stack: 1 4 2 3
        assertEquals(Destination3, nc.current)
    }

    @Test
    fun `NavController | properties | 'canGoBack' updates on navigation`() {
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        assertEquals(false, nc.canGoBack)
        nc.navigate(Destination2)
        assertEquals(true, nc.canGoBack)
    }

    @Test
    fun `NavController | saveState | saved to state`() {
        val nc = createSimpleNavController(
            key = "testController",
            startDestination = Destination1
        )
        nc.navigate(Destination2)
        nc.navigate(Destination3)

        val savedState = nc.saveToSavedState()

        assertEquals("testController", savedState["key"])
        val currentSavedState = savedState["current"] as Map<*, *>
        assertEquals("3", currentSavedState["destination"])
        val backStackList = savedState["backStack"] as List<*>
        assertEquals(2, backStackList.size)
        val firstBackStackEntry = backStackList[0] as Map<*, *>
        assertEquals("1", firstBackStackEntry["destination"])
        val secondBackStackEntry = backStackList[1] as Map<*, *>
        assertEquals("2", secondBackStackEntry["destination"])
    }

    @Test
    fun `NavController | saveState | restored from state`() {
        val originalNc = createSimpleNavController(
            key = "testController",
            startDestination = Destination1
        )
        originalNc.navigate(Destination2)
        originalNc.navigate(Destination3)
        val savedState = originalNc.saveToSavedState()

        val restoredNc = NavController.restoreFromSavedState(savedState)

        assertEquals("testController", restoredNc.key)
        assertEquals("3", restoredNc.current?.name)
        val backStack = restoredNc.getBackStack()
        assertEquals(2, backStack.size)
        assertEquals("1", backStack[0].destination.name)
        assertEquals("2", backStack[1].destination.name)
    }

    @Test
    fun `NavController | parent | can find it's parent`() {
        val nc1 = createSimpleNavController("nc1")
        val nc11 = createSimpleNavController("nc11", parent = nc1)
        val nc12 = createSimpleNavController("nc12", parent = nc1)
        val nc111 = createSimpleNavController("nc111", parent = nc11)
        val nc112 = createSimpleNavController("nc112", parent = nc11)
        val nc121 = createSimpleNavController("nc121", parent = nc12)
        val nc122 = createSimpleNavController("nc122", parent = nc12)
        val nc1111 = createSimpleNavController("nc1111", parent = nc111)
        val nc1112 = createSimpleNavController("nc1112", parent = nc111)
        val nc1121 = createSimpleNavController("nc1121", parent = nc112)
        val nc1122 = createSimpleNavController("nc1122", parent = nc112)
        val nc1211 = createSimpleNavController("nc1211", parent = nc121)
        val nc1212 = createSimpleNavController("nc1212", parent = nc121)
        val nc1221 = createSimpleNavController("nc1221", parent = nc122)
        val nc1222 = createSimpleNavController("nc1222", parent = nc122)

        assertEquals(nc1, nc1111.findParentNavController("nc1"))
        assertEquals(nc1, nc1112.findParentNavController("nc1"))
        assertEquals(nc1, nc1121.findParentNavController("nc1"))
        assertEquals(nc1, nc1122.findParentNavController("nc1"))
        assertEquals(nc1, nc1211.findParentNavController("nc1"))
        assertEquals(nc1, nc1212.findParentNavController("nc1"))
        assertEquals(nc1, nc1221.findParentNavController("nc1"))
        assertEquals(nc1, nc1222.findParentNavController("nc1"))

        assertEquals(nc11, nc1111.findParentNavController("nc11"))
        assertEquals(nc11, nc1112.findParentNavController("nc11"))
        assertEquals(nc11, nc1121.findParentNavController("nc11"))
        assertEquals(nc11, nc1122.findParentNavController("nc11"))
        assertEquals(null, nc1211.findParentNavController("nc11"))
        assertEquals(null, nc1212.findParentNavController("nc11"))
        assertEquals(null, nc1221.findParentNavController("nc11"))
        assertEquals(null, nc1222.findParentNavController("nc11"))

        assertEquals(null, nc1111.findParentNavController("nc12"))
        assertEquals(null, nc1112.findParentNavController("nc12"))
        assertEquals(null, nc1121.findParentNavController("nc12"))
        assertEquals(null, nc1122.findParentNavController("nc12"))
        assertEquals(nc12, nc1211.findParentNavController("nc12"))
        assertEquals(nc12, nc1212.findParentNavController("nc12"))
        assertEquals(nc12, nc1221.findParentNavController("nc12"))
        assertEquals(nc12, nc1222.findParentNavController("nc12"))
    }

    @Test
    fun `NavController | backstack | provides it's backstack`() {
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        nc.navigate(Destination2)
        // stack: 1 2
        assertContentEquals(
            listOf(Destination1),
            nc.getBackStack().map { it.destination }
        )
        nc.navigate(Destination3)
        // stack: 1 2 3
        assertContentEquals(
            listOf(Destination1, Destination2),
            nc.getBackStack().map { it.destination }
        )
        nc.replace(Destination4)
        // stack: 1 2 4
        assertContentEquals(
            listOf(Destination1, Destination2),
            nc.getBackStack().map { it.destination }
        )
        nc.popToTop(Destination2)
        // stack: 1 4 2
        assertContentEquals(
            listOf(Destination1, Destination4),
            nc.getBackStack().map { it.destination }
        )
        nc.popToTop(Destination3)
        // stack: 1 4 2 3
        assertContentEquals(
            listOf(Destination1, Destination4, Destination2),
            nc.getBackStack().map { it.destination }
        )
    }

    @Test
    fun `NavController | backstack | allows to edit backstack`() {
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        nc.navigate(Destination2)
        nc.navigate(Destination3)
        nc.navigate(Destination4)
        nc.navigate(Destination1)
        // bs = 1 2 3 4  current = 1
        nc.editBackStack {
            removeLast()
            assertContentEquals(
                listOf(Destination1, Destination2, Destination3),
                nc.getBackStack().map { it.destination }
            )
            removeAt(1)
            assertContentEquals(
                listOf(Destination1, Destination3),
                nc.getBackStack().map { it.destination }
            )
            removeLast(Destination1)
            assertContentEquals(
                listOf(Destination3),
                nc.getBackStack().map { it.destination }
            )
            removeLast { it.destination == Destination3 }
            assertContentEquals(
                listOf(),
                nc.getBackStack().map { it.destination }
            )
            add(Destination1)
            add(Destination3)
            add(Destination2)
            assertContentEquals(
                listOf(Destination1, Destination3, Destination2),
                nc.getBackStack().map { it.destination }
            )
            add(2, Destination4)
            assertContentEquals(
                listOf(Destination1, Destination3, Destination4, Destination2),
                nc.getBackStack().map { it.destination }
            )
            clear()
            assertContentEquals(
                listOf(),
                nc.getBackStack().map { it.destination }
            )
            set(Destination1, Destination2, Destination3)
            assertContentEquals(
                listOf(Destination1, Destination2, Destination3),
                nc.getBackStack().map { it.destination }
            )
            assertEquals(3, size())
        }
    }

    @Test
    fun `NavController | navigation | forward`() {
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        nc.navigate(Destination2)
        assertEquals(Destination2, nc.current)
        nc.navigate(Destination3)
        assertEquals(Destination3, nc.current)
        nc.navigate(Destination4)
        assertEquals(Destination4, nc.current)
    }

    @Test
    fun `NavController | navigation | replace`() {
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        nc.replace(Destination2)
        assertEquals(Destination2, nc.current)
        nc.navigate(Destination4)
        nc.replace(Destination3)
        assertEquals(Destination3, nc.current)
        nc.replace(Destination4)
        assertEquals(Destination4, nc.current)
    }

    @Test
    fun `NavController | navigation | popToTop`() {
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        nc.navigate(Destination2)
        nc.navigate(Destination3)
        nc.popToTop(Destination1) // 231
        assertEquals(Destination1, nc.current)
        nc.popToTop(Destination2) // 312
        assertEquals(Destination2, nc.current)
        nc.popToTop(Destination3) // 123
        assertEquals(Destination3, nc.current)
        nc.popToTop(Destination4) // 1234
        assertEquals(Destination4, nc.current)
        assertContentEquals(
            listOf(Destination1, Destination2, Destination3),
            nc.getBackStack().map { it.destination }
        )
    }

    @Test
    fun `NavController | navigation | route`() {
        TODO()
    }

    @Test
    fun `NavController | navigation | simple back`() {
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        // cant go back if no backstack & parent
        assertEquals(false, nc.canGoBack)
        assertEquals(false, nc.back())

        nc.navigate(Destination2)
        nc.navigate(Destination3)
        nc.navigate(Destination4)
        // simple back
        assertEquals(true, nc.canGoBack)
        nc.back()
        assertEquals(Destination3, nc.current)
    }

    @Test
    fun `NavController | navigation | back-with-result provides result`() {
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        nc.navigate(Destination2)
        nc.navigate(Destination3)
        assertEquals(true, nc.canGoBack)
        assertEquals(true, nc.back(result = 1))
        assertEquals(Destination2, nc.current)
        assertEquals(1, nc.currentNavEntry?.navResult)
    }

    @Test
    fun `NavController | navigation | backTo`() {
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        nc.navigate(Destination2)
        nc.navigate(Destination3)
        nc.navigate(Destination4)
        // ----- positive -------
        assertEquals(true, nc.canGoBack)
        assertEquals(true, nc.back(to = Destination2, inclusive = false, recursive = false))
        assertEquals(Destination2, nc.current)
        // ----- negative -------
        assertEquals(true, nc.canGoBack)
        assertEquals(false, nc.back(to = Destination3, inclusive = false, recursive = false))
        assertEquals(Destination2, nc.current)
        // ----- multi ----------
        nc.navigate(Destination3)
        nc.navigate(Destination4)
        nc.navigate(Destination1)
        nc.navigate(Destination2)
        nc.navigate(Destination3)
        nc.navigate(Destination4) // nc=[1234123 current=4]
        nc.back(to = Destination2, inclusive = false, recursive = false) // expect [12341 current = 2]
        assertEquals(Destination2, nc.current)
        assertContentEquals(
            listOf(Destination1, Destination2, Destination3, Destination4, Destination1),
            nc.getBackStack().map { it.destination }
        )
    }

    @Test
    fun `NavController | navigation | back-inclusive`() {
        val nc = createSimpleNavController(
            startDestination = Destination1
        )
        nc.navigate(Destination2)
        nc.navigate(Destination3)
        nc.navigate(Destination4)
        // ----- positive -------
        assertEquals(true, nc.canGoBack)
        assertEquals(
            true,
            nc.back(to = Destination2, inclusive = true, recursive = false)
        )
        assertEquals(Destination1, nc.current)
        // ----- negative -------
        assertEquals(false, nc.canGoBack)
        assertEquals(false, nc.back(to = Destination3, inclusive = true, recursive = false))
        assertEquals(Destination1, nc.current)
        // ----- root -------
        nc.navigate(Destination2)
        nc.navigate(Destination3)
        nc.navigate(Destination4)
        assertEquals(true, nc.canGoBack)
        assertEquals(false, nc.back(to = Destination1, inclusive = true, recursive = false))
        assertEquals(Destination4, nc.current)
    }

    @Test
    fun `NavController | navigation | back-recursive`() {
        val nc1 = createSimpleNavController(
            startDestination = Destination1
        )
        nc1.navigate(Destination2)
        val nc2 = createSimpleNavController(
            parent = nc1,
            startDestination = Destination3
        )
        // from root
        assertEquals(false, nc2.canGoBack)
        assertEquals(true, nc2.back(recursive = true))
        assertEquals(Destination1, nc1.current)
        // back to root inclusive
        nc1.navigate(Destination2) // nc1=[12]
        nc2.navigate(Destination4) // nc2=[34]
        assertEquals(true, nc2.back(to = Destination3, inclusive = true, recursive = true))
        assertEquals(true, nc2.back(recursive = true))
        assertEquals(Destination1, nc1.current)
        // back to parent dest
        nc1.navigate(Destination2) // nc1=[12]  nc2=[3]
        // should not navigate as D4 is not in any bs
        assertEquals(false, nc2.back(to = Destination4, inclusive = false, recursive = true))
        nc2.navigate(Destination4) // nc2=[34]
        // should not navigate as D2 is current for nc1 and not in bs
        assertEquals(false, nc2.back(to = Destination2, inclusive = false, recursive = true))
        assertEquals(true, nc2.back(to = Destination1, inclusive = false, recursive = true))
        assertEquals(Destination1, nc1.current)
    }

    @Test
    fun `NavController | navigation | listener is called on navigation`() {
        val nc = createSimpleNavController()
        var lastNavFrom: NavDestination<*>? = null
        var lastNavTo: NavDestination<*>? = null
        var lastNavOsForward: Boolean? = null
        nc.setOnNavigationListener { from, to, isForward ->
            lastNavFrom = from?.destination
            lastNavTo = to?.destination
            lastNavOsForward = isForward
        }
        fun assertCallbackResult(
            from: NavDestination<*>?,
            to: NavDestination<*>?,
            isForward: Boolean,
            message: String? = null
        ) {
            assertEquals(from, lastNavFrom, message)
            assertEquals(to, lastNavTo, message)
            assertEquals(isForward, lastNavOsForward, message)
        }

        nc.navigate(Destination1)
        assertCallbackResult(null, Destination1, true)
        nc.back() // will not navigate - bs is empty
        assertCallbackResult(null, Destination1, true)
        nc.navigate(Destination2)
        assertCallbackResult(Destination1, Destination2, true)
        nc.navigate(Destination3)
        assertCallbackResult(Destination2, Destination3, true)
        nc.back()
        assertCallbackResult(Destination3, Destination2, false)
        nc.navigate(Destination3)
        nc.navigate(Destination4)
        nc.back(to = Destination2, inclusive = false, recursive = false)
        assertCallbackResult(Destination4, Destination2, false)
        nc.navigate(Destination3)
        nc.navigate(Destination4)
        nc.back(to = Destination2, inclusive = true, recursive = false)
        assertCallbackResult(Destination4, Destination1, false)
        nc.navigate(Destination2)
        nc.navigate(Destination3)
        nc.popToTop(Destination1)
        assertCallbackResult(Destination3, Destination1, true)
        nc.popToTop(Destination4)
        assertCallbackResult(Destination1, Destination4, true)
    }
}