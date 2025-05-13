package composegears.tiamat.example.platform

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composegears.tiamat.navigation.NavController

@Composable
internal actual fun PredictiveBackContainer(
    navController: NavController,
    enabled: Boolean,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier) {
        content()
        TODO()
        /*if (enabled) Box(
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
        )*/
    }
}