package com.works.naval

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.basicAuth
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType

private val httpClient = HttpClient()

suspend fun login(usuario: String, password: String): Boolean {
    val response = httpClient.post("http://localhost:8080/api/auth/login") {
        contentType(ContentType.Application.Json)
        setBody("""{"usuario":"${usuario.escapeJson()}","password":"${password.escapeJson()}"}""")
    }

    return response.status.value == 200 && response.bodyAsText().contains("\"correcto\":true")
}

 suspend fun fetchOperarios(usuario: String, password: String): List<String> {
    val response = httpClient.get("http://localhost:8080/api/operarios") {
        basicAuth(usuario, password)
    }

    if (response.status.value != 200) {
        return emptyList()
    }

    val body = response.bodyAsText()
    val operarioRegex = Regex(
        """\{[^{}]*"nombre"\s*:\s*"([^"]*)"[^{}]*"apellidos"\s*:\s*"([^"]*)"[^{}]*\}"""
    )

    return operarioRegex.findAll(body).map { match ->
        "${match.groupValues[1]} ${match.groupValues[2]}"
    }.toList()
}

private fun String.escapeJson(): String =
    replace("\\", "\\\\")
        .replace("\"", "\\\"")
