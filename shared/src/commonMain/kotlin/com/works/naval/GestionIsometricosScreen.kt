package com.works.naval

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val MAX_ISOMETRICO_PDF_BYTES = 25 * 1024 * 1024

@Composable
fun GestionIsometricosScreen() {
    var bloques by remember { mutableStateOf<List<BloqueOption>>(emptyList()) }
    var isometricos by remember { mutableStateOf<List<IsometricoAdministrado>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var updatingIsometricoId by remember { mutableStateOf<Long?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var operationError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var codigo by remember { mutableStateOf("") }
    var lineaTuberia by remember { mutableStateOf("") }
    var revision by remember { mutableStateOf("A") }
    var selectedBlockId by remember { mutableStateOf<Long?>(null) }
    var blockMenuExpanded by remember { mutableStateOf(false) }
    var selectedPdf by remember { mutableStateOf<PdfFileSelection?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val language = LocalUiLanguage.current
    val pdfPicker = rememberPdfFilePicker(
        onPdfSelected = {
            selectedPdf = it
            formError = null
        },
        onError = { formError = it },
    )

    LaunchedEffect(refreshKey) {
        isLoading = true
        loadError = null
        try {
            val fetchedBlocks = fetchBloques().sortedBy { it.codigo }
            val fetchedIsometricos = fetchIsometricosAdministracion()
                .sortedWith(compareBy<IsometricoAdministrado> { it.codigo.lowercase() })
            bloques = fetchedBlocks
            isometricos = fetchedIsometricos
            if (selectedBlockId !in fetchedBlocks.map { it.id }) {
                selectedBlockId = fetchedBlocks.firstOrNull()?.id
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            loadError = exception.message ?: language.text(
                "No se pudieron cargar los isométricos.",
                "Non se puideron cargar os isométricos.",
            )
        } finally {
            isLoading = false
        }
    }

    fun blockLabel(blockId: Long): String =
        bloques.firstOrNull { it.id == blockId }?.codigo ?: blockId.toString()

    fun closeCreateDialog() {
        showCreateDialog = false
        codigo = ""
        lineaTuberia = ""
        revision = "A"
        selectedPdf = null
        formError = null
        blockMenuExpanded = false
    }

    fun createDraft() {
        val normalizedCode = codigo.trim()
        val selectedBlock = selectedBlockId
        val normalizedSystem = lineaTuberia.trim()
        val normalizedRevision = revision.trim()
        val pdf = selectedPdf
        when {
            normalizedCode.isBlank() ->
                formError = language.text("Introduce el código del isométrico.", "Introduce o código do isométrico.")
            selectedBlock == null ->
                formError = language.text("Selecciona un bloque.", "Selecciona un bloque.")
            normalizedSystem.isBlank() ->
                formError = language.text("Introduce el sistema o línea.", "Introduce o sistema ou liña.")
            normalizedRevision.isBlank() ->
                formError = language.text("Introduce la revisión.", "Introduce a revisión.")
            isometricos.any {
                it.codigo.equals(normalizedCode, ignoreCase = true) &&
                    it.bloqueId == selectedBlock &&
                    it.revision.equals(normalizedRevision, ignoreCase = true)
            } ->
                formError = language.text(
                    "Ya existe ese isométrico con esa revisión en el bloque seleccionado.",
                    "Xa existe ese isométrico con esa revisión no bloque seleccionado.",
                )
            pdf == null ->
                formError = language.text("Selecciona el PDF del isométrico.", "Selecciona o PDF do isométrico.")
            !pdf.fileName.endsWith(".pdf", ignoreCase = true) ||
                pdf.bytes.size < 5 ||
                pdf.bytes.copyOfRange(0, 5).decodeToString() != "%PDF-" ->
                formError = language.text("El archivo seleccionado no es un PDF válido.", "O arquivo seleccionado non é un PDF válido.")
            pdf.bytes.size > MAX_ISOMETRICO_PDF_BYTES ->
                formError = language.text("El PDF supera el límite de 25 MB.", "O PDF supera o límite de 25 MB.")
            else -> {
                isSaving = true
                formError = null
                scope.launch {
                    try {
                        createIsometricoDraft(
                            codigo = normalizedCode,
                            bloqueId = selectedBlock,
                            lineaTuberia = normalizedSystem,
                            revision = normalizedRevision,
                            pdf = pdf,
                        )
                        closeCreateDialog()
                        successMessage = language.text(
                            "Isométrico guardado como borrador.",
                            "Isométrico gardado como borrador.",
                        )
                        refreshKey++
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        formError = exception.message ?: language.text(
                            "No se pudo subir el isométrico.",
                            "Non se puido subir o isométrico.",
                        )
                    } finally {
                        isSaving = false
                    }
                }
            }
        }
    }

    fun changeStatus(isometrico: IsometricoAdministrado, newStatus: EstadoIsometrico) {
        if (updatingIsometricoId != null) return
        updatingIsometricoId = isometrico.id
        operationError = null
        successMessage = null
        scope.launch {
            try {
                updateIsometricoEstado(isometrico.id, newStatus)
                successMessage = when (newStatus) {
                    EstadoIsometrico.PUBLICADO -> language.text(
                        "Isométrico publicado.",
                        "Isométrico publicado.",
                    )
                    EstadoIsometrico.ARCHIVADO -> language.text(
                        "Isométrico archivado.",
                        "Isométrico arquivado.",
                    )
                    EstadoIsometrico.BORRADOR -> language.text(
                        "Isométrico actualizado.",
                        "Isométrico actualizado.",
                    )
                }
                refreshKey++
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                operationError = exception.message ?: language.text(
                    "No se pudo actualizar el isométrico.",
                    "Non se puido actualizar o isométrico.",
                )
            } finally {
                updatingIsometricoId = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                uiText("Gestión de isométricos", "Xestión de isométricos"),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
            )
            IconButton(
                onClick = { refreshKey++ },
                enabled = !isLoading && !isSaving && updatingIsometricoId == null,
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = uiText("Actualizar lista", "Actualizar lista"),
                )
            }
            Button(
                onClick = {
                    operationError = null
                    formError = null
                    showCreateDialog = true
                },
                enabled = !isSaving,
            ) {
                Text(uiText("Nuevo isométrico", "Novo isométrico"))
            }
        }

        operationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        when {
            isLoading -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            loadError != null -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(loadError.orEmpty(), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { refreshKey++ }) {
                    Text(uiText("Reintentar", "Tentar de novo"))
                }
            }
            isometricos.isEmpty() -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    uiText(
                        "Todavía no hay isométricos registrados.",
                        "Aínda non hai isométricos rexistrados.",
                    ),
                )
            }
            else -> LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(isometricos, key = { it.id }) { isometrico ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                uiText(
                                    "Isométrico: ${isometrico.codigo}",
                                    "Isométrico: ${isometrico.codigo}",
                                ),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                uiText(
                                    "Bloque: ${blockLabel(isometrico.bloqueId)} · Sistema: ${isometrico.lineaTuberia} · Revisión: ${isometrico.revision}",
                                    "Bloque: ${blockLabel(isometrico.bloqueId)} · Sistema: ${isometrico.lineaTuberia} · Revisión: ${isometrico.revision}",
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    estadoLabel(isometrico.estado),
                                    modifier = Modifier.weight(1f),
                                    color = estadoColor(isometrico.estado),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                when (isometrico.estado) {
                                    EstadoIsometrico.BORRADOR -> Button(
                                        onClick = {
                                            changeStatus(isometrico, EstadoIsometrico.PUBLICADO)
                                        },
                                        enabled = updatingIsometricoId == null,
                                    ) {
                                        Text(uiText("Publicar", "Publicar"))
                                    }
                                    EstadoIsometrico.PUBLICADO -> OutlinedButton(
                                        onClick = {
                                            changeStatus(isometrico, EstadoIsometrico.ARCHIVADO)
                                        },
                                        enabled = updatingIsometricoId == null,
                                    ) {
                                        Text(uiText("Archivar", "Arquivar"))
                                    }
                                    EstadoIsometrico.ARCHIVADO -> Unit
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        val selectedBlock = bloques.firstOrNull { it.id == selectedBlockId }
        AlertDialog(
            onDismissRequest = { if (!isSaving) closeCreateDialog() },
            title = { Text(uiText("Nuevo isométrico", "Novo isométrico")) },
            text = {
                Column(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it; formError = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(uiText("Código del isométrico", "Código do isométrico")) },
                        singleLine = true,
                        enabled = !isSaving,
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { blockMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving && bloques.isNotEmpty(),
                        ) {
                            Text(
                                selectedBlock?.codigo
                                    ?: uiText("Seleccionar bloque", "Seleccionar bloque"),
                            )
                        }
                        DropdownMenu(
                            expanded = blockMenuExpanded,
                            onDismissRequest = { blockMenuExpanded = false },
                        ) {
                            bloques.forEach { bloque ->
                                DropdownMenuItem(
                                    text = { Text(bloque.codigo) },
                                    onClick = {
                                        selectedBlockId = bloque.id
                                        blockMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = lineaTuberia,
                        onValueChange = { lineaTuberia = it; formError = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(uiText("Sistema o línea", "Sistema ou liña")) },
                        singleLine = true,
                        enabled = !isSaving,
                    )
                    OutlinedTextField(
                        value = revision,
                        onValueChange = { revision = it; formError = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(uiText("Revisión", "Revisión")) },
                        singleLine = true,
                        enabled = !isSaving,
                    )
                    OutlinedButton(
                        onClick = pdfPicker,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                    ) {
                        Text(
                            selectedPdf?.fileName
                                ?: uiText("Seleccionar PDF (máx. 25 MB)", "Seleccionar PDF (máx. 25 MB)"),
                        )
                    }
                    selectedPdf?.let { pdf ->
                        Text(
                            text = "${pdf.bytes.size / (1024 * 1024)} MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(
                    onClick = ::createDraft,
                    enabled = !isSaving,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator()
                    } else {
                        Text(uiText("Guardar borrador", "Gardar borrador"))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = ::closeCreateDialog,
                    enabled = !isSaving,
                ) {
                    Text(uiText("Cancelar", "Cancelar"))
                }
            },
        )
    }
}

@Composable
private fun estadoLabel(estado: EstadoIsometrico): String = when (estado) {
    EstadoIsometrico.BORRADOR -> uiText("Borrador", "Borrador")
    EstadoIsometrico.PUBLICADO -> uiText("Publicado", "Publicado")
    EstadoIsometrico.ARCHIVADO -> uiText("Archivado", "Arquivado")
}

@Composable
private fun estadoColor(estado: EstadoIsometrico) = when (estado) {
    EstadoIsometrico.BORRADOR -> MaterialTheme.colorScheme.tertiary
    EstadoIsometrico.PUBLICADO -> MaterialTheme.colorScheme.primary
    EstadoIsometrico.ARCHIVADO -> MaterialTheme.colorScheme.onSurfaceVariant
}
