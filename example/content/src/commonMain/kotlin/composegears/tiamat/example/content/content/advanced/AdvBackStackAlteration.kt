package composegears.tiamat.example.content.content.advanced

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import composegears.tiamat.example.ui.core.*

val AdvBackStackAlteration by navDestination<Unit>(ScreenInfo()) {
    Screen("Back stack alteration") {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val nc = rememberNavController(
                key = "BS alteration nav controller",
                startDestination = AdvBackStackAlterationScreenA,
            )
            val currentDestination by nc.currentNavDestinationAsState()
            val backStack by nc.currentBackStackFlow.collectAsState()
            VSpacer()
            Text(
                text = "Here some simple examples of backStack editing",
                textAlign = TextAlign.Center
            )
            Text(
                text = "Current stack is: " +
                    backStack.joinToString(postfix = if (backStack.isEmpty()) "" else " -> ") {
                        it.destination.name.substringAfter("Screen")
                    } +
                    "${currentDestination?.name?.substringAfter("Screen")} (current)",
                textAlign = TextAlign.Center
            )
            VSpacer()
            Row {
                Column(Modifier.weight(1f, false).widthIn(200.dp).width(IntrinsicSize.Min)) {
                    AppButton(
                        "Append ScreenA",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            nc.editBackStack { add(AdvBackStackAlterationScreenA) }
                        }
                    )
                    AppButton(
                        "Append ScreenB",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            nc.editBackStack { add(AdvBackStackAlterationScreenB) }
                        }
                    )
                    AppButton(
                        "Append ScreenC",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            nc.editBackStack { add(AdvBackStackAlterationScreenC) }
                        }
                    )
                }
                HSpacer()
                Column(Modifier.weight(1f, false).widthIn(200.dp).width(IntrinsicSize.Min)) {
                    AppButton(
                        "Clear",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = backStack.isNotEmpty(),
                        onClick = { nc.editBackStack { clear() } }
                    )
                    AppButton(
                        "Remove Last",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = backStack.isNotEmpty(),
                        onClick = { nc.editBackStack { removeLast() } }
                    )
                    AppButton(
                        "Remove First",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = backStack.isNotEmpty(),
                        onClick = { nc.editBackStack { removeAt(0) } }
                    )
                }
            }
            VSpacer()
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    AdvBackStackAlterationScreenA,
                    AdvBackStackAlterationScreenB,
                    AdvBackStackAlterationScreenC,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

// We are using nc.hasBackEntriesAsState().value instead nc.hasBackEntries() due to changes in backStack

private val AdvBackStackAlterationScreenA by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen A", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                enabled = nc.hasBackEntriesAsState().value,
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

private val AdvBackStackAlterationScreenB by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen B", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                enabled = nc.hasBackEntriesAsState().value,
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

private val AdvBackStackAlterationScreenC by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen C", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                enabled = nc.hasBackEntriesAsState().value,
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}