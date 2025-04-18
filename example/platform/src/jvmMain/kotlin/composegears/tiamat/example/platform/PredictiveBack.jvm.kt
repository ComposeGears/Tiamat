package composegears.tiamat.example.platform

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.NavController
import com.composegears.tiamat.TransitionController

@Composable
internal actual fun PredictiveBackContainer(
    navController: NavController,
    enabled: Boolean,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier) {
        content()
        if (enabled) Box(
            Modifier
                .fillMaxHeight()
                .width(64.dp)
                .align(Alignment.TopStart)
                .pointerInput("PredictiveBack") {
                    var controller: TransitionController? = null
                    var start = 0f
                    var slop = 0f
                    var dist = 0f
                    detectHorizontalDragGestures(
                        onDragStart = {
                            slop = 0f
                            dist = 0f
                            start = it.x
                        },
                        onDragEnd = {
                            if (slop > 0) controller?.finish()
                            else controller?.cancel()
                            controller = null
                        },
                        onDragCancel = {
                            controller?.cancel()
                            controller = null
                        },
                        onHorizontalDrag = { _, v ->
                            slop = v
                            dist += v
                            if (controller == null && navController.canGoBack && v > 0) {
                                controller = TransitionController()
                                navController.back(
                                    transition = ContentTransform(
                                        targetContentEnter = slideInHorizontally(tween(easing = LinearEasing)) { -it },
                                        initialContentExit = slideOutHorizontally(tween(easing = LinearEasing)) { it },
                                        sizeTransform = null
                                    ),
                                    transitionController = controller
                                )
                            }
                            val pdX = dist / (constraints.maxWidth - start)
                            controller?.update(pdX.coerceIn(0f, 1f))
                        }
                    )
                }
        )
    }
}