package com.wapps1.redcarga

import android.app.Application
import android.content.Context
import com.wapps1.redcarga.core.util.LocaleHelper
import com.wapps1.redcarga.core.util.RcLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RedCargaApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        RcLogger.init(this)
        LocaleHelper.setLocale(this, "es")
    }
    
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(base, "es"))
    }
}