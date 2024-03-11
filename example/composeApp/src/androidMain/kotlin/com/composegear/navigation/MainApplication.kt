package com.composegear.navigation

import android.app.Application
import content.examples.koin.KoinLib

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KoinLib.start()
    }
}