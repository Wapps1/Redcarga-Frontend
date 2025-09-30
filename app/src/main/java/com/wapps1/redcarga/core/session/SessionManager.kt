package com.wapps1.redcarga.core.session

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    private val _isAuthenticated = MutableStateFlow(isUserLoggedIn())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    fun isUserLoggedIn(): Boolean {
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        val userId = prefs.getString(KEY_USER_ID, null)
        return !token.isNullOrEmpty() && !userId.isNullOrEmpty()
    }
    
   
    fun saveSession(token: String, userId: String) {
        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_USER_ID, userId)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            apply()
        }
        _isAuthenticated.value = true
    }
    
   
    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }
    
  
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
 
    fun logout() {
        prefs.edit().clear().apply()
        _isAuthenticated.value = false
    }
    
  
    fun isSessionExpired(expirationDays: Int = 30): Boolean {
        val loginTime = prefs.getLong(KEY_LOGIN_TIME, 0)
        if (loginTime == 0L) return true
        
        val currentTime = System.currentTimeMillis()
        val daysSinceLogin = (currentTime - loginTime) / (1000 * 60 * 60 * 24)
        return daysSinceLogin > expirationDays
    }
    
    companion object {
        private const val PREFS_NAME = "red_carga_session"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_LOGIN_TIME = "login_time"
    }
}
