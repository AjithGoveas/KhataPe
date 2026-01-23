package dev.ajithgoveas.khatape

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KhataPeApplication : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        deleteDatabase("khata_pe")
        deleteDatabase("androidx.work.workdb")
    }
}