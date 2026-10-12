package com.works.naval

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

private val httpClient = HttpClient {
    followRedirects = false
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }
}
private val csrfInputRegex =
    Regex("""<input[^>]*name="([^"]+)"[^>]*value="([^"]+)"[^>]*>""", RegexOption.IGNORE_CASE)


// OBTENEMOS EL TOKEN RECIBIDO
suspend fun login(usuario: String, password: String): Boolean {
    val loginPage = httpClient.get("$apiBaseUrl/login")
    val csrfMatch = csrfInputRegex.find(loginPage.bodyAsText())
        ?: error("El backend no devolvió el token CSRF del formulario de acceso")

// INTRODUCIMOS LAS CREDENCIALES CON EL TOKEN RECIBIDO

    val response = httpClient.post("$apiBaseUrl/login") {
        setBody(
            FormDataContent(
                Parameters.build {
                    append("username", usuario)
                    append("password", password)
                    append(csrfMatch.groupValues[1], csrfMatch.groupValues[2])
                },
            ),
        )
    }

    /* OBTENEMOS LA CABECERA Y EL ESTADO , SI ESTA ENTRE 300 Y 400 Y TERMINA LA URL EN
    /DASHBOARD DEVOLVEREMOS TRUE
    */
    val redirect = response.headers[HttpHeaders.Location]

    return response.status.value in 300..399 &&
       // Utilizamos ?. para evitar los NUllPointerException

            redirect?.substringBefore('?')?.endsWith("/dashboard") == true
}

// FUNCION PARA BUSCAR OPERARIOS EN LA BASE DE DATOS AL PUSAR ASIGNAR NUEVO TRABAJO
suspend fun fetchOperarios(): List<String> {
    val response = httpClient.get("$apiBaseUrl/api/operarios")

    //SI EL RESPONSE NO ES OK , DEVOLVEMOS LISTA VACIA
    if (response.status.value != 200) {
        return emptyList()
    }
// DEFINIMOS EL PATRON A BUSCAR EN EL RESPONSE RECIBIDO
    val body = response.bodyAsText()
    val operarioRegex = Regex(
        """\{[^{}]*"nombre"\s*:\s*"([^"]*)"[^{}]*"apellidos"\s*:\s*"([^"]*)"[^{}]*\}"""
    )

    //BUSCAMOS  Y AGRUPAMOS POR NOMBRE Y APELLIDOS Y DEVOLVEMOS UNA LISTA
    return operarioRegex.findAll(body).map { match ->
        "${match.groupValues[1]} ${match.groupValues[2]}"
    }.toList()
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
    check(response.status.value == 200) {
        "El servidor respondió ${response.status.value} al consultar $path"
    }
    return Json.parseToJsonElement(response.bodyAsText()).jsonArray
}

// LEE UN TEXTO DEL JSON
private fun JsonObject.requiredString(field: String): String =
    this[field]?.jsonPrimitive?.contentOrNull
        ?: error("Falta el campo '$field' en la respuesta del servidor")

// LEE EL NUMERO DEL OBJETO JSON
private fun JsonObject.requiredLong(field: String): Long =
    this[field]?.jsonPrimitive?.longOrNull
        ?: error("Falta el campo '$field' en la respuesta del servidor")

// GUARDA LOS DATOS DEL ISOMETRICO QUE NECESITA LA LISTA DE TRABAJOS
private data class IsometricoData(
    val codigo: String,
    val bloque: String,
    val lineaTuberia: String,
)
