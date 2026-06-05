package com.chiko0085.testgym.model

import kotlinx.serialization.Serializable

@Serializable
data class Reservation(
    val id: String = "",
    val date: Long, // Epoch millis at start of day (00:00)
    val hour: Int, // 0-23
    val status: String, // "Empty", "Booked", "Maintenance"
    val reservedByName: String = ""
)
