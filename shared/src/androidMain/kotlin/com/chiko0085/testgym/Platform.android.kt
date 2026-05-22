package com.chiko0085.testgym

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun openWebLink(url: String) {
    println("Membuka link di Android: $url")
}