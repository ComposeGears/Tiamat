package composegears.tiamat.sample

import androidx.compose.ui.window.ComposeUIViewController
import composegears.tiamat.sample.platform.PredictiveBack
import composegears.tiamat.sample.ui.AppFeature

fun MainViewController() = ComposeUIViewController {
    App(
        platformFeatures = PlatformFeatures(
            "iOS",
            listOf(
                AppFeature(
                    name = "Predictive back",
                    description = "iOS predictive back",
                    destination = PredictiveBack
                )
            )
        )
    )
}
