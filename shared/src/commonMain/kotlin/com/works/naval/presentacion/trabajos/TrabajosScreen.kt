package com.works.naval.presentacion.trabajos

import com.works.naval.presentacion.configuracion.AdminLoginDialog
import com.works.naval.presentacion.isometricos.IsometricoDetalleVisor
import com.works.naval.presentacion.operarios.AddOperariosDialog

import com.works.naval.core.localization.*
import com.works.naval.data.preferences.*
import com.works.naval.data.remote.*
import com.works.naval.domain.model.*
import com.works.naval.platform.*

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun TrabajosScreen(
    onTrabajoSeleccionado: (TrabajoItem?) -> Unit,
    onTrabajosChanged: () -> Unit,
) {
    var trabajos by remember { mutableStateOf<List<TrabajoItem>>(emptyList()) }
    var trabajosEnServidor by remember { mutableStateOf<Set<Pair<Long, Long>>>(emptySet()) }
    var selectedTrabajoId by remember { mutableStateOf<Long?>(null) }
    var isOfflineMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var bulkDownloadProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var bulkDownloadMessage by remember { mutableStateOf<String?>(null) }
    var bulkDownloadHasErrors by remember { mutableStateOf(false) }
    var estadoUpdateError by remember { mutableStateOf<String?>(null) }
    var updatingTrabajoId by remember { mutableStateOf<Long?>(null) }
    var trabajoParaAñadirOperarios by remember { mutableStateOf<TrabajoItem?>(null) }
    var isSavingOperarios by remember { mutableStateOf(false) }
    var addOperariosError by remember { mutableStateOf<String?>(null) }
    var trabajoParaEliminar by remember { mutableStateOf<TrabajoItem?>(null) }
    var isDeleteConfirmationVisible by remember { mutableStateOf(false) }
    var isAdminLoginVisible by remember { mutableStateOf(false) }
    var deletingTrabajoId by remember { mutableStateOf<Long?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val language = LocalUiLanguage.current

    LaunchedEffect(refreshKey) {
        isLoading = true
        errorMessage = null
        try {
            val trabajosDelServidor = fetchTrabajos()
            trabajosEnServidor = trabajosDelServidor
                .map { it.id to it.isometricoId }
                .toSet()
            trabajos = trabajosDelServidor
            isOfflineMode = false
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            trabajosEnServidor = emptySet()
            try {
                trabajos = loadOfflineTrabajos()
                isOfflineMode = trabajos.isNotEmpty()
                if (!isOfflineMode) {
                    errorMessage = exception.message ?: language.text(
                        "No se pudieron cargar los trabajos",
                        "Non se puideron cargar os traballos",
                    )
                }
            } catch (offlineException: CancellationException) {
                throw offlineException
            } catch (offlineException: Exception) {
                errorMessage = offlineException.message ?: language.text(
                    "No se pudieron leer los PDFs guardados en el dispositivo",
                    "Non se puideron ler os PDFs gardados no dispositivo",
                )
            }
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
        errorMessage != null && trabajos.isEmpty() -> Column(
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
            trabajosEnServidor = trabajosEnServidor,
            isOfflineMode = isOfflineMode,
            bulkDownloadProgress = bulkDownloadProgress,
            bulkDownloadMessage = bulkDownloadMessage,
            bulkDownloadHasErrors = bulkDownloadHasErrors,
            onDownloadAll = {
                if (!isOfflineMode && bulkDownloadProgress == null && trabajos.isNotEmpty()) {
                    val trabajosUnicos = trabajos.distinctBy { it.isometricoId }
                    bulkDownloadProgress = 0 to trabajosUnicos.size
                    bulkDownloadMessage = null
                    bulkDownloadHasErrors = false
                    scope.launch {
                        val failedCodes = mutableListOf<String>()
                        var downloadedCount = 0
                        try {
                            val descargadosIds = listOfflineIsometricoIds().toSet()
                            val trabajosPendientes = trabajosUnicos.filterNot {
                                it.isometricoId in descargadosIds
                            }

                            if (trabajosPendientes.isEmpty()) {
                                bulkDownloadMessage = language.text(
                                    "Todos los PDFs ya están descargados.",
                                    "Todos os PDFs xa están descargados.",
                                )
                                return@launch
                            }

                            bulkDownloadProgress = 0 to trabajosPendientes.size
                            trabajosPendientes.forEachIndexed { index, trabajo ->
                                try {
                                    val pdfBytes = fetchIsometricoPdf(trabajo.isometricoId)
                                    saveOfflineIsometricoPdf(trabajo, pdfBytes)
                                    downloadedCount++
                                } catch (exception: CancellationException) {
                                    throw exception
                                } catch (exception: Exception) {
                                    failedCodes += trabajo.codigoIsometrico
                                } finally {
                                    bulkDownloadProgress = (index + 1) to trabajosPendientes.size
                                }
                            }
                            bulkDownloadMessage = if (failedCodes.isEmpty()) {
                                language.text(
                                    "Se descargaron $downloadedCount PDFs nuevos para usar sin conexión.",
                                    "Descargáronse $downloadedCount PDFs novos para usar sen conexión.",
                                )
                            } else {
                                bulkDownloadHasErrors = true
                                language.text(
                                    "Se descargaron $downloadedCount de ${trabajosPendientes.size} PDFs nuevos. Fallaron: ${failedCodes.joinToString()}",
                                    "Descargáronse $downloadedCount de ${trabajosPendientes.size} PDFs novos. Fallaron: ${failedCodes.joinToString()}",
                                )
                            }
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            bulkDownloadHasErrors = true
                            bulkDownloadMessage = exception.message ?: language.text(
                                "No se pudo comprobar qué PDFs faltaban por descargar.",
                                "Non se puido comprobar que PDFs faltaban por descargar.",
                            )
                        } finally {
                            bulkDownloadProgress = null
                        }
                    }
                }
            },
            onSelectTrabajo = {
                selectedTrabajoId = it.id
                onTrabajoSeleccionado(it)
            },
            estadoUpdateError = estadoUpdateError ?: deleteError ?: errorMessage,
            isUpdatingEstado = updatingTrabajoId != null || deletingTrabajoId != null,
            updatingTrabajoId = updatingTrabajoId,
            isAddingOperarios = isSavingOperarios || deletingTrabajoId != null,
            addingOperariosTrabajoId = trabajoParaAñadirOperarios?.id,
            onAddOperarios = { trabajo ->
                if (updatingTrabajoId == null && !isSavingOperarios) {
                    trabajoParaAñadirOperarios = trabajo
                    addOperariosError = null
                }
            },
            onEstadoChange = onEstadoChange@{ id, nuevoEstado ->
                val trabajo = trabajos.firstOrNull { it.id == id }
                if (trabajo != null && trabajo.estado != nuevoEstado && updatingTrabajoId == null) {
                    if (nuevoEstado == EstadoTrabajo.PENDIENTE && trabajo.operarioIds.isNotEmpty()) {
                        estadoUpdateError = language.text(
                            "No se puede cambiar el trabajo a pendiente porque tiene operarios asignados.",
                            "Non se pode cambiar o traballo a pendente porque ten operarios asignados.",
                        )
                        return@onEstadoChange
                    }

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
                            onTrabajosChanged()

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
            deletingTrabajoId = deletingTrabajoId,
            onDeleteTrabajo = { trabajo ->
                if (!isOfflineMode && updatingTrabajoId == null && !isSavingOperarios && deletingTrabajoId == null) {
                    trabajoParaEliminar = trabajo
                    isDeleteConfirmationVisible = true
                    deleteError = null
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

    if (isDeleteConfirmationVisible) {
        val trabajo = trabajoParaEliminar
        if (trabajo != null) {
            AlertDialog(
                onDismissRequest = {
                    isDeleteConfirmationVisible = false
                    trabajoParaEliminar = null
                },
                title = { Text(uiText("Eliminar trabajo", "Eliminar traballo")) },
                text = {
                    Text(
                        uiText(
                            "¿Quieres eliminar el trabajo del isométrico ${trabajo.codigoIsometrico}? Esta acción no se puede deshacer.",
                            "Queres eliminar o traballo do isométrico ${trabajo.codigoIsometrico}? Esta acción non se pode desfacer.",
                        ),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            isDeleteConfirmationVisible = false
                            isAdminLoginVisible = true
                        },
                    ) {
                        Text(uiText("Continuar", "Continuar"), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            isDeleteConfirmationVisible = false
                            trabajoParaEliminar = null
                        },
                    ) {
                        Text(uiText("Cancelar", "Cancelar"))
                    }
                },
            )
        }
    }

    if (isAdminLoginVisible && trabajoParaEliminar != null) {
        AdminLoginDialog(
            onDismiss = {
                isAdminLoginVisible = false
                trabajoParaEliminar = null
            },
            onAuthenticated = {
                val trabajo = trabajoParaEliminar ?: return@AdminLoginDialog
                isAdminLoginVisible = false
                deletingTrabajoId = trabajo.id
                deleteError = null
                scope.launch {
                    try {
                        deleteTrabajo(trabajo.id)
                        trabajos = trabajos.filterNot { it.id == trabajo.id }
                        trabajosEnServidor = trabajosEnServidor - (trabajo.id to trabajo.isometricoId)
                        onTrabajosChanged()
                        try {
                            val trabajosActualizados = fetchTrabajos()
                            trabajos = trabajosActualizados
                            trabajosEnServidor = trabajosActualizados
                                .map { it.id to it.isometricoId }
                                .toSet()
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            deleteError = language.text(
                                "El trabajo se eliminó, pero no se pudo actualizar la lista: ",
                                "Eliminouse o traballo, pero non se puido actualizar a lista: ",
                            ) + (exception.message ?: language.text("error desconocido", "erro descoñecido"))
                        }
                        trabajoParaEliminar = null
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        deleteError = exception.message ?: language.text(
                            "No se pudo eliminar el trabajo",
                            "Non se puido eliminar o traballo",
                        )
                        trabajoParaEliminar = null
                    } finally {
                        logoutAdministrador()
                        deletingTrabajoId = null
                    }
                }
            },
        )
    }
}