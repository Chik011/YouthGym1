package com.chiko0085.testgym

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Trainer(
    val id: String,
    val name: String,
    val username: String = "",
    val password: String = "",
    val gender: String = "Laki-laki",
    val specialization: String,
    val experience: String,
    @SerialName("rate")
    val rating: Double? = 0.0,
    val description: String,
    val availability: List<String> = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"),
    val schedules: List<String> = emptyList(),
    val profileImageUrl: String = ""
)