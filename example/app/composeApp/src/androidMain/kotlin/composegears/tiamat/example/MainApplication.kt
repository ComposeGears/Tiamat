package composegears.tiamat.example

import android.app.Application
import composegears.tiamat.sample.koin.KoinLib

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KoinLib.start()
    }
}