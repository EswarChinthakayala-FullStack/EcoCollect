package com.wastereporting

import android.app.Application
import android.content.Context

class WasteReportingApp : Application() {
    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}
