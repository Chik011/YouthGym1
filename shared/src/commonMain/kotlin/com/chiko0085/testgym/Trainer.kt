package com.chiko0085.testgym

import kotlinx.serialization.Serializable

@Serializable
data class Trainer(
    val id: String,
    val name: String,
    val username: String = "",
    val password: String = "",
    val specialization: String,
    val experience: String,
    val rating: Double,
    val description: String,
)