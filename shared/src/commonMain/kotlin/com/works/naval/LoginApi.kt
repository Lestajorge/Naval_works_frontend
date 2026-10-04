package com.works.naval

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters

private val httpClient = HttpClient {
    followRedirects = false
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }
}

suspend fun login(usuario: String, password: String): Boolean {
    val loginPage = httpClient.get("$apiBaseUrl/login")
    val csrfMatch = csrfInputRegex.find(loginPage.bodyAsText())
        ?: error("El backend no devolvió el token CSRF del formulario de acceso")

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

    val redirect = response.headers[HttpHeaders.Location]
    return response.status.value in 300..399 &&
        redirect?.substringBefore('?')?.endsWith("/dashboard") == true
}

suspend fun fetchOperarios(): List<String> {
    val response = httpClient.get("$apiBaseUrl/api/operarios")

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

private val csrfInputRegex =
    Regex("""<input[^>]*name="([^"]+)"[^>]*value="([^"]+)"[^>]*>""", RegexOption.IGNORE_CASE)
