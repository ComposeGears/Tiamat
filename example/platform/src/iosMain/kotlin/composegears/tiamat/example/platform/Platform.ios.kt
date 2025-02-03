@file:Suppress("MatchingDeclarationName")

package composegears.tiamat.example.platform

import composegears.tiamat.example.ui.core.AppFeature

actual object Platform {
    actual fun start() = Unit
    actual fun name(): String = "iOS"
    actual fun features(): List<AppFeature> = emptyList()
}