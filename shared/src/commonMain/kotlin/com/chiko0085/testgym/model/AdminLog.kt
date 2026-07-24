package com.chiko0085.testgym.model

import kotlinx.serialization.Serializable

@Serializable
data class AdminLog(
    val id: String = "",
    val adminUsername: String,
    val action: String,
    val details: String,
    val timestamp: Long
)
