package com.lalema.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalema.app.ai.ChatMessage
import com.lalema.app.ai.CloudAiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val cloudAi: CloudAiService
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(content: String) {
        val userMessage = ChatMessage(role = "user", content = content)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            val systemPrompt = """你是一位专业的肠道健康顾问。你的职责是：
1. 回答用户关于肠道健康、排便习惯、饮食调理的问题
2. 提供科学、实用的健康建议
3. 在必要时建议用户就医
4. 保持友好、专业的语气
5. 使用中文回答

请注意：
- 不要诊断疾病，只提供健康建议
- 如果症状严重，建议及时就医
- 基于一般医学常识回答，不替代专业医疗建议"""

            val allMessages = listOf(
                ChatMessage(role = "system", content = systemPrompt)
            ) + _messages.value.takeLast(10)

            val result = cloudAi.chat(allMessages)

            result.onSuccess { response ->
                val assistantMessage = ChatMessage(role = "assistant", content = response)
                _messages.value = _messages.value + assistantMessage
            }.onFailure { error ->
                val errorMessage = ChatMessage(
                    role = "assistant",
                    content = "抱歉，我暂时无法回答。${error.message ?: "请检查AI配置"}"
                )
                _messages.value = _messages.value + errorMessage
            }

            _isLoading.value = false
        }
    }
}
