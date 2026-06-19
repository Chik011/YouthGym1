// ImagePicker.kt - Picker gambar cross-platform

package com.chiko0085.testgym

import androidx.compose.runtime.Composable

// Komponen picker gambar
@Composable
expect fun rememberImagePicker(onResult: (ByteArray) -> Unit): () -> Unit