package com.works.naval



data class IsometricoItem(
    val id: String,          // Ej: "F110-B322-TUB-014"
    val bloque: String,      // Ej: "B322"
    val subsistema: String,  // Ej: "Refrigeración"
    val estado: EstadoTrabajo,
    val pdfUrl: String
)