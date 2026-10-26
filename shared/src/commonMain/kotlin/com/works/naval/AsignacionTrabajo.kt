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

data class BloqueOption(
    val id: Long,
    val codigo: String,
    val zona: String,
)

enum class EstadoIsometrico {
    BORRADOR,
    PUBLICADO,
    ARCHIVADO,
}

data class IsometricoAdministrado(
    val id: Long,
    val codigo: String,
    val bloqueId: Long,
    val lineaTuberia: String,
    val revision: String,
    val estado: EstadoIsometrico,
)

data class PdfFileSelection(
    val fileName: String,
    val bytes: ByteArray,
)
