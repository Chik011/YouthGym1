package com.chiko0085.testgym

import java.io.File

actual fun exportToExcel(csvContent: String) {
    try {
        // Menggunakan folder Downloads standar Windows di profil user
        // Contoh: C:\Users\NamaUser\Downloads
        val userHome = System.getProperty("user.home")
        val downloadDir = File(userHome, "Downloads")
        
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        
        val file = File(downloadDir, "Laporan_Pendapatan_YouthGym.xls")

        // Menulis konten
        file.writeText(csvContent)
        println("SUKSES EKSPOR: File tersimpan di ${file.absolutePath}")
    } catch (e: Exception) {
        println("GAGAL EKSPOR: ${e.message}")
        e.printStackTrace()
    }
}