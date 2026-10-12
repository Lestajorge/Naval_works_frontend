package com.works.naval

data class TrabajoItem(
    val id: Long,
    val isometricoId: Long,
    val codigoIsometrico: String,
    val bloque: String,
    val subsistema: String,
    val estado: EstadoTrabajo,
)
