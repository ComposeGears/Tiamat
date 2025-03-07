@file:Suppress("MatchingDeclarationName")

package composegears.tiamat.example.platform

import composegears.tiamat.example.ui.core.AppFeature

actual fun Platform.start() = Unit
actual fun Platform.name(): String = "Android"
actual fun Platform.platformFeatures(): List<AppFeature> = emptyList()