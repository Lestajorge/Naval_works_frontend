package com.works.naval

enum class EstadoTrabajo(val descripcion: String) {
    PENDIENTE("Pendiente de inicio"),
    EN_EJECUCION("En proceso de montaje/soldadura"),
    REVISADO("Pendiente de inspección de calidad"),
    ACEPTADO("Trabajo validado y finalizado")
}