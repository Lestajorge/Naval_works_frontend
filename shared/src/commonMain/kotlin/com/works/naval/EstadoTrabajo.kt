package com.works.naval

enum class EstadoTrabajo(val descripcion: String) {
    PENDIENTE("Pendiente de inicio"),
    EN_MONTAJE("En proceso de montaje"),
    MONTADO("Trabajo montado"),
    SOLDADO("Trabajo soldado"),
    REVISADO("Trabajo revisado")
}