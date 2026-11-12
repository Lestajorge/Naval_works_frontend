package com.works.naval.domain.model

enum class EstadoTrabajo(
    val descripcion: String,
    val descripcionGallega: String,
) {
    PENDIENTE("Pendiente", "Pendente"),
    EN_EJECUCION("En proceso", "En proceso"),
    REVISADO("Revisión", "Revisión"),
    ACEPTADO("Aceptado", "Aceptado")
}