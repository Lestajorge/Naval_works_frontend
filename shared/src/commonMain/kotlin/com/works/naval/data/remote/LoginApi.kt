package com.works.naval.data.remote

import com.works.naval.domain.model.*
import com.works.naval.platform.*

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.request.get
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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

class IsometricoYaAsignadoException : Exception()

class IsometricoAsociadoATrabajoException : Exception()

private var loginCredentials: BasicAuthCredentials? = null
private var regularLoginCredentials: BasicAuthCredentials? = null

data class AuthenticatedUser(
    val usuario: String,
    val rol: String,
)

private val httpClient = HttpClient {
    install(Auth) {
        basic {
            credentials { loginCredentials }
            sendWithoutRequest { true }
        }
    }
}
private val authHttpClient = HttpClient()

suspend fun login(usuario: String, password: String): AuthenticatedUser? {
    val authenticatedUser = try {
        authenticate(usuario, password)
    } catch (exception: Exception) {
        logoutSession()
        throw exception
    }
    if (authenticatedUser == null) {
        logoutSession()
        return null
    }
    val credentials = BasicAuthCredentials(usuario, password)
    regularLoginCredentials = credentials
    loginCredentials = credentials
    return authenticatedUser
}

suspend fun loginAdministrador(usuario: String, password: String): String? {
    val authenticatedUser = authenticate(usuario, password) ?: return null
    if (!authenticatedUser.rol.equals("ADMINISTRADOR", ignoreCase = true)) return null

    loginCredentials = BasicAuthCredentials(usuario, password)
    return authenticatedUser.usuario
}

private suspend fun authenticate(usuario: String, password: String): AuthenticatedUser? {
    val requestBody = buildJsonObject {
        put("usuario", usuario)
        put("password", password)
    }
    val response = authHttpClient.post("$apiBaseUrl/api/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(requestBody.toString())
    }

    if (response.status.value == 401 || response.status.value == 403) return null
    if (response.status.value != 200) {
        val details = response.bodyAsText().take(500)
        error(
            "El servidor respondió ${response.status.value} al validar las credenciales" +
                if (details.isBlank()) "" else ": $details"
        )
    }

    val user = Json.parseToJsonElement(response.bodyAsText()).jsonObject
    return AuthenticatedUser(
        usuario = user.requiredString("usuario"),
        rol = user.requiredString("rol"),
    )
}

fun logoutAdministrador() {
    loginCredentials = regularLoginCredentials
}

fun logoutSession() {
    loginCredentials = null
    regularLoginCredentials = null
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

suspend fun createOperario(
    nombre: String,
    apellidos: String,
    especialista: String,
    administrador: Boolean = false,
    usuario: String? = null,
    password: String? = null,
) {
    val hasUsuario = !usuario.isNullOrBlank()
    val hasPassword = !password.isNullOrBlank()
    require(hasUsuario == hasPassword) {
        "El usuario y la contraseña deben proporcionarse juntos"
    }
    require(!administrador || hasUsuario) {
        "El administrador requiere nombre de usuario y contraseña"
    }
    val response = httpClient.post("$apiBaseUrl/api/operarios") {
        contentType(ContentType.Application.Json)
        setBody(
            buildJsonObject {
                put("nombre", nombre)
                put("apellidos", apellidos)
                put("especialista", especialista.trim().uppercase())
                put("administrador", if (administrador) 1 else 0)
                if (hasUsuario) {
                    put("usuario", usuario.trim())
                    put("password", password.orEmpty())
                }
            }.toString(),
        )
    }

    if (response.status.value == 409) {
        throw IsometricoYaAsignadoException()
    }
    if (response.status.value !in 200..299) {
        val details = response.bodyAsText().take(500)
        error(
            "El servidor respondió ${response.status.value} al crear el operario" +
                if (details.isBlank()) "" else ": $details"
        )
    }
}

suspend fun updateOperario(id: Long, nombre: String, apellidos: String, especialista: String) {
    val response = httpClient.put("$apiBaseUrl/api/operarios/$id") {
        contentType(ContentType.Application.Json)
        setBody(operarioJson(nombre, apellidos, especialista).toString())
    }

    if (response.status.value !in 200..299) {
        val details = response.bodyAsText().take(500)
        error(
            "El servidor respondió ${response.status.value} al modificar el operario $id" +
                if (details.isBlank()) "" else ": $details"
        )
    }
}

suspend fun deleteOperario(id: Long) {
    fetchTrabajos()
        .filter { id in it.operarioIds }
        .forEach { trabajo ->
            updateTrabajo(trabajo.copy(operarioIds = trabajo.operarioIds.filterNot { it == id }))
        }

    val response = httpClient.delete("$apiBaseUrl/api/operarios/$id")
    if (response.status.value !in 200..299) {
        val details = response.bodyAsText().take(500)
        error(
            "El servidor respondió ${response.status.value} al dar de baja el operario $id" +
                if (details.isBlank()) "" else ": $details"
        )
    }
}

private fun operarioJson(nombre: String, apellidos: String, especialista: String) =
    buildJsonObject {
        put("nombre", nombre)
        put("apellidos", apellidos)
        put("especialista", especialista.trim().uppercase())
    }

suspend fun fetchIsometricos(): List<IsometricoOption> =
    fetchJsonArray("/api/isometricos")
        .filter { element ->
            val status = element.jsonObject["estado"]?.jsonPrimitive?.contentOrNull
            status == null || status.equals(EstadoIsometrico.PUBLICADO.name, ignoreCase = true)
        }
        .map { element ->
        val isometrico = element.jsonObject
        IsometricoOption(
            id = isometrico.requiredLong("id"),
            codigo = isometrico.requiredString("codigoIsometrico"),
            bloqueId = isometrico.requiredLong("bloqueId"),
        )
    }

suspend fun fetchIsometricosAdministracion(): List<IsometricoAdministrado> =
    fetchJsonArray("/api/isometricos/admin").map { element ->
        val isometrico = element.jsonObject
        val estadoNombre = isometrico.requiredString("estado")
        val estado = EstadoIsometrico.entries.firstOrNull {
            it.name.equals(estadoNombre, ignoreCase = true)
        }
            ?: error("Estado de isométrico no reconocido: $estadoNombre")
        IsometricoAdministrado(
            id = isometrico.requiredLong("id"),
            codigo = isometrico.requiredString("codigoIsometrico"),
            bloqueId = isometrico.requiredLong("bloqueId"),
            estado = estado,
        )
    }

suspend fun createIsometrico(
    codigo: String,
    bloqueId: Long,
    pdf: PdfFileSelection,
) {
    val safeFileName = pdf.fileName
        .substringAfterLast('/')
        .substringAfterLast('\\')
        .map { if (it.isLetterOrDigit() || it in "._-") it else '_' }
        .joinToString("")
        .ifBlank { "isometrico.pdf" }
    val response = httpClient.submitFormWithBinaryData(
        url = "$apiBaseUrl/api/isometricos",
        formData = formData {
            append("codigoIsometrico", codigo)
            append("bloqueId", bloqueId.toString())
            append(
                "file",
                pdf.bytes,
                Headers.build {
                    append(HttpHeaders.ContentType, ContentType.Application.Pdf.toString())
                    append(HttpHeaders.ContentDisposition, "filename=\"$safeFileName\"")
                },
            )
        },
    )
    requireSuccessful(response, "crear el isométrico y subir su PDF")
}

suspend fun deleteIsometrico(isometricoId: Long) {
    val response = httpClient.delete("$apiBaseUrl/api/isometricos/$isometricoId")
    if (response.status.value == 409) {
        throw IsometricoAsociadoATrabajoException()
    }
    requireSuccessful(response, "eliminar el isométrico $isometricoId")
}

suspend fun fetchBloques(): List<BloqueOption> =
    fetchJsonArray("/api/bloques").map { element ->
        val bloque = element.jsonObject
        BloqueOption(
            id = bloque.requiredLong("id"),
            codigo = bloque.requiredString("codigoBloque"),
            zona = bloque.requiredString("zona"),
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
    val bloques = fetchBloques().associateBy { it.id }

    // GUARDA LOS DATOS DE CADA ISOMETRICO Y BUSCA EL CODIGO DE SU BLOQUE
    // Metodo associate de libreria estandar de Kotlin para cualquier coleccion
    val isometricos = fetchJsonArray("/api/isometricos").associate { element ->
        val isometrico = element.jsonObject
        val id = isometrico.requiredLong("id")
        id to IsometricoData(
            codigo = isometrico.requiredString("codigoIsometrico"),
            bloque = bloques[isometrico.requiredLong("bloqueId")]?.codigo
                ?: error("No se encontró el bloque del isométrico $id"),
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

suspend fun deleteTrabajo(trabajoId: Long) {
    val response = httpClient.delete("$apiBaseUrl/api/trabajos/$trabajoId")
    requireSuccessful(response, "eliminar el trabajo $trabajoId")
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

private suspend fun requireSuccessful(response: HttpResponse, operation: String) {
    if (response.status.value !in 200..299) {
        val details = response.bodyAsText().take(500)
        error(
            "El servidor respondió ${response.status.value} al $operation" +
                if (details.isBlank()) "" else ": $details",
        )
    }
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
)
