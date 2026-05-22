package com.chiko0085.testgym.model

import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val id: String,
    var name: String,
    var username: String,
    var password: String,
    var remainingDays: Int,
    var weight: Double = 0.0,
    var height: Double = 0.0
)