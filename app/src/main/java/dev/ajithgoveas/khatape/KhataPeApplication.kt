package dev.ajithgoveas.khatape

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KhataPeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        deleteDatabase("khata_pe")
        deleteDatabase("androidx.work.workdb")
    }
}