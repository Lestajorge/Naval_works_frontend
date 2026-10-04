package com.works.naval

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect val apiBaseUrl: String