package com.chiko0085.testgym

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun openWebLink(url: String) {
    println("Membuka link di Android: $url")
}

actual fun getCurrentTimeMillis(): Long {
    val tz = TimeZone.getTimeZone("Asia/Jakarta")
    val cal = java.util.Calendar.getInstance(tz)
    return cal.timeInMillis
}

// --- TAMBAHAN BARU: Pekerja untuk mengubah angka menjadi teks kalender di Android ---
actual fun formatEpochToDate(millis: Long): String {
    if (millis <= 0L) return "-"
    val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
    sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
    return sdf.format(Date(millis))
}

actual fun parseDateToMillis(dateStr: String): Long? {
    return try {
        val parts = dateStr.trim().split(" ")
        if (parts.size != 3) return null
        
        val day = parts[0].toInt()
        val monthStr = parts[1].lowercase()
        val year = parts[2].toInt()
        
        val monthIdx = listOf("januari", "februari", "maret", "april", "mei", "juni", 
                              "juli", "agustus", "september", "oktober", "november", "desember")
                              .indexOf(monthStr)
        if (monthIdx == -1) return null
        
        val tz = TimeZone.getTimeZone("Asia/Jakarta")
        val cal = java.util.Calendar.getInstance(tz)
        cal.set(year, monthIdx, day, 0, 0, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    } catch (e: Exception) {
        null
    }
}

actual fun initFirebase() {
    // Di Android sudah otomatis via google-services.json
}