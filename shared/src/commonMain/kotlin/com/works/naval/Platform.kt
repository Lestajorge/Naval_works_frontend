package com.works.naval

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect val defaultApiBaseUrl: String

var apiBaseUrl: String = defaultApiBaseUrl
    private set

fun configureApiBaseUrl(url: String) {
    apiBaseUrl = url.trim().trimEnd('/')
}