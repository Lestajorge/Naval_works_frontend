package com.works.naval.presentation

import com.works.naval.core.localization.*
import com.works.naval.data.preferences.*
import com.works.naval.data.remote.*
import com.works.naval.domain.model.*
import com.works.naval.platform.*


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
    trabajosEnServidor: Set<Pair<Long, Long>>,
    isOfflineMode: Boolean,
    bulkDownloadProgress: Pair<Int, Int>?,
    bulkDownloadMessage: String?,
    bulkDownloadHasErrors: Boolean,
    onDownloadAll: () -> Unit,
    onSelectTrabajo: (TrabajoItem) -> Unit,
    onEstadoChange: (Long, EstadoTrabajo) -> Unit,
    estadoUpdateError: String?,
    isUpdatingEstado: Boolean,
    updatingTrabajoId: Long?,
    isAddingOperarios: Boolean,
    addingOperariosTrabajoId: Long?,
    onAddOperarios: (TrabajoItem) -> Unit,
    deletingTrabajoId: Long?,
    onDeleteTrabajo: (TrabajoItem) -> Unit,
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
        if (isOfflineMode) {
            Text(
                text = uiText(
                    "Sin conexión: solo se muestran los isométricos descargados.",
                    "Sen conexión: só se amosan os isométricos descargados.",
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Button(
            onClick = onDownloadAll,
            enabled = !isOfflineMode && bulkDownloadProgress == null && listaTrabajos.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                when {
                    isOfflineMode -> uiText("Descarga masiva no disponible sin conexión", "A descarga masiva non está dispoñible sen conexión")
                    bulkDownloadProgress != null -> uiText("Actualizando PDFs...", "Actualizando PDFs...")
                    else -> uiText("Actualizar PDFs", "Actualizar PDFs")
                },
            )
        }
        bulkDownloadProgress?.let { (completed, total) ->
            Text(
                text = uiText("Descargando $completed de $total", "Descargando $completed de $total"),
                modifier = Modifier.padding(top = 4.dp),
            )
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else completed.toFloat() / total },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
        bulkDownloadMessage?.let { message ->
            Text(
                text = message,
                color = if (bulkDownloadHasErrors) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.padding(top = 4.dp),
            )
        }

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

        if (isOfflineMode && estadoUpdateError == null) {
            Text(
                text = uiText(
                    "Los cambios de estado y operarios requieren conexión.",
                    "Os cambios de estado e operarios requiren conexión.",
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    isUpdating = isOfflineMode || isUpdatingEstado || isAddingOperarios,
                    isSaving = trabajo.id == updatingTrabajoId,
                    isAddingOperarios =
                        !isOfflineMode && isAddingOperarios && trabajo.id == addingOperariosTrabajoId,
                    isDeleting = !isOfflineMode && trabajo.id == deletingTrabajoId,
                    canDelete = !isOfflineMode && (trabajo.id to trabajo.isometricoId) in trabajosEnServidor,
                    onEstadoChange = { nuevoEstado ->
                        if (!isOfflineMode) onEstadoChange(trabajo.id, nuevoEstado)
                    },
                    onAddOperarios = { if (!isOfflineMode) onAddOperarios(trabajo) },
                    onDelete = { if (!isOfflineMode) onDeleteTrabajo(trabajo) },
                )
            }
        }
    }
}