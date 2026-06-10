package com.chiko0085.testgym

import androidx.compose.runtime.Composable
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun rememberImagePicker(onResult: (ByteArray) -> Unit): () -> Unit {
    return {
        try {
            // Membuka File Dialog bawaan Windows / MacOS / Linux secara native
            val fileDialog = FileDialog(null as Frame?, "Pilih Foto Profil Coach", FileDialog.LOAD)
            fileDialog.isVisible = true

            val file = fileDialog.file
            val directory = fileDialog.directory

            if (file != null && directory != null) {
                val selectedFile = File(directory, file)
                // Membaca file gambar lokal di laptop menjadi ByteArray lalu dikirim ke Firebase
                onResult(selectedFile.readBytes())
            }
        } catch (e: Exception) {
            println("DEBUG: Gagal membuka file explorer laptop: ${e.message}")
        }
    }
}