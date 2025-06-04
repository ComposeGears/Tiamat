package com.composegears.tiamat.navigation

import kotlin.test.*

class ViewModelsStorageTests {

    @Test
    fun `get # creates and returns new viewmodel`() {
        val storage = ViewModelsStorage()
        val viewModel = storage.get("testKey") { TestViewModel() }
        assertNotNull(viewModel)
        assertEquals(1, storage.viewModels.size)
        assertTrue(storage.viewModels.containsKey("testKey"))
    }

    @Test
    fun `get # returns existing viewmodel for same key`() {
        val storage = ViewModelsStorage()
        val firstViewModel = storage.get("testKey") { TestViewModel() }
        val secondViewModel = storage.get("testKey") { TestViewModel() }
        assertNotNull(firstViewModel)
        assertNotNull(secondViewModel)
        assertSame(firstViewModel, secondViewModel, "Should return the same instance for the same key")
        assertEquals(1, storage.viewModels.size)
    }

    @Test
    fun `get # creates different viewmodels for different keys`() {
        val storage = ViewModelsStorage()
        val firstViewModel = storage.get("key1") { TestViewModel() }
        val secondViewModel = storage.get("key2") { TestViewModel() }
        assertNotNull(firstViewModel)
        assertNotNull(secondViewModel)
        assertNotSame(firstViewModel, secondViewModel, "Should return different instances for different keys")
        assertEquals(2, storage.viewModels.size)
        assertTrue(storage.viewModels.containsKey("key1"))
        assertTrue(storage.viewModels.containsKey("key2"))
    }

    @Test
    fun `clear # closes all viewmodels and clears the storage`() {
        val storage = ViewModelsStorage()
        val viewModel1 = storage.get("key1") { TestViewModel() }
        val viewModel2 = storage.get("key2") { TestViewModel() }
        storage.clear()
        assertTrue(viewModel1.isClosed, "ViewModel1 should be closed")
        assertTrue(viewModel2.isClosed, "ViewModel2 should be closed")
        assertEquals(0, storage.viewModels.size, "ViewModels storage should be empty after clear")
    }

    private class TestViewModel : TiamatViewModel() {
        var isClosed = false

        override fun onClosed() {
            isClosed = true
        }
    }
}