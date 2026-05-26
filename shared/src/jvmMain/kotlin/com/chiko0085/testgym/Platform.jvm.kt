package com.chiko0085.testgym

import java.awt.Desktop
import java.net.URI
// Tambahan import untuk membaca format tanggal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun openWebLink(url: String) {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(url))
    }
}

actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

// --- TAMBAHAN BARU: Pekerja untuk mengubah angka menjadi teks kalender di Laptop/Desktop ---
actual fun formatEpochToDate(millis: Long): String {
    if (millis <= 0L) return "-"
    // Menampilkan format kalender versi Indonesia
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(millis))
}