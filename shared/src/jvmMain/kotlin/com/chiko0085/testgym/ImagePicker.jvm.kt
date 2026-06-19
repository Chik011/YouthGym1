// ImagePicker.jvm.kt - Picker gambar platform JVM/Desktop

package com.chiko0085.testgym

import androidx.compose.runtime.Composable
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

// Picker gambar native desktop
@Composable
actual fun rememberImagePicker(onResult: (ByteArray) -> Unit): () -> Unit {
    return {
        try {
            val fileDialog = FileDialog(null as Frame?, "Pilih Foto", FileDialog.LOAD)
            fileDialog.isVisible = true

            val file = fileDialog.file
            val directory = fileDialog.directory

            if (file != null && directory != null) {
                val selectedFile = File(directory, file)
                onResult(selectedFile.readBytes())
            }
        } catch (e: Exception) {
            println("DEBUG: Gagal membuka file explorer laptop: ${e.message}")
        }
    }
}
