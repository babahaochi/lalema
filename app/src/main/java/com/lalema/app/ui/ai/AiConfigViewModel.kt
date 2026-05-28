package com.lalema.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalema.app.ai.AiConfig
import com.lalema.app.ai.AiConfigManager
import com.lalema.app.ai.AiProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiConfigViewModel @Inject constructor(
    private val configManager: AiConfigManager
) : ViewModel() {

    val config: StateFlow<AiConfig> = configManager.config

    fun updateProvider(provider: AiProvider) {
        val current = config.value
        configManager.updateConfig(current.copy(provider = provider, model = provider.defaultModel))
    }

    fun saveConfig(
        apiKey: String,
        model: String,
        customUrl: String,
        useLocalOnly: Boolean
    ) {
        val current = config.value
        configManager.updateConfig(
            current.copy(
                apiKey = apiKey,
                model = model.ifBlank { current.provider.defaultModel },
                customBaseUrl = customUrl,
                useLocalAiOnly = useLocalOnly
            )
        )
    }
}
