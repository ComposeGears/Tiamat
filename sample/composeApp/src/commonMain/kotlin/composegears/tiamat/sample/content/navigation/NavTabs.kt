package composegears.tiamat.sample.content.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavDestination
import composegears.tiamat.sample.ui.*

val NavTabs by navDestination(ScreenInfo()) {
    Screen("Tabs navigation") {
        Column(Modifier.fillMaxSize()) {
            val tabs = remember {
                arrayOf<NavDestination<*>>(
                    NavTab1,
                    NavTab2,
                    NavTab3,
                )
            }
            val nc = rememberNavController(
                key = "Tabs nav controller",
                startDestination = NavTab1,
            )
            val activeTab by nc.currentNavDestinationAsState()
            Navigation(
                navController = nc,
                destinations = tabs,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                tabs.onEach {
                    // pop (or open) tab
                    AppButton(
                        text = it.name,
                        enabled = activeTab != it,
                        shape = RectangleShape,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { nc.popToTop(it) }
                    )
                }
            }
        }
    }
}

private val NavTab1 by navDestination {
    TabContent("NavTab1")
}

private val NavTab2 by navDestination {
    TabContent("NavTab2")
}

private val NavTab3 by navDestination {
    TabContent("NavTab3")
}

@Composable
fun TabContent(tabName: String) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(tabName, style = MaterialTheme.typography.headlineMedium)
        val nc = rememberNavController(
            key = "$tabName content",
            startDestination = NavTabsSubTabScreen1,
        )
        Navigation(
            navController = nc,
            destinations = arrayOf(
                NavTabsSubTabScreen1,
                NavTabsSubTabScreen2,
                NavTabsSubTabScreen3,
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

private val NavTabsSubTabScreen1 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(NavTabsSubTabScreen2) }
            )
        }
    }
}

private val NavTabsSubTabScreen2 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Row {
                AppButton(
                    "Back",
                    startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    onClick = { nc.back() }
                )
                HSpacer()
                AppButton(
                    "Next",
                    endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    onClick = { nc.navigate(NavTabsSubTabScreen3) }
                )
            }
        }
    }
}

private val NavTabsSubTabScreen3 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 3", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

@Preview
@Composable
private fun NavTabsPreview() = AppTheme {
    TiamatPreview(destination = NavTabs)
}
