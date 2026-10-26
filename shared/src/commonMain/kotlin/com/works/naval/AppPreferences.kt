package com.works.naval

interface AppPreferences {
    suspend fun readUiLanguage(): UiLanguage?

    suspend fun writeUiLanguage(language: UiLanguage)

    suspend fun readServerConnectionSettings(): ServerConnectionSettings?

    suspend fun writeServerConnectionSettings(settings: ServerConnectionSettings)
}
