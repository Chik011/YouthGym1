package com.chiko0085.testgym

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher

@Composable
actual fun rememberImagePicker(onResult: (ByteArray) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val launcher = rememberImagePickerLauncher(
        selectionMode = com.preat.peekaboo.image.picker.SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let { onResult(it) }
        }
    )
    return { launcher.launch() }
}