package com.chiko0085.testgym

import androidx.compose.runtime.Composable

/**
 * Fungsi expect untuk mendeteksi picker foto profil secara cross-platform.
 * Mengembalikan lambda () -> Unit untuk memicu pembukaan galeri/file explorer.
 */
@Composable
expect fun rememberImagePicker(onResult: (ByteArray) -> Unit): () -> Unit