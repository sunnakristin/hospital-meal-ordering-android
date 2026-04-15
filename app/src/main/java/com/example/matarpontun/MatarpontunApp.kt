package com.example.matarpontun

import android.app.Application

/**
 * Application entry point. Initialises [AppContainer] before any Activity starts,
 * which sets up the Room database and DataStore that activities depend on.
 */
class MatarpontunApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
