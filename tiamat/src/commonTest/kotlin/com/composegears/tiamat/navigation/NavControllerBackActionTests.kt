package com.composegears.tiamat.navigation

import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.createTestNavController
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import kotlin.test.*

class NavControllerBackActionTests {

    companion object {
        val Destination1 by navDestination {}
        val Destination2 by navDestination {}
        val Destination3 by navDestination {}
        val Destination4 by navDestination {}
    }

    @Test
    fun `back - pops one entry for both back behaviours`() {
        NavController.BackBehaviour.entries.forEach { behaviour ->
            val controller = controller(behaviour, Destination1, Destination2, Destination3)
            val removedEntry = controller.getNavStack().last()

            assertTrue(controller.back())
            assertDestinations(controller, Destination1, Destination2)
            assertFalse(removedEntry.isAttachedToNavController)
        }
    }

    @Test
    fun `back - ignores inclusive when no target is provided`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2
        )

        assertTrue(controller.back(inclusive = true))
        assertDestinations(controller, Destination1)
    }

    @Test
    fun `back - stops at root with AllowUntilRoot`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1
        )

        assertFalse(controller.back(recursive = false))
        assertDestinations(controller, Destination1)
        assertFalse(controller.canNavigateBack())
    }

    @Test
    fun `back - removes root with AllowUntilEmpty`() {
        val entry = Destination1.toNavEntry()
        val controller = NavController.create(
            startEntry = entry,
            backBehaviour = NavController.BackBehaviour.AllowUntilEmpty
        )

        assertTrue(controller.back())
        assertTrue(controller.getNavStack().isEmpty())
        assertNull(controller.getCurrentNavEntry())
        assertFalse(entry.isAttachedToNavController)
    }

    @Test
    fun `back - returns false for empty controller`() {
        val controller = createTestNavController(
            backBehaviour = NavController.BackBehaviour.AllowUntilEmpty
        )

        assertFalse(controller.back(recursive = false))
        assertTrue(controller.getNavStack().isEmpty())
    }

    @Test
    fun `back - to destination keeps target and removes entries above it`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2,
            Destination3,
            Destination4
        )

        assertTrue(controller.back(to = Destination2))
        assertDestinations(controller, Destination1, Destination2)
    }

    @Test
    fun `back - to destination inclusive removes target and entries above it`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2,
            Destination3
        )

        assertTrue(controller.back(to = Destination2, inclusive = true))
        assertDestinations(controller, Destination1)
    }

    @Test
    fun `back - to root inclusive is blocked by AllowUntilRoot`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2
        )

        assertFalse(controller.back(to = Destination1, inclusive = true, recursive = false))
        assertDestinations(controller, Destination1, Destination2)
    }

    @Test
    fun `back - to root inclusive clears stack with AllowUntilEmpty`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilEmpty,
            Destination1,
            Destination2
        )

        assertTrue(controller.back(to = Destination1, inclusive = true))
        assertTrue(controller.getNavStack().isEmpty())
    }

    @Test
    fun `back - to current destination do nothing`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2
        )

        assertFalse(controller.back(to = Destination2, recursive = false))
        assertDestinations(controller, Destination1, Destination2)
        assertFalse(controller.back(to = Destination2, inclusive = true))
        assertDestinations(controller, Destination1, Destination2)
    }

    @Test
    fun `back - to duplicate destination uses the last matching entry`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2,
            Destination2,
            Destination3
        )

        assertTrue(controller.back(to = Destination2))
        assertDestinations(controller, Destination1, Destination2, Destination2)
    }

    @Test
    fun `back - to duplicate destination back for 1 step`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2,
            Destination2,
        )

        assertTrue(controller.back(to = Destination2))
        assertDestinations(controller, Destination1, Destination2)
    }

    @Test
    fun `back - to duplicate destination back for 2 steps when inclusive`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2,
            Destination2,
        )

        assertTrue(controller.back(to = Destination2, inclusive = true))
        assertDestinations(controller, Destination1)
    }

    @Test
    fun `back - to missing destination does not change local stack`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilEmpty,
            Destination1,
            Destination2
        )

        assertFalse(controller.back(to = Destination3, recursive = false))
        assertDestinations(controller, Destination1, Destination2)
    }

    @Test
    fun `back - delivers result to surviving target`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2
        )

        assertTrue(controller.back(result = "result"))
        assertEquals("result", controller.getCurrentNavEntry()?.getNavResult<String>())
    }

    @Test
    fun `targeted back delivers result to destination selected by target`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2,
            Destination3
        )

        assertTrue(controller.back(to = Destination1, result = "result"))
        assertEquals("result", controller.getCurrentNavEntry()?.getNavResult<String>())
    }

    @Test
    fun `back - applies transition data and backward transition type`() {
        val controller = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2
        )

        assertTrue(controller.back(transitionData = "transition"))
        assertEquals("transition", controller.navStateFlow.value.transitionData)
        assertEquals(
            NavController.TransitionType.Backward,
            controller.navStateFlow.value.transitionType
        )
    }

    @Test
    fun `back - detaches every removed entry`() {
        val entries = listOf(
            Destination1.toNavEntry(),
            Destination2.toNavEntry(),
            Destination3.toNavEntry(),
            Destination4.toNavEntry()
        )
        val controller = createTestNavController()
        entries.forEach(controller::navigate)

        assertTrue(controller.back(to = Destination2, inclusive = true))
        assertFalse(entries[1].isAttachedToNavController)
        assertFalse(entries[2].isAttachedToNavController)
        assertFalse(entries[3].isAttachedToNavController)
    }

    @Test
    fun `back - recursively delegates when local operation is impossible`() {
        val parent = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2
        )
        val child = NavController.create(
            parent = parent,
            startDestination = Destination3,
            backBehaviour = NavController.BackBehaviour.AllowUntilRoot
        )

        assertTrue(child.back())
        assertDestinations(parent, Destination1)
        assertDestinations(child, Destination3)
    }

    @Test
    fun `back - does not delegate when recursive is false`() {
        val parent = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2
        )
        val child = NavController.create(parent = parent, startDestination = Destination3)

        assertFalse(child.back(recursive = false))
        assertDestinations(parent, Destination1, Destination2)
        assertDestinations(child, Destination3)
    }

    @Test
    fun `back - recursively delegates when target is not local`() {
        val parent = controller(
            NavController.BackBehaviour.AllowUntilRoot,
            Destination1,
            Destination2,
            Destination3
        )
        val child = NavController.create(parent = parent, startDestination = Destination4)

        assertTrue(child.back(to = Destination2))
        assertDestinations(parent, Destination1, Destination2)
        assertDestinations(child, Destination4)
    }

    private fun controller(
        behaviour: NavController.BackBehaviour,
        vararg destinations: NavDestination<*>
    ): NavController {
        val controller = createTestNavController(backBehaviour = behaviour)
        destinations.forEach { controller.navigate(it.toNavEntry()) }
        return controller
    }

    private fun assertDestinations(
        controller: NavController,
        vararg destinations: NavDestination<*>
    ) {
        assertEquals(destinations.toList(), controller.getNavStack().map { it.destination })
    }
}
