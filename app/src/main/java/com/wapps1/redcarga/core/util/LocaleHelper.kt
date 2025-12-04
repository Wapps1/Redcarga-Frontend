package com.wapps1.redcarga.core.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, language: String = "es"): Context {
        return try {
            val locale = Locale(language)
            Locale.setDefault(locale)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                updateResources(context, locale)
            } else {
                updateResourcesLegacy(context, locale)
            }
        } catch (e: Exception) {
            // Si falla, retornar el contexto original
            Log.e("LocaleHelper", "Error setting locale, using original context", e)
            context
        }
    }
    
    private fun updateResources(context: Context, locale: Locale): Context {
        return try {
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            context.createConfigurationContext(configuration)
        } catch (e: Exception) {
            Log.e("LocaleHelper", "Error creating configuration context", e)
            context
        }
    }
    
    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        return try {
            val resources = context.resources
            val configuration = resources.configuration
            configuration.locale = locale
            configuration.setLayoutDirection(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        } catch (e: Exception) {
            Log.e("LocaleHelper", "Error updating legacy configuration", e)
            context
        }
    }
    
    fun getCurrentLanguage(context: Context): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0].language
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale.language
            }
        } catch (e: Exception) {
            Log.e("LocaleHelper", "Error getting current language", e)
            "es"
        }
    }
}
