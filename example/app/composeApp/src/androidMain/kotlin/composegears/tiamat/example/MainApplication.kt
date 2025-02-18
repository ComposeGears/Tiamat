package composegears.tiamat.example

import android.app.Application
import composegears.tiamat.example.extra.A3rdParty
import composegears.tiamat.example.platform.Platform
import composegears.tiamat.example.platform.start

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Platform.start()
        A3rdParty.start()
    }
}