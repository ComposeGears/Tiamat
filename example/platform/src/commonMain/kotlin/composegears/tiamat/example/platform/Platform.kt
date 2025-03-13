package composegears.tiamat.example.platform

import composegears.tiamat.example.ui.core.AppFeature

object Platform

expect fun Platform.start()
expect fun Platform.name(): String
expect fun Platform.platformFeatures(): List<AppFeature>

fun Platform.features(): List<AppFeature> =
    platformFeatures() + listOf(
        AppFeature(
            name = "Predictive back",
            description = "Platform predictive back",
            destination = PredictiveBack
        )
    )
