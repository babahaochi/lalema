package com.lalema.app.ai

enum class AiProvider(val displayName: String, val baseUrl: String, val defaultModel: String) {
    OPENAI("OpenAI", "https://api.openai.com/v1/", "gpt-4o-mini"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1/", "deepseek-chat"),
    DASHSCOPE("通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1/", "qwen-turbo"),
    CUSTOM("自定义", "", "");

    companion object {
        fun fromName(name: String): AiProvider = values().find { it.name == name } ?: OPENAI
    }
}

data class AiConfig(
    val provider: AiProvider = AiProvider.DEEPSEEK,
    val apiKey: String = "",
    val model: String = provider.defaultModel,
    val customBaseUrl: String = "",
    val useLocalAiOnly: Boolean = false
)
