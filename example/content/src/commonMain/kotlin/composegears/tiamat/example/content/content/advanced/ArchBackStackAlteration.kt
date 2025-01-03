package composegears.tiamat.example.content.content.advanced

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberNavController
import composegears.tiamat.example.ui.core.*

val AdvBackStackAlteration by navDestination<Unit>(ScreenInfo()) {
    Screen("Back stack alteration") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val nc = rememberNavController(
                key = "BS alteration nav controller",
                startDestination = AdvBackStackAlterationScreenA,
                destinations = arrayOf(
                    AdvBackStackAlterationScreenA,
                    AdvBackStackAlterationScreenB,
                    AdvBackStackAlterationScreenC,
                )
            )
            var editsCount by remember { mutableIntStateOf(0) }
            val backStack = remember(nc.current, editsCount) {
                nc.getBackStack().joinToString(", ") {
                    it.destination.name.substringAfter("Screen")
                }
            }
            VSpacer()
            Text("Here some simple examples of backStack editing")
            Text(
                "Current stack is: " +
                    "${if (backStack.isNotBlank()) "$backStack ->" else ""} " +
                    "${nc.current?.name?.substringAfter("Screen")} (current)"
            )
            VSpacer()
            Row {
                Column {
                    AppButton(
                        "Append ScreenA",
                        modifier = Modifier.widthIn(min = 200.dp),
                        onClick = {
                            nc.editBackStack { add(AdvBackStackAlterationScreenA) }
                            editsCount++
                        }
                    )
                    AppButton(
                        "Append ScreenB",
                        modifier = Modifier.widthIn(min = 200.dp),
                        onClick = {
                            nc.editBackStack { add(AdvBackStackAlterationScreenB) }
                            editsCount++
                        }
                    )
                    AppButton(
                        "Append ScreenC",
                        modifier = Modifier.widthIn(min = 200.dp),
                        onClick = {
                            nc.editBackStack { add(AdvBackStackAlterationScreenC) }
                            editsCount++
                        }
                    )
                }
                HSpacer()
                Column {
                    AppButton(
                        "Clear",
                        modifier = Modifier.widthIn(min = 200.dp),
                        onClick = {
                            nc.editBackStack { clear() }
                            editsCount++
                        }
                    )
                    AppButton(
                        "Remove Last",
                        modifier = Modifier.widthIn(min = 200.dp),
                        onClick = {
                            nc.editBackStack { removeLast() }
                            editsCount++
                        }
                    )
                    AppButton(
                        "Remove First",
                        modifier = Modifier.widthIn(min = 200.dp),
                        onClick = {
                            nc.editBackStack { removeAt(0) }
                            editsCount++
                        }
                    )
                }
            }
            VSpacer()
            Navigation(
                nc,
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val AdvBackStackAlterationScreenA by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen A", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                enabled = nc.canGoBack,
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

private val AdvBackStackAlterationScreenB by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen B", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                enabled = nc.canGoBack,
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

private val AdvBackStackAlterationScreenC by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen C", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                enabled = nc.canGoBack,
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}