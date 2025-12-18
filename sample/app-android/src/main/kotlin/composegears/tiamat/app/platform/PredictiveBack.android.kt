package composegears.tiamat.app.platform

import android.annotation.SuppressLint
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseInSine
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavController
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.ui.*
import kotlin.coroutines.cancellation.CancellationException

@OptIn(TiamatExperimentalApi::class)
val PredictiveBack by navDestination(ScreenInfo()) {
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

private val PredictiveBackScreen1 by navDestination {
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
                endIcon = Icons.KeyboardArrowRight,
                onClick = { nc.navigate(PredictiveBackScreen2) }
            )
        }
    }
}

private val PredictiveBackScreen2 by navDestination {
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
                    startIcon = Icons.KeyboardArrowLeft,
                    onClick = { nc.back() }
                )
                HSpacer()
                AppButton(
                    "Next",
                    endIcon = Icons.KeyboardArrowRight,
                    onClick = { nc.navigate(PredictiveBackScreen3) }
                )
            }
        }
    }
}

private val PredictiveBackScreen3 by navDestination {
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
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

@Composable
@Suppress("SwallowedException")
@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
internal fun PredictiveBackContainer(
    navController: NavController,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier) {
        content()
        val canNavigateBack by navController.canNavigateBackAsState()
        PredictiveBackHandler(canNavigateBack && enabled) { progress ->
            val controller = TransitionController()
            navController.back(
                transition = ContentTransform(
                    targetContentEnter = EnterTransition.None,
                    initialContentExit = slideOut(
                        targetOffset = { IntOffset(it.width, 0) },
                        animationSpec = keyframes {
                            IntOffset(0, 0) at 0
                            IntOffset(0, 0) at 150
                            IntOffset(constraints.maxWidth, 0) at 300
                        }
                    ) + scaleOut(
                        targetScale = 0.8f,
                        transformOrigin = TransformOrigin(0.8f, 0.5f),
                        animationSpec = keyframes {
                            1f at 0 using EaseInSine
                            0.85f at 150 using EaseInSine
                            0.85f at 300
                        }
                    ),
                    sizeTransform = null,
                    targetContentZIndex = -1f
                ),
                transitionController = controller,
            )
            try {
                progress.collect { controller.update(0.5f * it.progress) }
                controller.finish()
            } catch (_: CancellationException) {
                controller.cancel()
            }
        }
    }
}