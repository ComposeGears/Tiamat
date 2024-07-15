package composegears.tiamat.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import com.composegears.tiamat.navigationFadeInOut
import composegears.tiamat.example.platform.DeeplinkScreen

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope> { error("-") }

class MainActivity : ComponentActivity() {

    private val deepLinkController = DeepLinkController()

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkController.onIntent(intent)
        setContent {
            App { rootNavController, content ->
                // pass deeplink deeper and handle inside screen
                val deeplink = deepLinkController.deeplink
                // using disposable effect as it runs faster then LaunchedEffect
                DisposableEffect(deeplink) {
                    if (deeplink != null) {
                        rootNavController.editBackStack {
                            clear()
                            add(MainScreen)
                            add(PlatformExamplesScreen)
                        }
                        rootNavController.replace(
                            dest = DeeplinkScreen,
                            freeArgs = deeplink,
                            // we only animate root content switch
                            // all nested items should use navigationNone() transition to prevent `blink`
                            transition = navigationFadeInOut()
                        )
                        deepLinkController.clearDeepLink()
                    }
                    onDispose { }
                }
                SharedTransitionLayout {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this
                    ) {
                        content()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkController.onIntent(intent)
    }
}