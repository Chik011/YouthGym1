package com.chiko0085.testgym

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun openWebLink(url: String)

expect fun getCurrentTimeMillis(): Long

expect fun formatEpochToDate(millis: Long): String