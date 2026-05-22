package com.chiko0085.testgym

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Youth Gym",
    ) {
        App()
    }
}