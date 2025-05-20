package com.composegears.tiamat.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteTests {

    @Test
    fun `init # empty list`() {
        val route = Route(emptyList())
        assertTrue(route.elements.isEmpty())
    }

    @Test
    fun `builder # with elements`() {
        val route = Route {
            destination("home")
        }
        assertEquals(1, route.elements.size)
        assertTrue(route.elements[0] is Route.Destination)
        assertEquals("home", (route.elements[0] as Route.Destination).name)
    }

    @Test
    fun `builder # with multiple elements`() {
        val route = Route {
            navController("main")
            destination("home")
            navController("sub")
            destination("details")
        }

        assertEquals(4, route.elements.size)
        assertTrue(route.elements[0] is Route.NavController)
        assertTrue(route.elements[1] is Route.Destination)
        assertTrue(route.elements[2] is Route.NavController)
        assertTrue(route.elements[3] is Route.Destination)

        assertEquals("main", (route.elements[0] as Route.NavController).key)
        assertEquals("home", (route.elements[1] as Route.Destination).name)
        assertEquals("sub", (route.elements[2] as Route.NavController).key)
        assertEquals("details", (route.elements[3] as Route.Destination).name)
    }

    @Test
    fun `element # add element`() {
        val route = Route(emptyList())
        val destination = Route.Destination("profile")
        route.element(destination)

        assertEquals(1, route.elements.size)
        assertEquals(destination, route.elements[0])
    }

    @Test
    fun `destination # add destination`() {
        val route = Route(emptyList())
        route.destination("settings")

        assertEquals(1, route.elements.size)
        assertTrue(route.elements[0] is Route.Destination)
        assertEquals("settings", (route.elements[0] as Route.Destination).name)
    }

    @Test
    fun `navController # add navController`() {
        val route = Route(emptyList())
        route.navController("nc1")
        route.navController("nc2", true)
        route.navController("nc3", false)

        assertEquals(3, route.elements.size)
        assertTrue(route.elements[0] is Route.NavController)
        assertEquals("nc1", (route.elements[0] as Route.NavController).key)
        assertEquals(null, (route.elements[0] as Route.NavController).saveable)
        assertEquals("nc2", (route.elements[1] as Route.NavController).key)
        assertEquals(true, (route.elements[1] as Route.NavController).saveable)
        assertEquals("nc3", (route.elements[2] as Route.NavController).key)
        assertEquals(false, (route.elements[2] as Route.NavController).saveable)
    }
}