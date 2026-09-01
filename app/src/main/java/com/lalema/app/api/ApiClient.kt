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
import java.util.concurrent.TimeUnit

object ApiClient {
    // 生产域名，与 network_security_config.xml 的 5ichat.online 一致。
    // 后端已部署正式 CA 证书（Nginx + Let's Encrypt），使用系统默认信任链即可，
    // 不再接受任意证书（修复此前全信任 SSL 的安全隐患）。
    private const val BASE_URL = "https://5ichat.online/api/"
    private const val PREFS_NAME = "auth_prefs"
    private var apiService: ApiService? = null

    private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun init(context: Context) {
        getToken(context)
    }

    fun setToken(context: Context, token: String?) {
        getPrefs(context).edit().putString("token", token).apply()
    }

    fun getToken(context: Context): String? {
        return getPrefs(context).getString("token", null)
    }

    fun clearToken(context: Context) {
        getPrefs(context).edit().remove("token").apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }

    /**
     * 安全 OkHttp 客户端：使用系统默认 CA 信任链 + 标准 hostname 校验，
     * 不再接受任意证书、不再恒 true 校验 hostname（修复全信任 SSL 的安全隐患）。
     * 仅额外注入 JWT Authorization 拦截器。
     */
    private fun createOkHttpClient(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val token = getToken(context)
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
    }

    fun getService(context: Context): ApiService {
        if (apiService == null) {
            apiService = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
        return apiService!!
    }
}
