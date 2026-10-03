package com.works.naval

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform