package com.composegears.tiamat.destinations

import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.NavDestination
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

@OptIn(TiamatExperimentalApi::class)
class TiamatDestinationsGraphTest {

    @OptIn(TiamatExperimentalApi::class)
    private fun createMockDestination(name: String): NavDestination<Any> {
        return object : NavDestination<Any>(name, typeOf<Any>()) {}
    }

    @Test
    fun `destinations # empty graph throw exception when destinations called`() {
        val graph = object : TiamatGraph {}
        assertFails { graph.destinations() }
    }

    @Test
    fun `plus # merging graphs combines destinations`() {
        val mockDestination1 = createMockDestination("dest1")
        val mockDestination2 = createMockDestination("dest2")
        val mockDestination3 = createMockDestination("dest3")

        val graph1 = object : TiamatGraph {
            override fun destinations(): Array<NavDestination<*>> =
                arrayOf(mockDestination1)
        }

        val graph2 = object : TiamatGraph {
            override fun destinations(): Array<NavDestination<*>> =
                arrayOf(mockDestination2, mockDestination3)
        }

        val mergedGraph = graph1 + graph2
        val destinations = mergedGraph.destinations()

        assertEquals(3, destinations.size)
        assertTrue(destinations.contains(mockDestination1))
        assertTrue(destinations.contains(mockDestination2))
        assertTrue(destinations.contains(mockDestination3))
    }

    @Test
    fun `plus # merging multiple graphs combines destinations`() {
        val mockDestination1 = createMockDestination("dest1")
        val mockDestination2 = createMockDestination("dest2")
        val mockDestination3 = createMockDestination("dest3")

        val graph1 = object : TiamatGraph {
            override fun destinations(): Array<NavDestination<*>> =
                arrayOf(mockDestination1)
        }

        val graph2 = object : TiamatGraph {
            override fun destinations(): Array<NavDestination<*>> =
                arrayOf(mockDestination2)
        }

        val graph3 = object : TiamatGraph {
            override fun destinations(): Array<NavDestination<*>> =
                arrayOf(mockDestination3)
        }

        val mergedGraph = graph1 + graph2 + graph3
        val destinations = mergedGraph.destinations()

        assertEquals(3, destinations.size)
        assertTrue(destinations.contains(mockDestination1))
        assertTrue(destinations.contains(mockDestination2))
        assertTrue(destinations.contains(mockDestination3))
    }

    @Test
    fun `plus # merged graph contains unique destinations`() {
        val mockDestination1 = createMockDestination("dest1")
        val mockDestination2 = createMockDestination("dest2")

        val graph1 = object : TiamatGraph {
            override fun destinations(): Array<NavDestination<*>> =
                arrayOf(mockDestination1, mockDestination2)
        }

        val graph2 = object : TiamatGraph {
            override fun destinations(): Array<NavDestination<*>> =
                arrayOf(mockDestination1) // Duplicate destination
        }

        val mergedGraph = graph1 + graph2
        val destinations = mergedGraph.destinations()

        assertEquals(2, destinations.size)
        assertTrue(destinations.contains(mockDestination1))
        assertTrue(destinations.contains(mockDestination2))
    }
}