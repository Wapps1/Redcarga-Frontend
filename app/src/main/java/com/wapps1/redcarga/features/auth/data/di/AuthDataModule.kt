package com.wapps1.redcarga.features.auth.data.di

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wapps1.redcarga.features.auth.data.local.db.AuthDatabase
import com.wapps1.redcarga.BuildConfig
import com.wapps1.redcarga.features.auth.data.network.AppAccessTokenInterceptor
import com.wapps1.redcarga.features.auth.data.network.FirebaseIdTokenInterceptor
import com.wapps1.redcarga.features.auth.data.repositories.*
import com.wapps1.redcarga.features.auth.data.remote.services.*
import com.wapps1.redcarga.features.auth.domain.repositories.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthDataModule {

    @Provides @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides @Singleton @Named("backendBaseUrl")
    fun provideBackendBaseUrl(): String = BuildConfig.BACKEND_BASE_URL

    @Provides @Singleton @Named("firebaseBaseUrl")
    fun provideFirebaseBaseUrl(): String = BuildConfig.FIREBASE_BASE_URL

    @Provides @Singleton @Named("firebaseApiKey")
    fun provideFirebaseApiKey(): String = BuildConfig.FIREBASE_API_KEY

    @Provides @Singleton @Named("firebaseAuthInterceptor")
    fun provideFirebaseInterceptor(impl: FirebaseIdTokenInterceptor): Interceptor = impl

    @Provides @Singleton @Named("appAuthInterceptor")
    fun provideAppAccessInterceptor(impl: AppAccessTokenInterceptor): Interceptor = impl

    @Provides @Singleton @Named("backendClient")
    fun provideBackendClient(
        @Named("firebaseAuthInterceptor") firebaseInterceptor: Interceptor,
        @Named("appAuthInterceptor") appInterceptor: Interceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        // Interceptor adicional para loguear el cuerpo enviado (request) de forma segura
        val requestLogger = Interceptor { chain ->
            val req = chain.request()
            val copy = req.newBuilder().build()
            val buffer = Buffer()
            copy.body?.writeTo(buffer)
            val bodyString = try { buffer.readUtf8() } catch (_: Throwable) { "<binary or empty>" }
            android.util.Log.d("OkHttpRequest", "→ ${req.method} ${req.url}\nHeaders: ${req.headers}\nBody: $bodyString")
            chain.proceed(req)
        }

        return OkHttpClient.Builder()
            // Autenticación primero (modifica headers)
            .addInterceptor(firebaseInterceptor)
            .addInterceptor(appInterceptor)
            // Luego log del request ya final
            .addInterceptor(requestLogger)
            // Y log de red para ver exactamente lo que sale/entra por socket
            .addNetworkInterceptor(loggingInterceptor)
            .build()
    }

    @Provides @Singleton @Named("firebaseClient")
    fun provideFirebaseClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val requestLogger = Interceptor { chain ->
            val req = chain.request()
            val copy = req.newBuilder().build()
            val buffer = Buffer()
            copy.body?.writeTo(buffer)
            val bodyString = try { buffer.readUtf8() } catch (_: Throwable) { "<binary or empty>" }
            android.util.Log.d("OkHttpRequest", "→ ${req.method} ${req.url}\nHeaders: ${req.headers}\nBody: $bodyString")
            chain.proceed(req)
        }
        return OkHttpClient.Builder()
            .addInterceptor(requestLogger)
            .addNetworkInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * ⭐ Cliente especial SOLO para refresh de token (sin interceptor de App para evitar ciclo)
     * Solo usa Firebase interceptor porque el endpoint /iam/login requiere Firebase token
     */
    @Provides @Singleton @Named("tokenRefreshClient")
    fun provideTokenRefreshClient(
        @Named("firebaseAuthInterceptor") firebaseInterceptor: Interceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            // Solo Firebase interceptor (NO App interceptor para evitar ciclo)
            .addInterceptor(firebaseInterceptor)
            .addNetworkInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * ⭐ Retrofit especial para refresh de token (usa cliente sin App interceptor)
     */
    @Provides @Singleton @Named("tokenRefreshRetrofit")
    fun provideTokenRefreshRetrofit(
        @Named("backendBaseUrl") baseUrl: String,
        @Named("tokenRefreshClient") client: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides @Singleton @Named("backend")
    fun provideBackendRetrofit(
        @Named("backendBaseUrl") baseUrl: String,
        @Named("backendClient") client: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides @Singleton @Named("firebase")
    fun provideFirebaseRetrofit(
        @Named("firebaseBaseUrl") baseUrl: String,
        @Named("firebaseClient") client: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // Services
    @Provides @Singleton 
    fun provideAuthService(@Named("backend") r: Retrofit): AuthService =
        r.create(AuthService::class.java)
        
    @Provides @Singleton 
    fun provideIdentityService(@Named("backend") r: Retrofit): IdentityService =
        r.create(IdentityService::class.java)
        
    @Provides @Singleton 
    fun provideProviderService(@Named("backend") r: Retrofit): ProviderService =
        r.create(ProviderService::class.java)
        
    @Provides @Singleton 
    fun provideFirebaseAuthService(@Named("firebase") r: Retrofit): FirebaseAuthService =
        r.create(FirebaseAuthService::class.java)

    // Room
    @Provides @Singleton
    fun provideAuthDatabase(@ApplicationContext ctx: Context): AuthDatabase =
        Room.databaseBuilder(ctx, AuthDatabase::class.java, "auth.db").build()

    @Provides 
    fun provideAccountDao(db: AuthDatabase) = db.accountDao()
    
    @Provides 
    fun provideIntentDao(db: AuthDatabase) = db.intentDao()
    
    @Provides 
    fun providePersonDraftDao(db: AuthDatabase) = db.personDraftDao()

    // Repos (domain contracts)
    @Provides @Singleton
    fun bindAuthRemoteRepository(impl: AuthRemoteRepositoryImpl): AuthRemoteRepository = impl

    @Provides @Singleton
    fun bindFirebaseAuthRepository(impl: FirebaseAuthRepositoryImpl): FirebaseAuthRepository = impl

    @Provides @Singleton
    fun bindIdentityRemoteRepository(impl: IdentityRemoteRepositoryImpl): IdentityRemoteRepository = impl

    @Provides @Singleton
    fun bindProviderRemoteRepository(impl: ProviderRemoteRepositoryImpl): ProviderRemoteRepository = impl

    @Provides @Singleton
    fun bindAuthLocalRepository(impl: AuthLocalRepositoryImpl): AuthLocalRepository = impl

    @Provides @Singleton
    fun bindSecureTokenRepository(impl: SecureTokenRepositoryImpl): SecureTokenRepository = impl
}
