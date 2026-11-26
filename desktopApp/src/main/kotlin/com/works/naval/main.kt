package com.works.naval

import com.works.naval.data.preferences.JvmAppPreferences
import com.works.naval.presentacion.app.App
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "NavalWorks_Frontend",
    ) {
        App(JvmAppPreferences())
    }
}