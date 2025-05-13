package com.composegears.tiamat.navigation

import kotlin.test.*

class ViewModelsStorageTests {

    private class TestViewModel : TiamatViewModel() {
        var isClosed = false

        override fun onClosed() {
            isClosed = true
        }
    }

    @Test
    fun `ViewModelsStorage | get | creates and returns new viewmodel`() {
        val storage = ViewModelsStorage()
        val viewModel = storage["testKey", { TestViewModel() }]
        assertNotNull(viewModel)
        assertEquals(1, storage.viewModels.size)
        assertTrue(storage.viewModels.containsKey("testKey"))
    }

    @Test
    fun `ViewModelsStorage | get | returns existing viewmodel for same key`() {
        val storage = ViewModelsStorage()
        val firstViewModel = storage["testKey", { TestViewModel() }]
        val secondViewModel = storage["testKey", { TestViewModel() }]
        assertNotNull(firstViewModel)
        assertNotNull(secondViewModel)
        assertSame(firstViewModel, secondViewModel, "Should return the same instance for the same key")
        assertEquals(1, storage.viewModels.size)
    }

    @Test
    fun `ViewModelsStorage | get | creates different viewmodels for different keys`() {
        val storage = ViewModelsStorage()
        val firstViewModel = storage["key1", { TestViewModel() }]
        val secondViewModel = storage["key2", { TestViewModel() }]
        assertNotNull(firstViewModel)
        assertNotNull(secondViewModel)
        assertNotSame(firstViewModel, secondViewModel, "Should return different instances for different keys")
        assertEquals(2, storage.viewModels.size)
        assertTrue(storage.viewModels.containsKey("key1"))
        assertTrue(storage.viewModels.containsKey("key2"))
    }

    @Test
    fun `ViewModelsStorage | clear | closes all viewmodels and clears the storage`() {
        val storage = ViewModelsStorage()
        val viewModel1 = storage["key1", { TestViewModel() }]
        val viewModel2 = storage["key2", { TestViewModel() }]
        storage.clear()
        assertTrue(viewModel1.isClosed, "ViewModel1 should be closed")
        assertTrue(viewModel2.isClosed, "ViewModel2 should be closed")
        assertEquals(0, storage.viewModels.size, "ViewModels storage should be empty after clear")
    }
}