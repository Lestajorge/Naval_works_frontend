package com.works.naval

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

enum class UiLanguage {
    SPANISH,
    GALICIAN,
}

val LocalUiLanguage = staticCompositionLocalOf { UiLanguage.SPANISH }

fun UiLanguage.text(spanish: String, galician: String): String =
    if (this == UiLanguage.GALICIAN) galician else spanish

@Composable
fun uiText(spanish: String, galician: String): String =
    LocalUiLanguage.current.text(spanish, galician)
