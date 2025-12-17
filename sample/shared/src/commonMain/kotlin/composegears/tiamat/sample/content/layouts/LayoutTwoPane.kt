package composegears.tiamat.sample.content.layouts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.ui.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
val LayoutTwoPane by navDestination(ScreenInfo()) {
    Screen("Two pane") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "TwoPane nav controller",
                startDestination = LayoutTwoPaneList,
            )
            NavigationScene(
                navController = nc,
                destinations = arrayOf(
                    LayoutTwoPaneList,
                    LayoutTwoPaneDetails1,
                    LayoutTwoPaneDetails2,
                )
            ) {
                LookaheadScope {
                    Row {
                        val navStack by nc.navStackAsState()
                        val commonEntry by remember(navStack) {
                            derivedStateOf {
                                navStack.firstOrNull()
                            }
                        }
                        val extraEntry by remember(navStack) {
                            derivedStateOf {
                                navStack.lastOrNull()?.takeIf { navStack.size > 1 }
                            }
                        }
                        if (commonEntry != null) Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .animateBounds(this@LookaheadScope)
                        ) {
                            EntryContent(commonEntry)
                        }
                        AnimatedContent(
                            targetState = extraEntry,
                            contentKey = { it?.contentKey() },
                            modifier = Modifier
                                .composed {
                                    if (extraEntry != null) weight(1f)
                                    else this
                                }
                                .fillMaxHeight()
                                .animateBounds(this@LookaheadScope),
                            transitionSpec = { navigationFadeInOut() }
                        ) {
                            EntryContent(it)
                        }
                    }
                }
            }
        }
    }
}

private val LayoutTwoPaneList by navDestination {
    val nc = navController()
    val items = remember {
        (0..10).map { "Item $it" }
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
        val timer by produceState(0) {
            while (true) {
                delay(1000)
                value++
            }
        }
        Text("Timer: $timer", style = MaterialTheme.typography.bodyMedium)
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(items) { item ->
                AppButton(
                    item,
                    modifier = Modifier.widthIn(min = 200.dp),
                    endIcon = Icons.KeyboardArrowRight,
                    onClick = { nc.navigate(LayoutTwoPaneDetails1, item) }
                )
            }
        }
    }
}

private val LayoutTwoPaneDetails1 by navDestination<String> {
    val nc = navController()
    val args = navArgs()
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            Text("Selected item: $args", style = MaterialTheme.typography.bodyMedium)
            val timer by produceState(0) {
                while (true) {
                    delay(1000)
                    value++
                }
            }
            Text("Timer: $timer", style = MaterialTheme.typography.bodyMedium)
            Row {
                AppButton(
                    "Back",
                    startIcon = Icons.KeyboardArrowLeft,
                    onClick = { nc.back() }
                )
                HSpacer()
                AppButton(
                    "Next",
                    endIcon = Icons.KeyboardArrowRight,
                    onClick = { nc.navigate(LayoutTwoPaneDetails2, args) }
                )
            }
        }
    }
}

private val LayoutTwoPaneDetails2 by navDestination<String> {
    val nc = navController()
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 3", style = MaterialTheme.typography.headlineMedium)
            Text("Selected item: ${navArgs()}", style = MaterialTheme.typography.bodyMedium)
            val timer by produceState(0) {
                while (true) {
                    delay(1000)
                    value++
                }
            }
            Text("Timer: $timer", style = MaterialTheme.typography.bodyMedium)
            AppButton(
                "Back",
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

@Preview
@Composable
private fun LayoutTwoPanePreview() = AppTheme {
    TiamatPreview(destination = LayoutTwoPane)
}
