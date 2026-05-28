package com.lalema.app.ai

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiConfigManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "ai_config_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<AiConfig> = _config

    fun updateConfig(config: AiConfig) {
        prefs.edit().apply {
            putString("provider", config.provider.name)
            putString("api_key", config.apiKey)
            putString("model", config.model)
            putString("custom_base_url", config.customBaseUrl)
            putBoolean("use_local_only", config.useLocalAiOnly)
            apply()
        }
        _config.value = config
    }

    private fun loadConfig(): AiConfig {
        val providerName = prefs.getString("provider", AiProvider.DEEPSEEK.name) ?: AiProvider.DEEPSEEK.name
        val provider = AiProvider.fromName(providerName)
        return AiConfig(
            provider = provider,
            apiKey = prefs.getString("api_key", "") ?: "",
            model = prefs.getString("model", provider.defaultModel) ?: provider.defaultModel,
            customBaseUrl = prefs.getString("custom_base_url", "") ?: "",
            useLocalAiOnly = prefs.getBoolean("use_local_only", false)
        )
    }

    fun isCloudAiAvailable(): Boolean {
        val cfg = _config.value
        return !cfg.useLocalAiOnly && cfg.apiKey.isNotBlank() &&
                (cfg.provider != AiProvider.CUSTOM || cfg.customBaseUrl.isNotBlank())
    }
}
