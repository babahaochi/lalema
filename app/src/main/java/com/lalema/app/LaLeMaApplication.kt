package com.lalema.app

import android.app.Application
import com.lalema.app.api.ApiClient
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LaLeMaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}
