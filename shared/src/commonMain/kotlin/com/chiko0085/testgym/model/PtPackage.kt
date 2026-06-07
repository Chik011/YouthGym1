package com.chiko0085.testgym.model

import kotlinx.serialization.Serializable

@Serializable
data class PtPackage(
    val id: String,
    var name: String,
    var price: Double? = 0.0,
    var sessions: Int = 0
)
