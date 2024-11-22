@file:Suppress("MatchingDeclarationName")

package composegears.tiamat.example.platform

actual object Platform {
    actual fun start() = Unit
    actual fun name(): String = "Android"
    actual fun features(): List<PlatformFeature> = emptyList()
}