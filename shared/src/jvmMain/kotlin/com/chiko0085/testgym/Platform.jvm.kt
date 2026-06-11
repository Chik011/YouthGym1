package com.chiko0085.testgym

import dev.gitlive.firebase.storage.Data
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.FirebaseOptions
import com.google.firebase.FirebasePlatform
import android.app.Application

import java.util.TimeZone

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun openWebLink(url: String) {
    val os = System.getProperty("os.name").lowercase()
    val rt = Runtime.getRuntime()
    if (os.contains("win")) rt.exec("rundll32 url.dll,FileProtocolHandler $url")
    else if (os.contains("mac")) rt.exec("open $url")
    else rt.exec("xdg-open $url")
}

actual fun openEmailClient(recipient: String, subject: String, body: String) {
    try {
        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
        if (desktop != null && desktop.isSupported(Desktop.Action.MAIL)) {
            val mailto = "mailto:$recipient" +
                    "?subject=${URLEncoder.encode(subject, "UTF-8").replace("+", "%20")}" +
                    "&body=${URLEncoder.encode(body, "UTF-8").replace("+", "%20")}"
            desktop.mail(URI(mailto))
        } else {
            // Fallback to openWebLink if Desktop.mail is not supported
            val mailto = "mailto:$recipient" +
                    "?subject=${URLEncoder.encode(subject, "UTF-8").replace("+", "%20")}" +
                    "&body=${URLEncoder.encode(body, "UTF-8").replace("+", "%20")}"
            openWebLink(mailto)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

actual fun getCurrentTimeMillis(): Long {
    // Mengambil waktu realtime dan memastikan dalam konteks GMT+7 (WIB)
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

// Global variable untuk menyimpan status inisialisasi agar tidak dipanggil berulang
private var isFirebaseInitialized = false

actual fun initFirebase() {
    if (isFirebaseInitialized) return
    
    try {
        // Initialize Firebase Platform for JVM/Desktop
        FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
            private val prefs = java.util.prefs.Preferences.userRoot().node("com.chiko0085.testgym")
            override fun store(key: String, value: String) {
                prefs.put(key, value)
            }
            override fun retrieve(key: String): String? = prefs.get(key, null)
            override fun clear(key: String) {
                prefs.remove(key)
            }
            override fun log(msg: String) = println("FIREBASE: $msg")
        })

        val options = FirebaseOptions(
            applicationId = "1:713442868886:android:40c655c252db2fda9bccb7",
            apiKey = "AIzaSyCvGwpy_N4ZgEPfiPaUdvreCbcnrgpK-CE",
            projectId = "youth-gym",
            storageBucket = "youth-gym.firebasestorage.app"
        )
        
        // Pada Desktop/JVM, gitlive-firebase butuh 'context' (stubbed Application) 
        // untuk menghindari error casting "null cannot be cast to Context"
        Firebase.initialize(Application(), options)
        
        isFirebaseInitialized = true
        println("INFO: Firebase Desktop initialized successfully.")
    } catch (e: Exception) {
        if (e.message?.contains("already exists") == true || e.message?.contains("initialized") == true) {
            isFirebaseInitialized = true
            println("INFO: Firebase already initialized.")
        } else {
            println("ERROR: Firebase Desktop init failed: ${e.message}")
            e.printStackTrace()
        }
    }
}

actual fun createStorageData(bytes: ByteArray): Data = TODO("Firebase Storage not supported on JVM yet")

actual fun isStorageSupported(): Boolean = false
