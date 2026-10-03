package com.works.naval

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "NavalWorks_Frontend",
    ) {
        App()
    }
}