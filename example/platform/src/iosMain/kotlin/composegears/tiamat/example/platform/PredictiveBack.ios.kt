package composegears.tiamat.example.platform

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import com.composegears.tiamat.compose.TransitionController
import com.composegears.tiamat.compose.back
import com.composegears.tiamat.compose.canGoBackAsState
import com.composegears.tiamat.navigation.NavController
import kotlin.coroutines.cancellation.CancellationException

@Composable
@OptIn(ExperimentalComposeUiApi::class)
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