@file:Suppress("MatchingDeclarationName")

package composegears.tiamat.sample.platform

import composegears.tiamat.sample.ui.AppFeature

actual fun Platform.name(): String = "Android"
actual fun Platform.platformFeatures(): List<AppFeature> = listOf(
    AppFeature(
        name = "CameraX",
        description = "CameraX + Lifecycle",
        destination = CameraXLifecycleScreen
    ),
    AppFeature(
        name = "Predictive back",
        description = "Android predictive back",
        destination = PredictiveBack
    )
)