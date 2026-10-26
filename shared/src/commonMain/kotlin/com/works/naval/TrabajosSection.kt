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
    estadoUpdateError: String?,
    isUpdatingEstado: Boolean,
    updatingTrabajoId: Long?,
    isAddingOperarios: Boolean,
    addingOperariosTrabajoId: Long?,
    onAddOperarios: (TrabajoItem) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val query = searchQuery.trim()
    val trabajosFiltrados = listaTrabajos.filter {
        it.codigoIsometrico.contains(query, ignoreCase = true) ||
            it.bloque.contains(query, ignoreCase = true) ||
            it.estado.descripcion.contains(query, ignoreCase = true) ||
            it.estado.descripcionGallega.contains(query, ignoreCase = true) ||
            it.estado.name.replace('_', ' ').contains(query, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        // Buscador superior
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(uiText("Buscar por bloque, ID o estado...", "Buscar por bloque, ID ou estado...")) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = uiText("Buscar", "Buscar")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        estadoUpdateError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (trabajosFiltrados.isEmpty()) {
                item {
                    Text(
                        text = if (listaTrabajos.isEmpty()) {
                            uiText("No hay trabajos registrados.", "Non hai traballos rexistrados.")
                        } else {
                            uiText("No se encontraron trabajos.", "Non se atoparon traballos.")
                        },
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
            items(trabajosFiltrados) { trabajo ->
                IsometricoCard(
                    item = trabajo,
                    onClick = { onSelectTrabajo(trabajo) },
                    isUpdating = isUpdatingEstado || isAddingOperarios,
                    isSaving = trabajo.id == updatingTrabajoId,
                    isAddingOperarios =
                        isAddingOperarios && trabajo.id == addingOperariosTrabajoId,
                    onEstadoChange = { nuevoEstado ->
                        onEstadoChange(trabajo.id, nuevoEstado)
                    },
                    onAddOperarios = { onAddOperarios(trabajo) },
                )
            }
        }
    }
}