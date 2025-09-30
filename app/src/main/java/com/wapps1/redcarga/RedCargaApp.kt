package com.wapps1.redcarga

import android.app.Application
import android.content.Context
import com.wapps1.redcarga.core.util.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RedCargaApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        LocaleHelper.setLocale(this, "es")
    }
    
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(base, "es"))
    }
}