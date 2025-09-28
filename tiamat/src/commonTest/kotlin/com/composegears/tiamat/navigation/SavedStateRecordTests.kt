package com.composegears.tiamat.navigation

import kotlin.test.*

class SavedStateRecordTests {

    @Test
    fun `record initializes with initial value when key absent`() {
        val savedState = MutableSavedState()
        val record = savedState.recordOf("intKey", 42)
        assertEquals(42, record.value)
        assertEquals(42, savedState["intKey"])
        assertEquals(42, record.asStateFlow().value)
    }

    @Test
    fun `record uses existing savedState value when present`() {
        val savedState = MutableSavedState("intKey" to 100)
        val record = savedState.recordOf("intKey", 42)
        assertEquals(100, record.value)
        assertEquals(100, record.asStateFlow().value)
    }

    @Test
    fun `setting value updates savedState and stateflow`() {
        val savedState = MutableSavedState()
        val record = savedState.recordOf("key", "initial")
        record.value = "updated"
        assertEquals("updated", record.value)
        assertEquals("updated", savedState["key"])
        assertEquals("updated", record.asStateFlow().value)
    }

    @Test
    fun `nullable values supported and flow updates`() {
        val savedState = MutableSavedState()
        val record = savedState.recordOf<String?>("nullableKey", null)
        assertNull(record.value)
        assertNull(savedState["nullableKey"])
        assertNull(record.asStateFlow().value)
        record.value = "hello"
        assertEquals("hello", record.value)
        assertEquals("hello", savedState["nullableKey"])
        assertEquals("hello", record.asStateFlow().value)
        record.value = null
        assertNull(record.value)
        assertNull(savedState["nullableKey"])
        assertNull(record.asStateFlow().value)
    }
}