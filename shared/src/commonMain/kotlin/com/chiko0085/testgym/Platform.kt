package com.chiko0085.testgym

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun openWebLink(url: String)

expect fun openEmailClient(recipient: String, subject: String, body: String)

expect fun getCurrentTimeMillis(): Long

expect fun formatEpochToDate(millis: Long): String

expect fun parseDateToMillis(dateStr: String): Long?

expect fun initFirebase()