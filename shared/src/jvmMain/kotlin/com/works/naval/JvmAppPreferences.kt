package com.works.naval

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

class JvmAppPreferences(
    private val databaseFile: Path = Paths.get(
        System.getProperty("user.home"),
        ".navalworks",
        "preferences.db",
    ),
) : AppPreferences {
    override suspend fun readUiLanguage(): UiLanguage? = withContext(Dispatchers.IO) {
        withConnection { connection ->
            connection.prepareStatement(
                "SELECT value FROM preferences WHERE key = ?",
            ).use { statement ->
                statement.setString(1, UI_LANGUAGE_KEY)
                statement.executeQuery().use { result ->
                    if (result.next()) {
                        result.getString(1)?.let { storedValue ->
                            UiLanguage.entries.firstOrNull { it.name == storedValue }
                        }
                    } else {
                        null
                    }
                }
            }
        }
    }

    override suspend fun writeUiLanguage(language: UiLanguage) {
        withContext(Dispatchers.IO) {
            withConnection { connection ->
                connection.prepareStatement(
                    """
                    INSERT INTO preferences (key, value) VALUES (?, ?)
                    ON CONFLICT(key) DO UPDATE SET value = excluded.value
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, UI_LANGUAGE_KEY)
                    statement.setString(2, language.name)
                    statement.executeUpdate()
                }
            }
        }
    }

    override suspend fun readServerConnectionSettings(): ServerConnectionSettings? =
        withContext(Dispatchers.IO) {
            withConnection { connection ->
                val values = mutableMapOf<String, String>()
                connection.prepareStatement(
                    """
                    SELECT key, value FROM preferences
                    WHERE key IN ('server_url', 'server_username', 'server_password')
                    """.trimIndent(),
                ).use { statement ->
                    statement.executeQuery().use { result ->
                        while (result.next()) {
                            values[result.getString("key")] = result.getString("value")
                        }
                    }
                }
                if (values.isEmpty()) {
                    null
                } else {
                    ServerConnectionSettings(
                        url = values["server_url"] ?: defaultApiBaseUrl,
                        username = values["server_username"].orEmpty(),
                        password = values["server_password"].orEmpty(),
                    )
                }
            }
        }

    override suspend fun writeServerConnectionSettings(settings: ServerConnectionSettings) {
        withContext(Dispatchers.IO) {
            withConnection { connection ->
                connection.autoCommit = false
                connection.prepareStatement(
                    """
                    INSERT INTO preferences (key, value) VALUES (?, ?)
                    ON CONFLICT(key) DO UPDATE SET value = excluded.value
                    """.trimIndent(),
                ).use { statement ->
                    listOf(
                        "server_url" to settings.url,
                        "server_username" to settings.username,
                        "server_password" to settings.password,
                    ).forEach { (key, value) ->
                        statement.setString(1, key)
                        statement.setString(2, value)
                        statement.addBatch()
                    }
                    statement.executeBatch()
                }
                connection.commit()
            }
        }
    }

    private fun <T> withConnection(block: (Connection) -> T): T {
        Files.createDirectories(databaseFile.toAbsolutePath().parent)
        return DriverManager.getConnection("jdbc:sqlite:$databaseFile").use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    "CREATE TABLE IF NOT EXISTS preferences (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)",
                )
            }
            block(connection)
        }
    }

    private companion object {
        const val UI_LANGUAGE_KEY = "ui_language"
    }
}
