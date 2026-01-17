package dev.ajithgoveas.khatape

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KhataPeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialization code if needed
        deleteDatabase("khata_pe" ) // For testing purposes only
    }
}