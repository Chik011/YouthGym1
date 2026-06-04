package com.chiko0085.testgym

import android.os.Build
// Tambahan import untuk membaca format tanggal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun openWebLink(url: String) {
    println("Membuka link di Android: $url")
}

actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

// --- TAMBAHAN BARU: Pekerja untuk mengubah angka menjadi teks kalender di Android ---
actual fun formatEpochToDate(millis: Long): String {
    if (millis <= 0L) return "-"
    // Menampilkan format kalender versi Indonesia (Contoh: 23 Mei 2026)
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(millis))
}

actual fun initFirebase() {
    // Di Android sudah otomatis via google-services.json
}