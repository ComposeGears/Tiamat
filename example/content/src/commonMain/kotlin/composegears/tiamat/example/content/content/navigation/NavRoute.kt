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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.*
import composegears.tiamat.example.ui.core.*

@OptIn(TiamatExperimentalApi::class)
val NavRoute by navDestination(ScreenInfo()) {
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
                        element(NavRouteScreen1)
                        element(NavRouteScreen2)
                        element(NavRouteScreen3)
                    }
                }
            )
            AppButton(
                "Route: 1->2->3 (by name)",
                modifier = Modifier.widthIn(min = 400.dp),
                onClick = {
                    nc.route {
                        destination("NavRouteScreen1")
                        destination("NavRouteScreen2")
                        destination("NavRouteScreen3")
                    }
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
                navController = nc,
                destinations = arrayOf(
                    NavRouteStub,
                    NavRouteScreen1,
                    NavRouteScreen2,
                    NavRouteScreen3,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val NavRouteStub by navDestination {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Stub", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

private val NavRouteScreen1 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
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

private val NavRouteScreen2 by navDestination {
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
                    onClick = { nc.navigate(NavRouteScreen3) }
                )
            }
        }
    }
}

private val NavRouteScreen3 by navDestination<String> {
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