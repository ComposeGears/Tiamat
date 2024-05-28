package composegears.tiamat.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import com.composegears.tiamat.navigationFadeInOut
import composegears.tiamat.example.platform.DeeplinkScreen

class MainActivity : ComponentActivity() {

    private val deepLinkController = DeepLinkController()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
                content()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkController.onIntent(intent)
    }
}