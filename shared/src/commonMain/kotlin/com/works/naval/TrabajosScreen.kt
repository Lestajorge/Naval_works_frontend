package com.works.naval

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException

@Composable
fun TrabajosScreen() {
    var trabajos by remember { mutableStateOf<List<TrabajoItem>>(emptyList()) }
    var selectedTrabajoId by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        isLoading = true
        errorMessage = null
        try {
            trabajos = fetchTrabajos()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "No se pudieron cargar los trabajos"
        } finally {
            isLoading = false
        }
    }

    val selectedTrabajo = trabajos.firstOrNull { it.id == selectedTrabajoId }
    when {
        selectedTrabajo != null -> IsometricoDetalleVisor(
            item = selectedTrabajo,
            onVolver = { selectedTrabajoId = null },
        )
        isLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        errorMessage != null -> Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = { refreshKey++ }) {
                Text("Reintentar")
            }
        }
        else -> TrabajosSection(
            listaTrabajos = trabajos,
            onSelectTrabajo = { selectedTrabajoId = it.id },
            onEstadoChange = { id, nuevoEstado ->
                trabajos = trabajos.map { trabajo ->
                    if (trabajo.id == id) trabajo.copy(estado = nuevoEstado) else trabajo
                }
            },
        )
    }
}