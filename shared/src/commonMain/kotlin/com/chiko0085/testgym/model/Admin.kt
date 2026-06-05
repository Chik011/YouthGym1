package com.chiko0085.testgym.model

import kotlinx.serialization.Serializable

@Serializable
data class Admin(
    val username: String = "admin",
    val password: String = "admin123"
)
