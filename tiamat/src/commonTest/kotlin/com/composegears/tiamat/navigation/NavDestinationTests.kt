package com.composegears.tiamat.navigation

import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import kotlin.test.Test
import kotlin.test.assertEquals

class NavDestinationTests {

    @Test
    fun `companion # toNavEntry # convert to NavEntry`() {
        val intNavEntry = AnyTestDestination<Int>()
        val intEntry = intNavEntry.toNavEntry(
            navArgs = 2,
            freeArgs = true,
            navResult = "1"
        )
        assertEquals(2, intEntry.navArgs)
        assertEquals(true, intEntry.freeArgs)
        assertEquals("1", intEntry.navResult)

        val stringNavEntry = AnyTestDestination<String>()
        val stringEntry = stringNavEntry.toNavEntry(
            navArgs = "hello",
            freeArgs = false,
            navResult = 42
        )
        assertEquals("hello", stringEntry.navArgs)
        assertEquals(false, stringEntry.freeArgs)
        assertEquals(42, stringEntry.navResult)

        val booleanNavEntry = AnyTestDestination<Boolean>()
        val booleanEntry = booleanNavEntry.toNavEntry(
            navArgs = true,
            freeArgs = "extra data",
            navResult = 3.14
        )
        assertEquals(true, booleanEntry.navArgs)
        assertEquals("extra data", booleanEntry.freeArgs)
        assertEquals(3.14, booleanEntry.navResult)
    }

    class AnyTestDestination<T> : NavDestination<T> {
        override val name: String = "AnyTestDestination"
    }
}