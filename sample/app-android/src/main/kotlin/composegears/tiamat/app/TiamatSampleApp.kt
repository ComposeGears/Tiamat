package composegears.tiamat.app

import android.app.Application
import composegears.tiamat.sample.content.state.KoinInit
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TiamatSampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        KoinInit.start()
    }
}