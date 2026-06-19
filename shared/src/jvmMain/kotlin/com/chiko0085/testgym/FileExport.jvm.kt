// FileExport.jvm.kt - Ekspor file platform JVM/Desktop

package com.chiko0085.testgym

import java.io.File

// Ekspor data csv ke direktori Downloads
actual fun exportToExcel(csvContent: String) {
    try {
        val userHome = System.getProperty("user.home")
        val downloadDir = File(userHome, "Downloads")
        
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        
        val file = File(downloadDir, "Laporan_Pendapatan_YouthGym.xls")
        file.writeText(csvContent)
        println("SUKSES EKSPOR: File tersimpan di ${file.absolutePath}")
    } catch (e: Exception) {
        println("GAGAL EKSPOR: ${e.message}")
        e.printStackTrace()
    }
}
