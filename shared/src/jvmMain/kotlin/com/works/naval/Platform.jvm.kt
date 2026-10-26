package com.works.naval

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual val defaultApiBaseUrl: String = "http://localhost:8080"