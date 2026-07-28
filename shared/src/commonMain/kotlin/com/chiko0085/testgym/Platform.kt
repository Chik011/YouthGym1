package com.chiko0085.testgym

import dev.gitlive.firebase.storage.Data

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

expect fun createStorageData(bytes: ByteArray): Data

expect fun isStorageSupported(): Boolean

expect fun saveSetting(key: String, value: String)

expect fun getSetting(key: String): String?

expect fun clearSetting(key: String)
