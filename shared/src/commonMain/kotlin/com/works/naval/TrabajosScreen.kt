package com.works.naval

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@Composable
fun TrabajosScreen(
    onTrabajoSeleccionado: (TrabajoItem?) -> Unit,
) {
    var trabajos by remember { mutableStateOf<List<TrabajoItem>>(emptyList()) }
    var selectedTrabajoId by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var estadoUpdateError by remember { mutableStateOf<String?>(null) }
    var updatingTrabajoId by remember { mutableStateOf<Long?>(null) }
    var trabajoParaAñadirOperarios by remember { mutableStateOf<TrabajoItem?>(null) }
    var isSavingOperarios by remember { mutableStateOf(false) }
    var addOperariosError by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val language = LocalUiLanguage.current

    LaunchedEffect(refreshKey) {
        isLoading = true
        errorMessage = null
        try {
            trabajos = fetchTrabajos()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            errorMessage = exception.message ?: language.text(
                "No se pudieron cargar los trabajos",
                "Non se puideron cargar os traballos",
            )
        } finally {
            isLoading = false
        }
    }

    val selectedTrabajo = trabajos.firstOrNull { it.id == selectedTrabajoId }
    when {
        selectedTrabajo != null -> IsometricoDetalleVisor(
            item = selectedTrabajo,
            onVolver = {
                selectedTrabajoId = null
                onTrabajoSeleccionado(null)
            },
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
                Text(uiText("Reintentar", "Tentar de novo"))
            }
        }
        else -> TrabajosSection(
            listaTrabajos = trabajos,
            onSelectTrabajo = {
                selectedTrabajoId = it.id
                onTrabajoSeleccionado(it)
            },
            estadoUpdateError = estadoUpdateError,
            isUpdatingEstado = updatingTrabajoId != null,
            updatingTrabajoId = updatingTrabajoId,
            isAddingOperarios = isSavingOperarios,
            addingOperariosTrabajoId = trabajoParaAñadirOperarios?.id,
            onAddOperarios = { trabajo ->
                if (updatingTrabajoId == null && !isSavingOperarios) {
                    trabajoParaAñadirOperarios = trabajo
                    addOperariosError = null
                }
            },
            onEstadoChange = { id, nuevoEstado ->
                val trabajo = trabajos.firstOrNull { it.id == id }
                if (trabajo != null && trabajo.estado != nuevoEstado && updatingTrabajoId == null) {
                    updatingTrabajoId = id
                    estadoUpdateError = null
                    scope.launch {
                        try {
                            try {
                                updateTrabajoEstado(trabajo, nuevoEstado)
                            } catch (exception: CancellationException) {
                                throw exception
                            } catch (exception: Exception) {
                                estadoUpdateError = exception.message ?: language.text(
                                    "No se pudo guardar el estado del trabajo",
                                    "Non se puido gardar o estado do traballo",
                                )
                                return@launch
                            }

                            try {
                                trabajos = fetchTrabajos()
                            } catch (exception: CancellationException) {
                                throw exception
                            } catch (exception: Exception) {
                                estadoUpdateError =
                                    language.text(
                                        "El estado se guardó, pero no se pudo refrescar la lista: ",
                                        "Gardouse o estado, pero non se puido actualizar a lista: ",
                                    ) + (exception.message ?: language.text("error desconocido", "erro descoñecido"))
                            }
                        } finally {
                            updatingTrabajoId = null
                        }
                    }
                }
            },
        )
    }

    trabajoParaAñadirOperarios?.let { trabajo ->
        AddOperariosDialog(
            trabajo = trabajo,
            isSaving = isSavingOperarios,
            saveError = addOperariosError,
            onDismiss = {
                trabajoParaAñadirOperarios = null
                addOperariosError = null
            },
            onConfirm = { nuevosOperarioIds ->
                if (!isSavingOperarios && nuevosOperarioIds.isNotEmpty()) {
                    isSavingOperarios = true
                    addOperariosError = null
                    scope.launch {
                        try {
                            addOperariosToTrabajo(trabajo, nuevosOperarioIds)
                            val trabajoActualizado = trabajo.copy(
                                operarioIds = (trabajo.operarioIds + nuevosOperarioIds).distinct(),
                            )
                            trabajos = trabajos.map { trabajoEnLista ->
                                if (trabajoEnLista.id == trabajo.id) {
                                    trabajoActualizado
                                } else {
                                    trabajoEnLista
                                }
                            }
                            trabajoParaAñadirOperarios = null
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            addOperariosError = exception.message ?: language.text(
                                "No se pudieron añadir los operarios",
                                "Non se puideron engadir os operarios",
                            )
                        } finally {
                            isSavingOperarios = false
                        }
                    }
                }
            },
        )
    }
}