package com.works.naval

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import java.nio.file.Files

class SharedLogicDesktopTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun uiLanguagePreferencePersistsInSqlite() = runBlocking {
        val directory = Files.createTempDirectory("navalworks-preferences-test")
        val databaseFile = directory.resolve("preferences.db")
        try {
            val preferences = JvmAppPreferences(databaseFile)
            assertNull(preferences.readUiLanguage())

            preferences.writeUiLanguage(UiLanguage.GALICIAN)

            assertEquals(
                UiLanguage.GALICIAN,
                JvmAppPreferences(databaseFile).readUiLanguage(),
            )
        } finally {
            Files.deleteIfExists(databaseFile)
            Files.deleteIfExists(directory)
        }
    }

    @Test
    fun serverConnectionSettingsPersistAndCanBeUpdatedInSqlite() = runBlocking {
        val directory = Files.createTempDirectory("navalworks-connection-test")
        val databaseFile = directory.resolve("preferences.db")
        try {
            val preferences = JvmAppPreferences(databaseFile)
            assertNull(preferences.readServerConnectionSettings())
            val settings = ServerConnectionSettings(
                url = "https://navalworks.example/api",
                username = "supervisor",
                password = "secret",
            )
            preferences.writeServerConnectionSettings(settings)

            assertEquals(settings, JvmAppPreferences(databaseFile).readServerConnectionSettings())

            val updatedSettings = settings.copy(password = "updated-secret")
            preferences.writeServerConnectionSettings(updatedSettings)
            assertEquals(
                updatedSettings,
                JvmAppPreferences(databaseFile).readServerConnectionSettings(),
            )
        } finally {
            Files.deleteIfExists(databaseFile)
            Files.deleteIfExists(directory)
        }
    }
}