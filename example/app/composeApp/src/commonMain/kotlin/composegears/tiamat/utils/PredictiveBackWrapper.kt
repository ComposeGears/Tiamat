package composegears.tiamat.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import com.composegears.tiamat.NavController
import com.composegears.tiamat.NavEntry
import com.composegears.tiamat.navigationNone
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalTransitionApi::class)
fun PredictiveBackWrapper(
    navController: NavController,
    config: PredictiveContentWrapperConfig = Android_PCWC,
//    config: PredictiveContentWrapperConfig = iOS_PCWC,
    content: @Composable () -> Unit
) {
    BoxWithConstraints {
        content()
        val density = LocalDensity.current
        val size = with(density) { Offset(maxWidth.toPx(), maxHeight.toPx()) }
        val minCommitDist = with(density) { 56.dp.toPx() }
        var offset by remember { mutableStateOf(Offset(0f, 0f)) }
        var velocity by remember { mutableStateOf(Offset(0f, 0f)) }
        var startPosition by remember { mutableStateOf<Offset?>(null) }
        var destinationFrom by remember { mutableStateOf<NavEntry<*>?>(null) }
        Box(
            Modifier
                .height(maxHeight)
                .align(Alignment.CenterStart)
                .width(config.startSideSize)
                .background(Color.Red.copy(0.05f))
                .draggable2D(
                    // allow click under this - migrate to touch or put content inside??
                    enabled = navController.canGoBack || startPosition != null,
                    state = rememberDraggable2DState {
                        velocity = it
                        offset += it
                    },
                    onDragStarted = {
                        destinationFrom = navController.currentNavEntry
                        offset = Offset(0f, 0f)
                        velocity = Offset(0f, 0f)
                        startPosition = it
                    },
                    onDragStopped = {
                        startPosition = null
                        destinationFrom = null
                    },
                )
        )
        LaunchedEffect(startPosition) {
            val currentDestination = destinationFrom
            if (startPosition != null && currentDestination != null) {
                navController.setPendingBackTransition(config.contentTransform(density, DpSize(maxWidth, maxHeight)))
                navController.setPendingTransitionController {
                    while (startPosition != null) {
                        val progress = config.progressTransform((offset.x / size.x).coerceIn(0f, 0.9999f))
                        snapToFraction(progress)
                        withFrameNanos { }
                    }
                    val progress = config.progressTransform((offset.x / size.x).coerceIn(0f, 0.9999f))
                    if (offset.x >= minCommitDist && velocity.x >= 0) {
                        val duration = (config.animDuration * (1f - progress)).roundToInt().coerceAtLeast(50)
                        animateToTargetState(tween(duration))
                    } else {
                        val duration = (config.animDuration * progress).roundToInt().coerceAtLeast(50)
                        animateToCurrentState(tween(duration))
                        navController.navigate(currentDestination, transition = navigationNone())
                    }
                }
                navController.back()
            }
        }
    }
}

val iOS_PCWC = PredictiveContentWrapperConfig(
    startSideSize = 56.dp,
    animDuration = 300,
    progressTransform = { it },
    contentTransform = { _, _ ->
        ContentTransform(
            targetContentEnter = slideIn(tween(easing = LinearEasing)) { IntOffset(-it.width, 0) },
            initialContentExit = slideOut(tween(easing = LinearEasing)) { IntOffset(it.width, 0) },
            sizeTransform = null
        )
    }
)

val Android_PCWC = PredictiveContentWrapperConfig(
    startSideSize = 56.dp,
    animDuration = 300,
    progressTransform = {
        // Linear 0..1 -> Cubic 0 .. 1/3
        0.33f * (1f - (1f - it).pow(3))
    },
    contentTransform = { density, size ->
        val startOffset = 56.dp
        val endOffset = 8.dp
        val scale = (size.width - startOffset - endOffset) / size.width
        ContentTransform(
            targetContentEnter = EnterTransition.None,
            initialContentExit =
            scaleOut(tween(100, easing = LinearEasing), scale)
                +
                slideOut(keyframes {
                    IntOffset(0, 0) at 0 using EaseInCubic
                    IntOffset(
                        x = with(density) { ((startOffset - endOffset) / 2).toPx().roundToInt() },
                        y = 0
                    ) at 100 using EaseInCubic
                    IntOffset(with(density) { size.width.toPx().roundToInt() }, 0) at 300 using LinearEasing
                }) {
                    IntOffset(it.width, 0)
                },
            targetContentZIndex = -1f,
            sizeTransform = null
        )
    }
)

class PredictiveContentWrapperConfig(
    val startSideSize: Dp,
    val animDuration: Int,
    val progressTransform: (Float) -> Float,
    val contentTransform: (Density, DpSize) -> ContentTransform,
) {
    init {
        if (animDuration < 0) error("animDuration should be >=0")
    }
}