@file:Suppress("MatchingDeclarationName")

package composegears.tiamat.sample.platform

import composegears.tiamat.sample.ui.AppFeature

actual fun Platform.name(): String = "WASM"
actual fun Platform.platformFeatures(): List<AppFeature> = emptyList()