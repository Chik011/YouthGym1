package com.chiko0085.testgym.model

import kotlinx.serialization.Serializable

@Serializable
data class Admin(
    val username: String = "admin",
    val password: String = "admin123",
    val role: String = "admin", // "admin" or "super_admin"
    val permissions: List<String> = listOf("dashboard")
)
