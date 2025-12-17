package composegears.tiamat.sample.platform

import composegears.tiamat.sample.ui.AppFeature

object Platform

expect fun Platform.name(): String
expect fun Platform.platformFeatures(): List<AppFeature>

fun Platform.features(): List<AppFeature> =
    platformFeatures()