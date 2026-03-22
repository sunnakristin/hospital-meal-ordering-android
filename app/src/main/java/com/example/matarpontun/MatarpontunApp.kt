package com.example.matarpontun

import android.app.Application

class MatarpontunApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
