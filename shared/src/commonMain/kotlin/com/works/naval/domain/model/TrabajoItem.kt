package com.works.naval.domain.model

data class TrabajoItem(
    val id: Long,
    val isometricoId: Long,
    val codigoIsometrico: String,
    val bloque: String,
    val estado: EstadoTrabajo,
    val operarioIds: List<Long> = emptyList(),
)
