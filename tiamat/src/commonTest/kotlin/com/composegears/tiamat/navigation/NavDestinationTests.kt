package com.composegears.tiamat.navigation

import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import kotlin.test.Test
import kotlin.test.assertEquals

class NavDestinationTests {

    @Test
    fun `companion # toNavEntry # convert to NavEntry`() {
        val intNavEntry by navDestination<Int> {}
        val intEntry = intNavEntry.toNavEntry(
            navArgs = 2,
            freeArgs = true,
            navResult = "1"
        )
        assertEquals(2, intEntry.getNavArgs())
        assertEquals(true, intEntry.getFreeArgs())
        assertEquals("1", intEntry.getNavResult())

        val stringNavEntry by navDestination<String> {}
        val stringEntry = stringNavEntry.toNavEntry(
            navArgs = "hello",
            freeArgs = false,
            navResult = 42
        )
        assertEquals("hello", stringEntry.getNavArgs())
        assertEquals(false, stringEntry.getFreeArgs())
        assertEquals(42, stringEntry.getNavResult())

        val booleanNavEntry by navDestination<Boolean> {}
        val booleanEntry = booleanNavEntry.toNavEntry(
            navArgs = true,
            freeArgs = "extra data",
            navResult = 3.14
        )
        assertEquals(true, booleanEntry.getNavArgs())
        assertEquals("extra data", booleanEntry.getFreeArgs())
        assertEquals(3.14, booleanEntry.getNavResult())
    }
}