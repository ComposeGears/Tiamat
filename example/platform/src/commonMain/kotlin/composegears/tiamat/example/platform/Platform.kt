package composegears.tiamat.example.platform

import composegears.tiamat.example.ui.core.AppFeature

expect object Platform {
    fun start()
    fun name(): String
    fun features(): List<AppFeature>
}