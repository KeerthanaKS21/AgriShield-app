package com.agrishield.app.data.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender = MessageSender.USER,
    val text: String = "",
    val language: String = "auto", // "ta", "en", "mixed"
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val contextInfo: String? = null
)

enum class MessageSender {
    USER,
    AGRIBOT,
    SYSTEM
}
