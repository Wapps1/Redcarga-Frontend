package com.wapps1.redcarga

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wapps1.redcarga.core.navigation.Navigation
import com.wapps1.redcarga.core.session.SessionManager
import com.wapps1.redcarga.core.ui.theme.RedcargaTheme
import com.wapps1.redcarga.core.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "es"))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedcargaTheme {
                Navigation(sessionManager = sessionManager)
            }
        }
    }
}

