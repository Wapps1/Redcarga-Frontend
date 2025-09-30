package com.wapps1.redcarga

import android.app.Application
import android.content.Context
import com.wapps1.redcarga.core.util.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RedCargaApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Establecer español como idioma predeterminado de la aplicación
        LocaleHelper.setLocale(this, "es")
    }
    
    override fun attachBaseContext(base: Context) {
        // Aplicar el idioma antes de que la aplicación se inicialice
        super.attachBaseContext(LocaleHelper.setLocale(base, "es"))
    }
}