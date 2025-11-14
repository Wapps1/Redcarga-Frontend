package com.wapps1.redcarga.features.auth.data.network

import android.util.Log
import com.wapps1.redcarga.features.auth.data.mappers.toDomain
import com.wapps1.redcarga.features.auth.data.mappers.toDomainSession
import com.wapps1.redcarga.features.auth.data.mappers.toAccountSnapshotDomain
import com.wapps1.redcarga.features.auth.data.remote.models.AppLoginRequestDto
import com.wapps1.redcarga.features.auth.data.remote.services.AuthService
import com.wapps1.redcarga.features.auth.domain.models.value.Platform
import com.wapps1.redcarga.features.auth.domain.repositories.AuthLocalRepository
import com.wapps1.redcarga.features.auth.domain.repositories.SecureTokenRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "AppTokenInterceptor"
private const val TOKEN_REFRESH_MARGIN_MS = 60_000L // 60 segundos antes de expirar

/**
 * Interceptor que maneja el token IAM con refresh automático
 * - Verifica si el token está expirado antes de usarlo
 * - Refresca automáticamente si está expirado o cerca de expirar
 * - Si recibe 401, intenta refrescar y reintentar una vez
 * 
 * ⭐ Usa un Retrofit especial sin App interceptor para evitar ciclo de dependencias
 */
class AppAccessTokenInterceptor @Inject constructor(
    private val secureTokenRepository: SecureTokenRepository,
    @Named("tokenRefreshRetrofit") private val tokenRefreshRetrofit: Retrofit,
    private val authLocalRepository: AuthLocalRepository
) : Interceptor {
    
    private fun isTokenExpiredOrNearExpiry(expiresAt: Long): Boolean {
        val now = System.currentTimeMillis()
        val expiryWithMargin = expiresAt - TOKEN_REFRESH_MARGIN_MS
        return now >= expiryWithMargin
    }
    
    /**
     * Refresca el token IAM llamando al backend
     * ⭐ Usa el Retrofit especial sin App interceptor para evitar ciclo
     */
    private suspend fun refreshToken(): Boolean {
        return try {
            Log.d(TAG, "🔄 Intentando refrescar token IAM...")
            
            // Necesitamos Firebase session para hacer app login
            val fb = secureTokenRepository.getFirebaseSession()
            if (fb == null) {
                Log.e(TAG, "❌ No hay Firebase session para refrescar token")
                return false
            }
            
            // ⭐ Crear servicio desde el Retrofit especial (sin App interceptor)
            val authService = tokenRefreshRetrofit.create(AuthService::class.java)
            
            // Hacer login de nuevo (esto obtiene un nuevo token)
            // El Retrofit especial solo tiene Firebase interceptor, así que agregará el Firebase token automáticamente
            val dto = authService.login(
                AppLoginRequestDto(
                    platform = Platform.ANDROID.name, // "ANDROID"
                    ip = "0.0.0.0" // IP por defecto, el backend puede obtener la real
                )
            )
            
            // Mapear a dominio y guardar
            val now = System.currentTimeMillis()
            val app = dto.toDomainSession(now)
            val snapshot = dto.toAccountSnapshotDomain()
            
            // Guardar la nueva sesión
            secureTokenRepository.saveAppSession(app)
            if (snapshot != null) {
                authLocalRepository.saveAccountSnapshot(snapshot)
            }
            
            Log.d(TAG, "✅ Token refrescado exitosamente - nuevo expiresAt: ${app.expiresAt}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al refrescar token: ${e.message}", e)
            false
        }
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val needsAppAuth = req.header("X-App-Auth") == "true"

        if (!needsAppAuth) {
            return chain.proceed(req)
        }

        // Obtener token actual
        val app = runBlocking { secureTokenRepository.getAppSession() }
            ?: throw IOException("Missing App access token")

        // ⭐ VERIFICAR SI EL TOKEN ESTÁ EXPIRADO O CERCA DE EXPIRAR
        if (isTokenExpiredOrNearExpiry(app.expiresAt)) {
            Log.w(TAG, "⚠️ Token expirado o cerca de expirar (expiresAt=${app.expiresAt}), refrescando...")
            val refreshed = runBlocking { refreshToken() }
            
            if (!refreshed) {
                throw IOException("Failed to refresh expired token")
            }
            
            // Obtener el nuevo token
            val newApp = runBlocking { secureTokenRepository.getAppSession() }
                ?: throw IOException("Token refresh failed - no new token")
            
            // Usar el nuevo token
            val newReq = req.newBuilder()
                .removeHeader("X-App-Auth")
                .header("Authorization", "Bearer ${newApp.accessToken}")
                .build()
            
            val response = chain.proceed(newReq)
            
            // ⭐ Si aún recibimos 401 después de refrescar, no hay nada más que hacer
            if (response.code == 401) {
                response.close()
                throw IOException("Token refresh failed - still unauthorized")
            }
            
            return response
        }

        // Token válido, usar normalmente
        val newReq = req.newBuilder()
            .removeHeader("X-App-Auth")
            .header("Authorization", "Bearer ${app.accessToken}")
            .build()

        val response = chain.proceed(newReq)

        // ⭐ SI RECIBIMOS 401, INTENTAR REFRESCAR Y REINTENTAR UNA VEZ
        if (response.code == 401 && response.request.url.toString().contains("redcargabk")) {
            Log.w(TAG, "⚠️ Recibido 401 Unauthorized, intentando refrescar token...")
            response.close() // Cerrar la respuesta anterior
            
            val refreshed = runBlocking { refreshToken() }
            
            if (refreshed) {
                // Obtener el nuevo token
                val newApp = runBlocking { secureTokenRepository.getAppSession() }
                    ?: throw IOException("Token refresh failed - no new token")
                
                // Reintentar la petición con el nuevo token
                val retryReq = req.newBuilder()
                    .removeHeader("X-App-Auth")
                    .header("Authorization", "Bearer ${newApp.accessToken}")
                    .build()
                
                Log.d(TAG, "🔄 Reintentando petición con nuevo token...")
                return chain.proceed(retryReq)
            } else {
                throw IOException("Token refresh failed after 401")
            }
        }

        return response
    }
}
