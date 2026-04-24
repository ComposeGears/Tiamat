package com.composegears.tiamat.navigation

import com.composegears.tiamat.TiamatExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(TiamatExperimentalApi::class)
class DeepLinkTests {

    @Test
    fun `bind # zero params`() {
        val dl = DeepLink {
            bind("home") { destination("Home") }
        }
        val route = dl.parse("app://x/home")
        assertEquals(1, route.elements.size)
        assertEquals("Home", (route.elements[0] as Route.Destination).name)
    }

    @Test
    fun `bind # single anonymous param`() {
        val dl = DeepLink {
            bind("product/{.*}") { params -> destination("Product[${params[0]}]") }
        }
        val route = dl.parse("app://x/product/42")
        assertEquals(1, route.elements.size)
        assertEquals("Product[42]", (route.elements[0] as Route.Destination).name)
    }

    @Test
    fun `bind # single named param`() {
        val dl = DeepLink {
            bind("product/{id}") { params -> destination("Product[${params[0]}]") }
        }
        val route = dl.parse("app://x/product/99")
        assertEquals(1, route.elements.size)
        assertEquals("Product[99]", (route.elements[0] as Route.Destination).name)
    }

    @Test
    fun `bind # multi param`() {
        val dl = DeepLink {
            bind("product/{pid}/feedback/{fid}") { params ->
                destination("Product[${params[0]}]")
                navController("nested")
                destination("Feedback[${params[1]}]")
            }
        }
        val route = dl.parse("app://x/product/42/feedback/34")
        assertEquals(3, route.elements.size)
        assertEquals("Product[42]", (route.elements[0] as Route.Destination).name)
        assertEquals("nested", (route.elements[1] as Route.NavController).key)
        assertEquals("Feedback[34]", (route.elements[2] as Route.Destination).name)
    }

    @Test
    fun `bind # multiple patterns same builder`() {
        val dl = DeepLink {
            bind("product/{id}", "product?id={id}") { params ->
                destination("Product[${params[0]}]")
            }
        }
        val r1 = dl.parse("product/42")
        assertEquals("Product[42]", (r1.elements[0] as Route.Destination).name)

        val r2 = dl.parse("product?id=99")
        assertEquals("Product[99]", (r2.elements[0] as Route.Destination).name)
    }

    @Test
    fun `bind # query style param`() {
        val dl = DeepLink {
            bind("product?id={.*}") { params -> destination("Product[${params[0]}]") }
        }
        val route = dl.parse("product?id=42")
        assertEquals("Product[42]", (route.elements[0] as Route.Destination).name)
    }

    @Test
    fun `parse # first registered match wins`() {
        val dl = DeepLink {
            bind("product/{.*}") { params -> destination("First[${params[0]}]") }
            bind("product/{.*}x") { params -> destination("Second[${params[0]}]") }
        }
        val route = dl.parse("product/42")
        assertEquals("First[42]", (route.elements[0] as Route.Destination).name)
    }

    @Test
    fun `parse # chained segments`() {
        val dl = DeepLink {
            bind("shop/product/{id}/feedback/{fid}") { params ->
                destination("Shop")
                destination("Product[${params[0]}]")
                navController("pd-nested")
                destination("Feedback[${params[1]}]")
            }
        }
        val route = dl.parse("app://host/shop/product/42/feedback/34")
        assertEquals(4, route.elements.size)
        assertEquals("Shop", (route.elements[0] as Route.Destination).name)
        assertEquals("Product[42]", (route.elements[1] as Route.Destination).name)
        assertEquals("pd-nested", (route.elements[2] as Route.NavController).key)
        assertEquals("Feedback[34]", (route.elements[3] as Route.Destination).name)
    }

    @Test
    fun `parse # no scheme`() {
        val dl = DeepLink {
            bind("home") { destination("Home") }
        }
        val r = dl.parse("/home")
        assertEquals(1, r.elements.size)
        assertTrue(r.elements[0] is Route.Destination)
    }

    @Test
    fun `parse # unknown path throws`() {
        val dl = DeepLink {
            bind("home") { destination("Home") }
        }
        assertFailsWith<IllegalArgumentException> { dl.parse("app://x/unknown") }
    }

    @Test
    fun `parse # empty uri throws`() {
        val dl = DeepLink {
            bind("home") { destination("Home") }
        }
        assertFailsWith<IllegalArgumentException> { dl.parse("app://x/") }
        assertFailsWith<IllegalArgumentException> { dl.parse("") }
    }

    @Test
    fun `build # duplicate pattern throws`() {
        assertFailsWith<IllegalArgumentException> {
            DeepLink {
                bind("product/{.*}") { _ -> destination("A") }
                bind("product/{.*}") { _ -> destination("B") }
            }
        }
    }

    @Test
    fun `build # duplicate across bind calls throws`() {
        assertFailsWith<IllegalArgumentException> {
            DeepLink {
                bind("home", "product/{.*}") { _ -> destination("A") }
                bind("product/{.*}") { _ -> destination("B") }
            }
        }
    }

    @Test
    fun `bind # empty params list for zero-placeholder pattern`() {
        val dl = DeepLink {
            bind("shop") { params ->
                assertEquals(0, params.size)
                destination("Shop")
            }
        }
        dl.parse("shop")
    }
}
