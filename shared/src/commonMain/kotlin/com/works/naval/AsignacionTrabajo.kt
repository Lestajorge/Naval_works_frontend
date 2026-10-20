package com.works.naval

data class OperarioOption(
    val id: Long,
    val nombre: String,
    val apellidos: String,
    val especialista: String,
) {
    val nombreCompleto: String
        get() = "$nombre $apellidos"
}

data class IsometricoOption(
    val id: Long,
    val codigo: String,
    val bloqueId: Long,
)
