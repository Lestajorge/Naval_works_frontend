package com.works.naval

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun TrabajosScreen() {
    var selectedIsometrico by remember { mutableStateOf<IsometricoItem?>(null) }
    var listaIsometricos by remember { mutableStateOf(mockIsometricos) }

    if (selectedIsometrico == null) {
        TrabajosSection(
            listaIsometricos = listaIsometricos,
            onSelectIsometrico = { item ->
                selectedIsometrico = item
            },
            onEstadoChange = { id, nuevoEstado ->
                listaIsometricos = listaIsometricos.map { item ->
                    if (item.id == id) item.copy(estado = nuevoEstado) else item
                }
            },
        )
    } else {
        IsometricoDetalleVisor(
            item = listaIsometricos.firstOrNull { it.id == selectedIsometrico!!.id }
                ?: selectedIsometrico!!,
            onVolver = { selectedIsometrico = null }
        )
    }
}