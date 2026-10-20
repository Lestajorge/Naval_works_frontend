package com.works.naval

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException

@Composable
fun AddOperariosDialog(
    trabajo: TrabajoItem,
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onConfirm: (Set<Long>) -> Unit,
) {
    var operariosDisponibles by remember(trabajo.id) {
        mutableStateOf<List<OperarioOption>>(emptyList())
    }
    var selectedOperarioIds by remember(trabajo.id) { mutableStateOf<Set<Long>>(emptySet()) }
    var isLoading by remember(trabajo.id) { mutableStateOf(true) }
    var loadError by remember(trabajo.id) { mutableStateOf<String?>(null) }
    var refreshKey by remember(trabajo.id) { mutableStateOf(0) }
    val language = LocalUiLanguage.current

    LaunchedEffect(trabajo.id, refreshKey) {
        isLoading = true
        loadError = null
        try {
            val assignedIds = trabajo.operarioIds.toSet()
            operariosDisponibles = fetchOperarios()
                .filter { it.id !in assignedIds }
                .sortedWith(
                    compareBy<OperarioOption> { it.nombre.lowercase() }
                        .thenBy { it.apellidos.lowercase() },
                )
            selectedOperarioIds = emptySet()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            loadError = exception.message ?: language.text(
                "No se pudieron cargar los operarios",
                "Non se puideron cargar os operarios",
            )
        } finally {
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                uiText(
                    "Añadir operarios al trabajo #${trabajo.id}",
                    "Engadir operarios ao traballo #${trabajo.id}",
                ),
            )
        },
        text = {
            Column(
                modifier = Modifier.widthIn(min = 300.dp, max = 500.dp),
            ) {
                Text(
                    uiText(
                        "Ya asignados: ${trabajo.operarioIds.size}",
                        "Xa asignados: ${trabajo.operarioIds.size}",
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                when {
                    isLoading -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text(uiText("Cargando operarios...", "Cargando operarios..."))
                    }
                    loadError != null -> Column {
                        Text(
                            text = loadError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                        )
                        TextButton(onClick = { refreshKey++ }) {
                            Text(uiText("Reintentar", "Tentar de novo"))
                        }
                    }
                    operariosDisponibles.isEmpty() -> Text(
                        uiText(
                            "Todos los operarios ya están asignados.",
                            "Todos os operarios xa están asignados.",
                        ),
                    )
                    else -> LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(operariosDisponibles, key = { it.id }) { operario ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedOperarioIds =
                                            if (operario.id in selectedOperarioIds) {
                                                selectedOperarioIds - operario.id
                                            } else {
                                                selectedOperarioIds + operario.id
                                            }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = operario.id in selectedOperarioIds,
                                    onCheckedChange = { checked ->
                                        selectedOperarioIds =
                                            if (checked) {
                                                selectedOperarioIds + operario.id
                                            } else {
                                                selectedOperarioIds - operario.id
                                            }
                                    },
                                )
                                Column {
                                    Text(operario.nombreCompleto)
                                    Text(
                                        text = operario.especialista,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
                saveError?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedOperarioIds) },
                enabled = !isSaving && !isLoading && selectedOperarioIds.isNotEmpty(),
            ) {
                Text(
                    if (isSaving) uiText("Guardando...", "Gardando...")
                    else uiText("Añadir", "Engadir"),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving,
            ) {
                Text(uiText("Cancelar", "Cancelar"))
            }
        },
    )
}
