@file:Suppress("MatchingDeclarationName")

package composegears.tiamat.example

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import composegears.tiamat.example.ui.core.SimpleScreen
import composegears.tiamat.example.ui.core.Spacer
import composegears.tiamat.example.ui.core.TextButton
import composegears.tiamat.example.ui.core.webPathExtension

@OptIn(TiamatExperimentalApi::class)
val RouteAndDeepLinks by navDestination<Unit>(webPathExtension()) {
    SimpleScreen("Data passing: Result") {
        Row {
            val routeNC = rememberNavController(
                startDestination = SimpleForwardBackRoot,
                destinations = arrayOf(
                    SimpleForwardBackRoot,
                    SimpleForwardBackRootScreen1,
                    SimpleForwardBackRootScreen2,
                    SimpleForwardBackRootScreen3,
                    SimpleTabsRoot
                )
            )
            // actions
            Column(Modifier.width(200.dp).padding(8.dp).fillMaxHeight()) {
                TextButton(
                    text = "Route: open SimpleBackFroward",
                    onClick = {
                        routeNC.route(Route.build(SimpleForwardBackRoot))
                    }
                )
                Spacer()
                TextButton(
                    text = "Route: open tabs",
                    onClick = {
                        routeNC.route(Route.build(SimpleTabsRoot))
                    }
                )
                Spacer()
                TextButton(
                    text = "Auto Route: open tabs -> tab3 -> screen1 -> screen2 -> screen3",
                    onClick = {
                        routeNC.route(Route.build(SimpleTabsRoot, Tab3, TabScreen1, TabScreen2, TabScreen3))
                    }
                )
                Spacer()
                TextButton(
                    text = "NonAuto Route: open tabs -> tab3 -> tab1 -> tab2 -> tab3",
                    onClick = {
                        routeNC.route(Route.build(autoPath = false, throwOnFail = true) {
                            route(SimpleTabsRoot)
                            selectNavController()
                            route(Tab3)
                            selectNavController()
                            route(TabScreen1)
                            route(TabScreen2)
                            route(TabScreen3)
                        })
                    }
                )
                Spacer()
                TextButton(
                    text = "NonAuto Key-based Route: open tabs -> tab3 -> tab1 -> tab2 -> tab3",
                    onClick = {
                        routeNC.route(Route.build(autoPath = false, throwOnFail = true) {
                            route("SimpleTabsRoot")
                            selectNavController("tabs")
                            route("Tab3")
                            selectNavController("Tab3NavController")
                            route("TabScreen1")
                            route("TabScreen2")
                            route("TabScreen3")
                        })
                    }
                )
            }
            // content
            Navigation(
                navController = routeNC,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .border(4.dp, MaterialTheme.colorScheme.onSurface)
                    .padding(4.dp)
            )
        }
    }
}