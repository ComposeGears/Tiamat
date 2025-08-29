@file:OptIn(ExperimentalTestApi::class)

package com.composegears.tiamat.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertNotNull

// todo add more tests and rename class, this name is temporary
class RememberSaveable {

    companion object {
        val Screen1 by navDestination {
            val saveableProbe = rememberSaveable { "probe-" + Random.nextInt(999_999) }
            val navController = navController()
            Column {
                Text(
                    text = "TestPage $saveableProbe",
                    modifier = Modifier.testTag("Text")
                )
                Button(
                    onClick = { navController.navigate(Screen2) },
                    modifier = Modifier.testTag("S1Button")
                ) {
                    Text("Go")
                }
            }
        }
        val Screen2 by navDestination {
            val navController = navController()
            Button(
                onClick = { navController.back() },
                modifier = Modifier.testTag("S2Button")
            ) {
                Text("Back")
            }
        }
    }

    @Test
    fun `RememberSaveable # persist data during navigation`() = runComposeUiTest {
        setContent {
            val navController = rememberNavController(startDestination = Screen1)
            Navigation(navController = navController, destinations = arrayOf(Screen1, Screen2))
        }
        val rememberedText = onNodeWithTag("Text")
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            ?.firstOrNull()
            ?.text
        assertNotNull(rememberedText, " remembered text should not be null")
        onNodeWithTag("S1Button").performClick()
        onNodeWithTag("S2Button").assertExists()
        onNodeWithTag("S2Button").performClick()
        onNodeWithTag("Text").assertExists()
        onNodeWithTag("Text").assertTextEquals(rememberedText)
    }
}