package composegears.tiamat.sample.content.other

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.ui.*

val OtherNavStackAlteration by navDestination(ScreenInfo()) {
    Screen("Nav stack alteration") {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val nc = rememberNavController(
                key = "NS alteration nav controller",
                startDestination = OtherNavStackAlterationScreenA,
            )
            val navStack by nc.navStackAsState()
            val canNavigateBack by nc.canNavigateBackAsState()
            VSpacer()
            Text(
                text = "Here some simple examples of nav-stack editing",
                textAlign = TextAlign.Center
            )
            Text(
                text = "Current stack is: " +
                    navStack.joinToString {
                        it.destination.name.substringAfter("Screen")
                    },
                textAlign = TextAlign.Center
            )
            VSpacer()
            Row {
                Column(Modifier.weight(1f, false).widthIn(200.dp).width(IntrinsicSize.Min)) {
                    AppButton(
                        "Add A pre-last",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            nc.editNavStack { old ->
                                val current = old.last()
                                old - current + OtherNavStackAlterationScreenA.toNavEntry() + current
                            }
                        }
                    )
                    AppButton(
                        "Add B pre-last",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            nc.editNavStack { old ->
                                val current = old.last()
                                old - current + OtherNavStackAlterationScreenB.toNavEntry() + current
                            }
                        }
                    )
                    AppButton(
                        "Add C pre-last",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            nc.editNavStack { old ->
                                val current = old.last()
                                old - current + OtherNavStackAlterationScreenC.toNavEntry() + current
                            }
                        }
                    )
                }
                HSpacer()
                Column(Modifier.weight(1f, false).widthIn(200.dp).width(IntrinsicSize.Min)) {
                    AppButton(
                        "Clear",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canNavigateBack,
                        onClick = { nc.editNavStack { old -> old.takeLast(1) } }
                    )
                    AppButton(
                        "Remove Last (nav back)",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canNavigateBack,
                        onClick = {
                            // remove last means we intend to go back
                            // so we use transition type backward
                            nc.editNavStack(transitionType = NavController.TransitionType.Backward) { old ->
                                old.dropLast(1)
                            }
                        }
                    )
                    AppButton(
                        "Remove Pre-last",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canNavigateBack,
                        onClick = {
                            nc.editNavStack { old ->
                                val current = old.last()
                                old.dropLast(2) + current
                            }
                        }
                    )
                }
            }
            VSpacer()
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    OtherNavStackAlterationScreenA,
                    OtherNavStackAlterationScreenB,
                    OtherNavStackAlterationScreenC,
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

private val OtherNavStackAlterationScreenA by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen A", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                enabled = nc.canNavigateBackAsState().value,
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

private val OtherNavStackAlterationScreenB by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen B", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                enabled = nc.canNavigateBackAsState().value,
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

private val OtherNavStackAlterationScreenC by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen C", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                enabled = nc.canNavigateBackAsState().value,
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

@Preview
@Composable
private fun OtherNavStackAlterationPreview() = AppTheme {
    TiamatPreview(destination = OtherNavStackAlteration)
}