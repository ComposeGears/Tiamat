package composegears.tiamat.example.content.content.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import composegears.tiamat.example.ui.core.*

@OptIn(TiamatExperimentalApi::class)
val NavRoute by navDestination<Unit>(ScreenInfo()) {
    Screen("Routing") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val nc = rememberNavController(
                key = "Route nav controller",
                startDestination = null,
                destinations = arrayOf(
                    NavRouteStub,
                    NavRouteScreen1,
                    NavRouteScreen2,
                    NavRouteScreen3,
                )
            )
            VSpacer()
            Text("Here some simple examples of Route-api")
            Text("There is more complex option available")
            VSpacer()
            AppButton(
                "Route: 1->2->3 direct",
                modifier = Modifier.widthIn(min = 400.dp),
                onClick = {
                    nc.route(Route.build(NavRouteScreen1, NavRouteScreen2, NavRouteScreen3))
                }
            )
            AppButton(
                "Route: 1->2->3 (by name)",
                modifier = Modifier.widthIn(min = 400.dp),
                onClick = {
                    nc.route(
                        Route.build {
                            route("NavRouteScreen1")
                            route("NavRouteScreen2")
                            route("NavRouteScreen3")
                        }
                    )
                }
            )
            AppButton(
                "Route: 1->2->3 (mixed)",
                modifier = Modifier.widthIn(min = 400.dp),
                onClick = {
                    nc.route(
                        Route.build {
                            // direct name
                            route(NavRouteScreen1)
                            // auto-search by name
                            route("NavRouteScreen2")
                            // manual resolve from nav controller
                            route { nc -> nc.findDestination { it.name.contains("3") }?.toNavEntry() }
                        }
                    )
                }
            )
            AppButton(
                "Reset to stub",
                modifier = Modifier.widthIn(min = 400.dp),
                onClick = {
                    nc.navigate(NavRouteStub)
                    nc.editBackStack { clear() }
                }
            )

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

private val NavRouteStub by navDestination<Unit> {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Stub", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

private val NavRouteScreen1 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(NavRouteScreen2) }
            )
        }
    }
}

private val NavRouteScreen2 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    onClick = { nc.navigate(NavRouteScreen3) }
                )
            }
        }
    }
}

private val NavRouteScreen3 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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