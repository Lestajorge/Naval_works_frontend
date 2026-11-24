package com.works.naval.data.preferences

import com.works.naval.core.localization.UiLanguage

interface AppPreferences {
    suspend fun readUiLanguage(): UiLanguage?

    suspend fun writeUiLanguage(language: UiLanguage)

    suspend fun readServerConnectionSettings(): ServerConnectionSettings?

    suspend fun writeServerConnectionSettings(settings: ServerConnectionSettings)

    suspend fun readIncidencias(): List<IncidenciaRecord>?

    suspend fun writeIncidencias(incidencias: List<IncidenciaRecord>)
}
