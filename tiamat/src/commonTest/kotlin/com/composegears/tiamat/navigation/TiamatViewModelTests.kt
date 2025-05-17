package com.composegears.tiamat.navigation

import kotlinx.coroutines.isActive
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TiamatViewModelTests {

    @Test
    fun `close # coroutine scope is closed`() {
        val model = TmpViewModel()
        assertTrue(model.scope.isActive)
        model.close()
        assertFalse(model.scope.isActive)
    }

    @Test
    fun `close # calls onClosed when closed`() {
        val model = TmpViewModel()
        model.close()
        assertTrue(model.isClosed)
    }

    class TmpViewModel : TiamatViewModel() {
        val scope = viewModelScope
        var isClosed = false

        override fun onClosed() {
            isClosed = true
            super.onClosed()
        }
    }
}