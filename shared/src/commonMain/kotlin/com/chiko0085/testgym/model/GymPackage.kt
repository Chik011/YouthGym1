package com.chiko0085.testgym.model

import kotlinx.serialization.Serializable

@Serializable
data class GymPackage(
    val id: String,
    var name: String,
    var price: Double? = 0.0,
    var durationDays: Int = 0
)
