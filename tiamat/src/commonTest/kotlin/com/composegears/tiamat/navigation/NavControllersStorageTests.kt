package com.composegears.tiamat.navigation

import kotlin.test.*

class NavControllersStorageTests {

    @Test
    fun `init # initializes with empty list`() {
        val storage = NavControllerStore()
        assertTrue(storage.navControllers.isEmpty())
    }

    @Test
    fun `add # stores nav controller in internal list`() {
        val storage = NavControllerStore()
        val navController = createTestNavController("test")
        storage.add(navController)
        assertEquals(1, storage.navControllers.size)
        assertEquals(navController, storage.navControllers[0])
    }

    @Test
    fun `add # throws error when adding controller with duplicate key`() {
        val storage = NavControllerStore()
        val key = "duplicate"
        val navController1 = createTestNavController(key)
        val navController2 = createTestNavController(key)
        storage.add(navController1)
        assertFails { storage.add(navController2) }
    }

    @Test
    fun `get # returns null when controller with key doesn't exist`() {
        val storage = NavControllerStore()
        assertNull(storage.get("nonexistent"))
    }

    @Test
    fun `get # returns controller when key exists`() {
        val storage = NavControllerStore()
        val key = "test"
        val navController = createTestNavController(key)
        storage.add(navController)
        assertEquals(navController, storage.get(key))
    }

    @Test
    fun `remove # removes controller from storage`() {
        val storage = NavControllerStore()
        val navController = createTestNavController("test")
        storage.add(navController)
        assertEquals(1, storage.navControllers.size)
        storage.remove(navController)
        assertTrue(storage.navControllers.isEmpty())
    }

    @Test
    fun `saveToSavedState # only saves controllers that are saveable`() {
        val storage = NavControllerStore()
        val saveable = createTestNavController("save", true)
        val notSaveable = createTestNavController("no-save", false)
        storage.add(saveable)
        storage.add(notSaveable)
        val savedState = storage.saveToSavedState()
        val restored = NavControllerStore()
        restored.loadFromSavedState(null, savedState)
        assertEquals(1, restored.navControllers.size)
        assertEquals(saveable.key, restored.navControllers[0].key)
    }

    @Test
    fun `loadFromSavedState # loads controllers from saved state`() {
        val storage = NavControllerStore()
        val controller1 = createTestNavController("test1")
        val controller2 = createTestNavController("test2")
        val savedState = NavControllerStore()
            .apply {
                add(controller1)
                add(controller2)
            }
            .saveToSavedState()
        storage.loadFromSavedState(null, savedState)
        assertEquals(2, storage.navControllers.size)
        assertEquals("test1", storage.navControllers[0].key)
        assertEquals("test2", storage.navControllers[1].key)
    }

    @Test
    fun `loadFromSavedState # attaches parent to restored controllers`() {
        val parent = createTestNavController("parent")
        val storage = NavControllerStore()
        val controller1 = createTestNavController("test1")
        val savedState = NavControllerStore()
            .apply { add(controller1) }
            .saveToSavedState()
        storage.loadFromSavedState(parent, savedState)
        assertEquals(1, storage.navControllers.size)
        assertEquals("test1", storage.navControllers[0].key)
        assertEquals(parent, storage.navControllers[0].parent)
    }

    @Test
    fun `loadFromSavedState # clears existing controllers before loading new ones`() {
        val storage = NavControllerStore()
        val existingController = createTestNavController("existing")
        storage.add(existingController)
        val newController = createTestNavController("new")
        val savedState = NavControllerStore()
            .apply { add(newController) }
            .saveToSavedState()
        storage.loadFromSavedState(null, savedState)
        assertEquals(1, storage.navControllers.size)
        assertEquals("new", storage.navControllers[0].key)
        assertNull(storage.get("existing"))
    }

    @Test
    fun `loadFromSavedState # does nothing when saved state is null`() {
        val storage = NavControllerStore()
        val navController = createTestNavController("test")
        storage.add(navController)
        storage.loadFromSavedState(null, null)
        assertTrue(storage.navControllers.isEmpty())
    }

    @Test
    fun `clear # closes and removes all controllers`() {
        val storage = NavControllerStore()
        val navController1 = createTestNavController("test1")
        val navController2 = createTestNavController("test2")
        storage.add(navController1)
        storage.add(navController2)
        assertEquals(2, storage.navControllers.size)
        storage.clear()
        assertTrue(storage.navControllers.isEmpty())
        assertNull(navController1.getCurrentNavEntry())
        assertNull(navController2.getCurrentNavEntry())
    }

    private class TestNavDestination : NavDestination<Unit> {
        override val name: String = "tnd"
    }

    // Helper functions
    private fun createTestNavController(key: String, saveable: Boolean = true): NavController =
        NavController.create(key = key, saveable = saveable, startDestination = TestNavDestination())
}