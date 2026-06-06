package com.chiko0085.testgym.model

import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val id: String,
    var name: String,
    var username: String,
    var password: String,
    var remainingDays: Int = 0,
    var joinDate: Long = 0L,
    var expiredDate: Long = 0L,
    var weight: Double? = 0.0,
    var height: Double? = 0.0,
    var gender: String = "Laki-laki",
    var phoneNumber: String = "",
    var pricePaid: Double? = 0.0,    // Tambahan untuk laporan keuangan
    var packageName: String = "",   // Tambahan untuk laporan keuangan
    var remainingPtSessions: Int = 0 // Sisa sesi Personal Trainer
)