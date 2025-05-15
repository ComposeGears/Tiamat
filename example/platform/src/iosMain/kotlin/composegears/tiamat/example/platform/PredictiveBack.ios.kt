package composegears.tiamat.example.platform

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavController
import composegears.tiamat.example.ui.core.*
import kotlin.coroutines.cancellation.CancellationException


@OptIn(TiamatExperimentalApi::class)
val PredictiveBack by navDestination<Unit>(ScreenInfo()) {
    Screen("PredictiveBack") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "PredictiveBack nav controller",
                startDestination = PredictiveBackScreen1,
                configuration = {
                    route {
                        element(PredictiveBackScreen1)
                        element(PredictiveBackScreen2)
                        element(PredictiveBackScreen3)
                    }
                }
            )
            PredictiveBackContainer(
                navController = nc,
                enabled = true,
                modifier = Modifier.fillMaxSize()
            ) {
                Navigation(
                    navController = nc,
                    destinations = arrayOf(
                        PredictiveBackScreen1,
                        PredictiveBackScreen2,
                        PredictiveBackScreen3,
                    ),
                    modifier = Modifier.fillMaxSize(),
                    contentTransformProvider = { navigationPlatformDefault(it) }
                )
            }
        }
    }
}

private val PredictiveBackScreen1 by navDestination<Unit> {
    val nc = navController()
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(PredictiveBackScreen2) }
            )
        }
    }
}

private val PredictiveBackScreen2 by navDestination<Unit> {
    val nc = navController()
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
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
                    onClick = { nc.navigate(PredictiveBackScreen3) }
                )
            }
        }
    }
}

private val PredictiveBackScreen3 by navDestination<Unit> {
    val nc = navController()
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
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

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal fun PredictiveBackContainer(
    navController: NavController,
    enabled: Boolean,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier) {
        content()
        val canGoBack by navController.canGoBackAsState()
        PredictiveBackHandler(canGoBack && enabled) { progress ->
            val controller = TransitionController()
            navController.back(
                transition = ContentTransform(
                    targetContentEnter = slideInHorizontally(
                        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
                        initialOffsetX = { -it / 3 }
                    ) + fadeIn(tween(100)),
                    initialContentExit = slideOutHorizontally(
                        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
                        targetOffsetX = { it }
                    ),
                    sizeTransform = null,
                    targetContentZIndex = -1f
                ),
                transitionController = controller,
            )
            try {
                progress.collect { controller.update(it.progress) }
                controller.finish()
            } catch (_: CancellationException) {
                controller.cancel()
            }
        }
    }
}