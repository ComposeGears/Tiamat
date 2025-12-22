package composegears.tiamat.sample.content.navigation.actions

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.ui.*

@OptIn(TiamatExperimentalApi::class)
val NavActionRoute by navDestination(ScreenInfo()) {
    Screen("Routing") {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val nc = rememberNavController(
                key = "Route nav controller",
                startDestination = null,
            )
            VSpacer()
            Text(
                text = "Here some simple examples of Route-api",
                textAlign = TextAlign.Center
            )
            Text(
                text = "There is more complex option available",
                textAlign = TextAlign.Center
            )
            VSpacer()

            AppButton(
                "Route: 1->2->3 direct",
                modifier = Modifier.widthIn(min = 400.dp),
                onClick = {
                    nc.route {
                        element(NavActionRouteScreen1)
                        element(NavActionRouteScreen2)
                        element(NavActionRouteScreen3)
                    }
                }
            )
            AppButton(
                "Route: 1->2->3 (by name)",
                modifier = Modifier.widthIn(min = 400.dp),
                onClick = {
                    nc.route {
                        destination("NavActionRouteScreen1")
                        destination("NavActionRouteScreen2")
                        destination("NavActionRouteScreen3")
                    }
                }
            )
            AppButton(
                "Reset to stub",
                modifier = Modifier.widthIn(min = 400.dp),
                onClick = {
                    nc.editNavStack { _ -> listOf(NavActionRouteStub.toNavEntry()) }
                }
            )

            VSpacer()
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    NavActionRouteStub,
                    NavActionRouteScreen1,
                    NavActionRouteScreen2,
                    NavActionRouteScreen3,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val NavActionRouteStub by navDestination {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Stub", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

private val NavActionRouteScreen1 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.KeyboardArrowRight,
                onClick = { nc.navigate(NavActionRouteScreen2) }
            )
        }
    }
}

private val NavActionRouteScreen2 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
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
                    onClick = { nc.navigate(NavActionRouteScreen3) }
                )
            }
        }
    }
}

private val NavActionRouteScreen3 by navDestination<String> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 3", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
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
private fun NavActionRoutePreview() = AppTheme {
    TiamatPreview(destination = NavActionRoute)
}
