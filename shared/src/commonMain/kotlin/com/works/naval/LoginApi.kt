package com.works.naval

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

private var loginCredentials: BasicAuthCredentials? = null

private val httpClient = HttpClient {
    install(Auth) {
        basic {
            credentials { loginCredentials }
            sendWithoutRequest { true }
        }
    }
}

suspend fun login(usuario: String, password: String): Boolean {
    loginCredentials = BasicAuthCredentials(usuario, password)
    val response = try {
        httpClient.get("$apiBaseUrl/api/bloques")
    } catch (exception: Exception) {
        loginCredentials = null
        throw exception
    }

    if (response.status.value == 401) {
        loginCredentials = null
        return false
    }

    if (response.status.value != 200) {
        loginCredentials = null
        error("El servidor respondió ${response.status.value} al comprobar las credenciales")
    }
    return true
}

suspend fun fetchOperarios(): List<OperarioOption> =
    fetchJsonArray("/api/operarios").map { element ->
        val operario = element.jsonObject
        OperarioOption(
            id = operario.requiredLong("id"),
            nombre = operario.requiredString("nombre"),
            apellidos = operario.requiredString("apellidos"),
            especialista = operario.requiredString("especialista"),
        )
    }

suspend fun fetchIsometricos(): List<IsometricoOption> =
    fetchJsonArray("/api/isometricos").map { element ->
        val isometrico = element.jsonObject
        IsometricoOption(
            id = isometrico.requiredLong("id"),
            codigo = isometrico.requiredString("codigoIsometrico"),
            bloqueId = isometrico.requiredLong("bloqueId"),
        )
    }

suspend fun createTrabajo(isometricoId: Long, operarioIds: Set<Long>) {
    val requestBody = buildJsonObject {
        put("estado", JsonPrimitive(EstadoTrabajo.PENDIENTE.name))
        put("isometricoId", JsonPrimitive(isometricoId))
        put(
            "operarioIds",
            buildJsonArray {
                operarioIds.forEach { add(JsonPrimitive(it)) }
            },
        )
    }
    val response = httpClient.post("$apiBaseUrl/api/trabajos") {
        contentType(ContentType.Application.Json)
        setBody(requestBody.toString())
    }

    if (response.status.value !in 200..299) {
        val details = response.bodyAsText().take(500)
        error(
            "El servidor respondió ${response.status.value} al crear el trabajo" +
                if (details.isBlank()) "" else ": $details"
        )
    }
}

// CARGA LOS TRABAJOS Y LOS COMPLETA CON LOS DATOS DE SUS ISOMETRICOS Y BLOQUES
suspend fun fetchTrabajos(): List<TrabajoItem> {
    // Relaciona cada bloque con su código.
    val bloques = fetchJsonArray("/api/bloques").associate { element ->
        val bloque = element.jsonObject
        // se convierte en clave par-valor el id y el codigo de bloque
        bloque.requiredLong("id") to bloque.requiredString("codigoBloque")
    }



    // GUARDA LOS DATOS DE CADA ISOMETRICO Y BUSCA EL CODIGO DE SU BLOQUE
    // Metodo associate de libreria estandar de Kotlin para cualquier coleccion
    val isometricos = fetchJsonArray("/api/isometricos").associate { element ->
        val isometrico = element.jsonObject
        val id = isometrico.requiredLong("id")
        id to IsometricoData(
            codigo = isometrico.requiredString("codigoIsometrico"),
            bloque = bloques[isometrico.requiredLong("bloqueId")]
                ?: error("No se encontró el bloque del isométrico $id"),
            lineaTuberia = isometrico.requiredString("lineaTuberia"),
        )
    }
    // COMBINA CADA TRABAJO CON SU ISOMETRICO Y CONVIERTE SU ESTADO.
    return fetchJsonArray("/api/trabajos").map { element ->
        val trabajo = element.jsonObject
        val trabajoId = trabajo.requiredLong("id")
        val isometricoId = trabajo.requiredLong("isometricoId")
        val isometrico = isometricos[isometricoId]
            ?: error("No se encontró el isométrico $isometricoId del trabajo $trabajoId")
        val estado = trabajo.requiredString("estado")

        TrabajoItem(
            id = trabajoId,
            isometricoId = isometricoId,
            codigoIsometrico = isometrico.codigo,
            bloque = isometrico.bloque,
            subsistema = isometrico.lineaTuberia,
            estado = EstadoTrabajo.values().firstOrNull { it.name == estado }
                ?: error("Estado de trabajo no reconocido: $estado"),
            operarioIds = trabajo.requiredLongList("operarioIds"),
        )
    }
}

// Guarda el nuevo estado conservando el resto de datos que requiere el PUT del backend.
suspend fun updateTrabajoEstado(trabajo: TrabajoItem, nuevoEstado: EstadoTrabajo) {
    updateTrabajo(trabajo.copy(estado = nuevoEstado))
}

suspend fun addOperariosToTrabajo(trabajo: TrabajoItem, nuevosOperarioIds: Set<Long>) {
    updateTrabajo(
        trabajo.copy(operarioIds = (trabajo.operarioIds + nuevosOperarioIds).distinct()),
    )
}

private suspend fun updateTrabajo(trabajo: TrabajoItem) {
    val requestBody = buildJsonObject {
        put("estado", trabajo.estado.name)
        put("isometricoId", JsonPrimitive(trabajo.isometricoId))
        put(
            "operarioIds",
            buildJsonArray {
                trabajo.operarioIds.forEach { add(JsonPrimitive(it)) }
            },
        )
    }
    val response = httpClient.put("$apiBaseUrl/api/trabajos/${trabajo.id}") {
        contentType(ContentType.Application.Json)
        setBody(requestBody.toString())
    }

    if (response.status.value !in 200..299) {
        val details = response.bodyAsText().take(500)
        error(
            "El servidor respondió ${response.status.value} al actualizar el trabajo " +
                "${trabajo.id}${if (details.isBlank()) "" else ": $details"}"
        )
    }
}


// DESCARGA EL ISOMETRICO DEL PDF BUSCADO
suspend fun fetchIsometricoPdf(isometricoId: Long): ByteArray {
    val response = httpClient.get("$apiBaseUrl/api/isometricos/$isometricoId/pdf")
    if (response.status.value != 200) {
        val details = response.bodyAsText().take(500)
        error(
            "El servidor respondió ${response.status.value} al cargar el PDF del isométrico " +
                "$isometricoId${if (details.isBlank()) "" else ": $details"}"
        )
    }
    return response.bodyAsBytes()
}
// SOLICITA UNA RUTA Y DEVUELVE SU RESPUESTA COMO UNA LISTA JSON
private suspend fun fetchJsonArray(path: String): JsonArray {
    val response = httpClient.get("$apiBaseUrl$path")
    val body = response.bodyAsText()
    if (response.status.value != 200) {
        val details = body.take(500)
        error(
            "El servidor respondió ${response.status.value} al consultar $path" +
                if (details.isBlank()) "" else ": $details"
        )
    }

    return Json.parseToJsonElement(body).jsonArray
}

// LEE UN TEXTO DEL JSON
private fun JsonObject.requiredString(field: String): String =
    this[field]?.jsonPrimitive?.contentOrNull
        ?: error("Falta el campo '$field' en la respuesta del servidor")

// LEE EL NUMERO DEL OBJETO JSON
private fun JsonObject.requiredLong(field: String): Long =
    this[field]?.jsonPrimitive?.longOrNull
        ?: error("Falta el campo '$field' en la respuesta del servidor")

private fun JsonObject.requiredLongList(field: String): List<Long> =
    this[field]?.jsonArray?.map { element ->
        element.jsonPrimitive.longOrNull
            ?: error("Valor no válido en el campo '$field' de la respuesta del servidor")
    } ?: error("Falta el campo '$field' en la respuesta del servidor")

// GUARDA LOS DATOS DEL ISOMETRICO QUE NECESITA LA LISTA DE TRABAJOS
private data class IsometricoData(
    val codigo: String,
    val bloque: String,
    val lineaTuberia: String,
)
