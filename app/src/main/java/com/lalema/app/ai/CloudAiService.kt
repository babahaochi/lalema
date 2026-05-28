package com.lalema.app.ai

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudAiService @Inject constructor(
    private val configManager: AiConfigManager
) {
    private fun createClient(): OpenAiApi? {
        val config = configManager.config.value
        if (!configManager.isCloudAiAvailable()) return null

        val baseUrl = when (config.provider) {
            AiProvider.CUSTOM -> config.customBaseUrl
            else -> config.provider.baseUrl
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${config.apiKey}")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAiApi::class.java)
    }

    suspend fun chat(messages: List<ChatMessage>): Result<String> {
        return try {
            val api = createClient() ?: return Result.failure(Exception("云端AI未配置"))
            val config = configManager.config.value

            val request = ChatRequest(
                model = config.model,
                messages = messages.map {
                    Message(role = it.role, content = it.content)
                }
            )

            val response = api.chat(request)
            val content = response.choices.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("AI返回为空"))

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeHealth(prompt: String): Result<String> {
        val systemPrompt = """你是一位专业的肠道健康顾问。请根据用户的排便记录数据，提供详细的健康分析报告。
分析维度包括：
1. 整体健康评分（0-100分）
2. 排便规律性评估
3. 便便形态分析（Bristol标准）
4. 潜在健康风险提示
5. 个性化改善建议

请以友好、专业的语气回答，使用中文。如果数据不足，请说明需要更多记录。"""

        return chat(listOf(
            ChatMessage(role = "system", content = systemPrompt),
            ChatMessage(role = "user", content = prompt)
        ))
    }

    suspend fun suggestDiet(prompt: String): Result<String> {
        val systemPrompt = """你是一位专业的营养师。请根据用户的排便记录，提供个性化的饮食建议。
建议内容包括：
1. 推荐食物清单（含具体食材）
2. 应避免的食物
3. 每日饮食搭配建议
4. 饮水和纤维摄入建议

请以友好、实用的语气回答，使用中文。"""

        return chat(listOf(
            ChatMessage(role = "system", content = systemPrompt),
            ChatMessage(role = "user", content = prompt)
        ))
    }

    suspend fun predictTrend(prompt: String): Result<String> {
        val systemPrompt = """你是一位健康数据分析师。请根据用户的历史排便数据，预测未来趋势。
分析内容包括：
1. 未来一周的排便频率预测
2. 趋势方向（改善/稳定/恶化）
3. 需要关注的异常信号
4. 预防性建议

请以专业但易懂的方式回答，使用中文。"""

        return chat(listOf(
            ChatMessage(role = "system", content = systemPrompt),
            ChatMessage(role = "user", content = prompt)
        ))
    }
}

interface OpenAiApi {
    @POST("chat/completions")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}

data class ChatRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("max_tokens") val maxTokens: Int = 2000
)

data class Message(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class ChatResponse(
    @SerializedName("choices") val choices: List<Choice>
)

data class Choice(
    @SerializedName("message") val message: Message,
    @SerializedName("finish_reason") val finishReason: String?
)
