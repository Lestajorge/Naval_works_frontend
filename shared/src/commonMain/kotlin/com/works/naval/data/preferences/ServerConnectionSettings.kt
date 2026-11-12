package com.works.naval.data.preferences

import com.works.naval.platform.defaultApiBaseUrl

data class ServerConnectionSettings(
    val url: String,
    val username: String,
    val password: String,
) {
    companion object {
        fun defaults(): ServerConnectionSettings =
            ServerConnectionSettings(defaultApiBaseUrl, username = "", password = "")
    }
}
