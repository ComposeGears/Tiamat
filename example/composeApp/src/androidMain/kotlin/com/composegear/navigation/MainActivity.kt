package com.composegear.navigation

import App
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import com.composegears.tiamat.navigationFadeInOut
import content.MainScreen
import content.examples.PlatformExample

class MainActivity : ComponentActivity() {

    private val deepLinkController = DeepLinkController()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        deepLinkController.onIntent(intent)
        setContent {
            App { rootNavController ->
                // pass deeplink deeper and handle inside screen
                val deeplink = deepLinkController.deeplink
                // using disposable effect as it runs faster then LaunchedEffect
                DisposableEffect(deeplink) {
                    if (deeplink != null) {
                        rootNavController.editBackStack {
                            clear()
                            add(MainScreen)
                        }
                        rootNavController.replace(
                            dest = PlatformExample,
                            freeArgs = deeplink,
                            // we only animate root content switch
                            // all nested items should use navigationNone() transition to prevent `blink`
                            transition = navigationFadeInOut()
                        )
                        deepLinkController.clearDeepLink()
                    }
                    onDispose { }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.let(deepLinkController::onIntent)
    }
}