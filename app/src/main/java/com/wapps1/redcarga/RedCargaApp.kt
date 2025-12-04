package com.wapps1.redcarga

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import com.wapps1.redcarga.core.util.LocaleHelper
import com.wapps1.redcarga.core.util.RcLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RedCargaApp : Application() {
    
    override fun attachBaseContext(base: Context) {
        try {
            Log.d("RedCargaApp", "attachBaseContext - Android ${Build.VERSION.SDK_INT}")
            super.attachBaseContext(LocaleHelper.setLocale(base, "es"))
        } catch (e: Exception) {
            Log.e("RedCargaApp", "Error en attachBaseContext", e)
            // Si falla, usar el contexto original
            super.attachBaseContext(base)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        try {
            Log.d("RedCargaApp", "onCreate iniciado")
            RcLogger.init(this)
            Log.d("RedCargaApp", "RcLogger inicializado")
        } catch (e: Exception) {
            Log.e("RedCargaApp", "Error inicializando RcLogger", e)
        }
        try {
            LocaleHelper.setLocale(this, "es")
            Log.d("RedCargaApp", "Locale configurado")
        } catch (e: Exception) {
            Log.e("RedCargaApp", "Error configurando locale", e)
        }
        Log.d("RedCargaApp", "onCreate completado")
    }
}