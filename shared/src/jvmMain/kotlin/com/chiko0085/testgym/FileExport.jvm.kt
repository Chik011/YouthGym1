package com.chiko0085.testgym

import java.io.File

actual fun exportToExcel(csvContent: String) {
    try {
        val userHome = System.getProperty("user.home")
        val file = File(userHome, "Laporan_Member_YouthGym.xls")

        file.writeText(csvContent)
        println("SUKSES EKSPOR: File tersimpan di ${file.absolutePath}")
    } catch (e: Exception) {
        println("GAGAL EKSPOR: ${e.message}")
    }
}