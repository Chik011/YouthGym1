package com.chiko0085.testgym

import dev.gitlive.firebase.storage.Data
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.content.Context

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

// --- KODE BARU: Global Context untuk Android ---
private var androidContext: Context? = null

fun setAndroidContext(context: Context) {
    androidContext = context
}

actual fun openWebLink(url: String) {
    val context = androidContext
    if (context != null) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            println("INFO: Membuka link di Android: $url")
        } catch (e: Exception) {
            println("ERROR: Gagal membuka link: ${e.message}")
        }
    } else {
        println("ERROR: Android Context belum diinisialisasi")
    }
}

actual fun openEmailClient(recipient: String, subject: String, body: String) {
    val context = androidContext
    if (context != null) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            println("ERROR: Gagal membuka email client: ${e.message}")
        }
    }
}

actual fun getCurrentTimeMillis(): Long {
    val tz = TimeZone.getTimeZone("Asia/Jakarta")
    val cal = java.util.Calendar.getInstance(tz)
    return cal.timeInMillis
}

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

actual fun createStorageData(bytes: ByteArray): Data = Data(bytes)

actual fun isStorageSupported(): Boolean = true
