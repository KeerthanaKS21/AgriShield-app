package com.agrishield.app.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {

    @POST("models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class GeminiRequest(
    @SerializedName("contents") val contents: List<GeminiContent>,
    @SerializedName("systemInstruction") val systemInstruction: GeminiSystemInstruction? = null,
    @SerializedName("generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContent(
    @SerializedName("role") val role: String = "user",
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text") val text: String
)

data class GeminiSystemInstruction(
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiGenerationConfig(
    @SerializedName("temperature") val temperature: Double = 0.4,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 1024
)

data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate>? = null,
    @SerializedName("error") val error: GeminiError? = null
)

data class GeminiCandidate(
    @SerializedName("content") val content: GeminiContentResponse? = null,
    @SerializedName("finishReason") val finishReason: String? = null
)

data class GeminiContentResponse(
    @SerializedName("parts") val parts: List<GeminiPart>? = null
)

data class GeminiError(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: String
)
