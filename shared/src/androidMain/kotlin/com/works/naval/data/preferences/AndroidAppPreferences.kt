package com.works.naval.data.preferences

import com.works.naval.core.localization.UiLanguage
import com.works.naval.platform.defaultApiBaseUrl

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidAppPreferences(context: Context) : AppPreferences {
    private val database = object : SQLiteOpenHelper(
        context.applicationContext,
        DATABASE_NAME,
        null,
        DATABASE_VERSION,
    ) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_PREFERENCES_TABLE)
            db.execSQL(CREATE_INCIDENCIAS_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2) {
                db.execSQL(CREATE_INCIDENCIAS_TABLE)
            }
        }
    }

    override suspend fun readUiLanguage(): UiLanguage? = withContext(Dispatchers.IO) {
        database.readableDatabase.rawQuery(
            "SELECT value FROM preferences WHERE key = ?",
            arrayOf(UI_LANGUAGE_KEY),
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                null
            } else {
                cursor.getString(0)?.let { storedValue ->
                    UiLanguage.entries.firstOrNull { it.name == storedValue }
                }
            }
        }
    }

    override suspend fun writeUiLanguage(language: UiLanguage) {
        withContext(Dispatchers.IO) {
            writePreference(database.writableDatabase, UI_LANGUAGE_KEY, language.name)
        }
    }

    override suspend fun readServerConnectionSettings(): ServerConnectionSettings? =
        withContext(Dispatchers.IO) {
            val database = database.readableDatabase
            val settings = mapOf(
                "server_url" to readPreference(database, "server_url"),
                "server_username" to readPreference(database, "server_username"),
                "server_password" to readPreference(database, "server_password"),
            )
            if (settings.values.all { it == null }) {
                null
            } else {
                ServerConnectionSettings(
                    url = settings["server_url"] ?: defaultApiBaseUrl,
                    username = settings["server_username"].orEmpty(),
                    password = settings["server_password"].orEmpty(),
                )
            }
        }

    override suspend fun writeServerConnectionSettings(settings: ServerConnectionSettings) {
        withContext(Dispatchers.IO) {
            val database = database.writableDatabase
            database.beginTransaction()
            try {
                listOf(
                    "server_url" to settings.url,
                    "server_username" to settings.username,
                    "server_password" to settings.password,
                ).forEach { (key, value) ->
                    writePreference(database, key, value)
                }
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
            }
        }
    }

    override suspend fun readIncidencias(): List<IncidenciaRecord>? =
        withContext(Dispatchers.IO) {
            val database = database.readableDatabase
            if (readPreference(database, INCIDENCIAS_INITIALIZED_KEY) != "true") {
                null
            } else {
                val incidencias = mutableListOf<IncidenciaRecord>()
                database.rawQuery(
                    "SELECT nombre, descripcion FROM incidencias ORDER BY id",
                    emptyArray(),
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        incidencias += IncidenciaRecord(
                            nombre = cursor.getString(0).orEmpty(),
                            descripcion = cursor.getString(1).orEmpty(),
                        )
                    }
                }
                incidencias
            }
        }

    override suspend fun writeIncidencias(incidencias: List<IncidenciaRecord>) {
        withContext(Dispatchers.IO) {
            val database = database.writableDatabase
            database.beginTransaction()
            try {
                database.delete("incidencias", null, null)
                incidencias.forEach { incidencia ->
                    val values = ContentValues().apply {
                        put("nombre", incidencia.nombre)
                        put("descripcion", incidencia.descripcion)
                    }
                    check(
                        database.insert("incidencias", null, values) != -1L,
                    ) { "No se pudo guardar la incidencia en SQLite" }
                }
                writePreference(database, INCIDENCIAS_INITIALIZED_KEY, "true")
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
            }
        }
    }

    private fun writePreference(database: SQLiteDatabase, key: String, value: String) {
        val values = ContentValues().apply {
            put("key", key)
            put("value", value)
        }
        val rowId = database.insertWithOnConflict(
            "preferences",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
        check(rowId != -1L) { "No se pudo guardar la preferencia '$key' en SQLite" }
    }

    private fun readPreference(database: SQLiteDatabase, key: String): String? =
        database.rawQuery(
            "SELECT value FROM preferences WHERE key = ?",
            arrayOf(key),
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

    private companion object {
        const val DATABASE_NAME = "navalworks_preferences.db"
        const val DATABASE_VERSION = 2
        const val UI_LANGUAGE_KEY = "ui_language"
        const val INCIDENCIAS_INITIALIZED_KEY = "incidencias_initialized"
        const val CREATE_PREFERENCES_TABLE =
            "CREATE TABLE IF NOT EXISTS preferences (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)"
        const val CREATE_INCIDENCIAS_TABLE =
            "CREATE TABLE IF NOT EXISTS incidencias (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, descripcion TEXT NOT NULL)"
    }
}
