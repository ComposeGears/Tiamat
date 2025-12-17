@file:Suppress("MatchingDeclarationName")

package composegears.tiamat.sample.platform

import composegears.tiamat.sample.ui.AppFeature

actual fun Platform.name(): String = "iOS"
actual fun Platform.platformFeatures(): List<AppFeature> = listOf(
    AppFeature(
        name = "Predictive back",
        description = "iOS predictive back",
        destination = PredictiveBack
    )
)