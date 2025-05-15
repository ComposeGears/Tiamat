@file:Suppress("MatchingDeclarationName")

package composegears.tiamat.example.platform

import composegears.tiamat.example.ui.core.AppFeature

actual fun Platform.start() = Unit
actual fun Platform.name(): String = "Android"
actual fun Platform.platformFeatures(): List<AppFeature> = listOf(
    AppFeature(
        name = "CameraX",
        description = "AndroidView + lifecycle handling",
        destination = AndroidViewLifecycleScreen
    ),
    AppFeature(
        name = "Predictive back",
        description = "Android predictive back",
        destination = PredictiveBack
    )
)