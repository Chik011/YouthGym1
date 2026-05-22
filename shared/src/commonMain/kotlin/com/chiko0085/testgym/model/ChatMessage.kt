package com.chiko0085.testgym.model

import kotlin.time.Clock

data class ChatMessage(
    val senderId: String, // "admin" or member.id
    val receiverId: String,
    val message: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
