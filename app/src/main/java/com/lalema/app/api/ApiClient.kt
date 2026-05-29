package com.lalema.app.api

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.lalema.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiClient {
    private const val BASE_URL = "https://47.109.151.2/api/"
    private var apiService: ApiService? = null
    private var tokenProvider: (() -> String?)? = null

    fun init(context: Context) {
        val prefs = EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        tokenProvider = { prefs.getString("token", null) }
    }

    fun setToken(context: Context, token: String?) {
        val prefs = EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(context: Context): String? {
        val prefs = EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return prefs.getString("token", null)
    }

    fun clearToken(context: Context) {
        val prefs = EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        prefs.edit().remove("token").apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }

    private fun createUnsafeOkHttpClient(): OkHttpClient.Builder {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, java.security.SecureRandom())
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
    }

    fun getService(context: Context): ApiService {
        if (apiService == null) {
            try {
                android.util.Log.e("ApiClient", "=== Starting ApiClient initialization ===")
                android.util.Log.e("ApiClient", "BASE_URL: $BASE_URL")
                init(context)
                android.util.Log.e("ApiClient", "init() completed")

                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                android.util.Log.e("ApiClient", "Creating unsafe OkHttp client...")
                val unsafeBuilder = createUnsafeOkHttpClient()
                android.util.Log.e("ApiClient", "Unsafe client builder created")
                
                val client = unsafeBuilder
                    .addInterceptor(Interceptor { chain ->
                        val token = tokenProvider?.invoke()
                        val request = if (token != null) {
                            chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer $token")
                                .build()
                        } else {
                            chain.request()
                        }
                        chain.proceed(request)
                    })
                    .addInterceptor(logging)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
                android.util.Log.e("ApiClient", "OkHttp client built successfully")

                android.util.Log.e("ApiClient", "Creating Retrofit with baseUrl: $BASE_URL")
                apiService = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
                android.util.Log.e("ApiClient", "=== ApiClient initialization SUCCESS ===")
            } catch (e: Exception) {
                android.util.Log.e("ApiClient", "=== ApiClient initialization FAILED ===", e)
                android.util.Log.e("ApiClient", "Exception type: ${e.javaClass.name}")
                android.util.Log.e("ApiClient", "Exception message: ${e.message}")
                throw e
            }
        }
        return apiService!!
    }
}
