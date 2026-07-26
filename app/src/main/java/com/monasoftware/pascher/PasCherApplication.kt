package com.monasoftware.pascher

import android.app.Application
import com.monasoftware.pascher.di.AppContainer
import com.monasoftware.pascher.di.AppContainerImpl

class PasCherApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}
