package com.chiko0085.testgym.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String = "",
    val memberName: String,
    val amount: Double,
    val type: String, // "Gym Package", "PT Package", "Padel"
    val description: String,
    val timestamp: Long
)
