package composegears.tiamat.example.extra

import composegears.tiamat.example.extra.content.A3rdPKoin
import composegears.tiamat.example.extra.content.a3rdPKoinModule
import composegears.tiamat.example.ui.core.AppFeature
import org.koin.core.context.startKoin

object A3rdParty {

    fun start() {
        startKoin { modules(a3rdPKoinModule) }
    }

    fun features(): List<AppFeature> = listOf(
        AppFeature(
            name = "Koin",
            description = "Koin view-models usage example",
            destination = A3rdPKoin
        )
    )
}