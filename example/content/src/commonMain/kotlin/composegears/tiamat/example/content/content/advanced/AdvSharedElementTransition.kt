package composegears.tiamat.example.content.content.advanced

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import composegears.tiamat.example.ui.core.*

@OptIn(ExperimentalSharedTransitionApi::class)
private val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope> { error("No scope provided") }

@OptIn(ExperimentalSharedTransitionApi::class)
val AdvSharedElementTransition by navDestination<Unit>(ScreenInfo()) {
    Screen("SharedElementTransition") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "SharedElementTransition nav controller",
                startDestination = AdvSharedElementTransitionScreen1,
                destinations = arrayOf(
                    AdvSharedElementTransitionScreen1,
                    AdvSharedElementTransitionScreen2,
                    AdvSharedElementTransitionScreen3,
                )
            )
            SharedTransitionLayout {
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this
                ) {
                    Navigation(
                        navController = nc,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentTransformProvider = { navigationSlideInOut(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private val AdvSharedElementTransitionScreen1 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            with(LocalSharedTransitionScope.current) {
                Text(
                    "This is SHARED element on screen 1",
                    modifier = Modifier
                        .sharedElement(
                            state = rememberSharedContentState("element"),
                            animatedVisibilityScope = this@navDestination,
                        )
                        .renderInSharedTransitionScopeOverlay()
                        .animateEnterExit()
                        .background(Color.Green.copy(alpha = 0.3f))
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(AdvSharedElementTransitionScreen2) }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private val AdvSharedElementTransitionScreen2 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            with(LocalSharedTransitionScope.current) {
                Text(
                    "This is SHARED element on screen 2",
                    modifier = Modifier
                        .sharedElement(
                            state = rememberSharedContentState("element"),
                            animatedVisibilityScope = this@navDestination,
                        )
                        .renderInSharedTransitionScopeOverlay()
                        .animateEnterExit()
                        .background(Color.Red.copy(alpha = 0.3f))
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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
                    onClick = { nc.navigate(AdvSharedElementTransitionScreen3) }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private val AdvSharedElementTransitionScreen3 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 3", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            with(LocalSharedTransitionScope.current) {
                Text(
                    "This is SHARED element on screen 3",
                    modifier = Modifier
                        .sharedElement(
                            state = rememberSharedContentState("element"),
                            animatedVisibilityScope = this@navDestination,
                        )
                        .renderInSharedTransitionScopeOverlay()
                        .animateEnterExit()
                        .background(Color.Blue.copy(alpha = 0.3f))
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            VSpacer()
            AppButton(
                "Back",
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}