package composegears.tiamat.example.platform

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseInSine
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import com.composegears.tiamat.compose.TransitionController
import com.composegears.tiamat.compose.back
import com.composegears.tiamat.compose.canGoBackAsState
import com.composegears.tiamat.navigation.NavController
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Suppress("SwallowedException")
internal actual fun PredictiveBackContainer(
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
            } catch (e: CancellationException) {
                controller.cancel()
            }
        }
    }
}