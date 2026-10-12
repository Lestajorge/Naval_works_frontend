package com.works.naval


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrabajosSection(
    listaTrabajos: List<TrabajoItem>,
    onSelectTrabajo: (TrabajoItem) -> Unit,
    onEstadoChange: (Long, EstadoTrabajo) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    // Filtrado en tiempo real por ID de isométrico o por Bloque (ej: B322)
    val trabajosFiltrados = listaTrabajos.filter {
        it.codigoIsometrico.contains(searchQuery, ignoreCase = true) ||
                it.bloque.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Buscador superior
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por bloque o ID (ej. B322)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (trabajosFiltrados.isEmpty()) {
                item {
                    Text(
                        text = if (listaTrabajos.isEmpty()) {
                            "No hay trabajos registrados."
                        } else {
                            "No se encontraron trabajos."
                        },
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
            items(trabajosFiltrados) { trabajo ->
                IsometricoCard(
                    item = trabajo,
                    onClick = { onSelectTrabajo(trabajo) },
                    onEstadoChange = { nuevoEstado ->
                        onEstadoChange(trabajo.id, nuevoEstado)
                    },
                )
            }
        }
    }
}