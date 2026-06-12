package com.chiko0085.testgym

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    try {
        initFirebase()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Youth Gym Management",
        ) {
            App()
        }
    }
}