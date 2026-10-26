package com.works.naval

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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

private enum class OperarioAdminAction {
    CREATE,
    DELETE,
    UPDATE,
}

@Composable
fun GestionOperariosScreen() {
    var action by remember { mutableStateOf(OperarioAdminAction.CREATE) }
    var operarios by remember { mutableStateOf<List<OperarioOption>>(emptyList()) }
    var selectedOperarioId by remember { mutableStateOf<Long?>(null) }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var especialista by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var operarioParaEliminar by remember { mutableStateOf<OperarioOption?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val language = LocalUiLanguage.current
    val createdSuccessMessage = uiText(
        "Operario registrado correctamente.",
        "Operario rexistrado correctamente.",
    )
    val updatedSuccessMessage = uiText(
        "Operario modificado correctamente.",
        "Operario modificado correctamente.",
    )
    val deletedSuccessMessage = uiText(
        "Operario dado de baja correctamente.",
        "Operario dado de baixa correctamente.",
    )

    LaunchedEffect(action, refreshKey) {
        if (action != OperarioAdminAction.CREATE) {
            isLoading = true
            errorMessage = null
            try {
                operarios = fetchOperarios().sortedWith(
                    compareBy<OperarioOption> { it.nombre.lowercase() }
                        .thenBy { it.apellidos.lowercase() },
                )
                val selected = if (action == OperarioAdminAction.UPDATE) operarios.firstOrNull() else null
                selectedOperarioId = selected?.id
                nombre = selected?.nombre.orEmpty()
                apellidos = selected?.apellidos.orEmpty()
                especialista = selected?.especialista.orEmpty()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                errorMessage = exception.message ?: language.text(
                    "No se pudieron cargar los operarios.",
                    "Non se puideron cargar os operarios.",
                )
            } finally {
                isLoading = false
            }
        }
    }

    val operarioSeleccionado = operarios.firstOrNull { it.id == selectedOperarioId }

    fun save(
        successMessage: String,
        operation: suspend () -> Unit,
        onSuccess: () -> Unit = {},
    ) {
        if (isSaving) return
        isSaving = true
        errorMessage = null
        statusMessage = null
        scope.launch {
            try {
                operation()
                onSuccess()
                statusMessage = successMessage
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                errorMessage = exception.message ?: language.text(
                    "No se pudo completar la operación.",
                    "Non se puido completar a operación.",
                )
            } finally {
                isSaving = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = uiText("Administrar operarios", "Administrar operarios"),
            style = MaterialTheme.typography.headlineSmall,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OperarioActionButton(
                label = uiText("Alta", "Alta"),
                selected = action == OperarioAdminAction.CREATE,
                modifier = Modifier.weight(1f),
                onClick = {
                    action = OperarioAdminAction.CREATE
                    errorMessage = null
                    statusMessage = null
                },
            )
            OperarioActionButton(
                label = uiText("Baja", "Baixa"),
                selected = action == OperarioAdminAction.DELETE,
                modifier = Modifier.weight(1f),
                onClick = {
                    action = OperarioAdminAction.DELETE
                    errorMessage = null
                    statusMessage = null
                },
            )
            OperarioActionButton(
                label = uiText("Modificar", "Modificar"),
                selected = action == OperarioAdminAction.UPDATE,
                modifier = Modifier.weight(1f),
                onClick = {
                    action = OperarioAdminAction.UPDATE
                    errorMessage = null
                    statusMessage = null
                },
            )
        }

        when (action) {
            OperarioAdminAction.CREATE -> {
                Text(
                    text = uiText(
                        "Registra un nuevo operario en la base de datos.",
                        "Rexistra un novo operario na base de datos.",
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OperarioForm(
                    nombre = nombre,
                    onNombreChange = { nombre = it; statusMessage = null; errorMessage = null },
                    apellidos = apellidos,
                    onApellidosChange = { apellidos = it; statusMessage = null; errorMessage = null },
                    especialista = especialista,
                    onEspecialistaChange = { especialista = it; statusMessage = null; errorMessage = null },
                    enabled = !isSaving,
                    submitLabel = uiText("Guardar operario", "Gardar operario"),
                    isSaving = isSaving,
                    onSubmit = {
                        save(
                            successMessage = createdSuccessMessage,
                            operation = {
                                createOperario(nombre.trim(), apellidos.trim(), especialista)
                            },
                            onSuccess = {
                                nombre = ""
                                apellidos = ""
                                especialista = ""
                            },
                        )
                    },
                )
            }
            OperarioAdminAction.DELETE,
            OperarioAdminAction.UPDATE,
            -> {
                Text(
                    text = uiText(
                        "Selecciona un operario de la base de datos.",
                        "Selecciona un operario da base de datos.",
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                when {
                    isLoading -> CircularProgressIndicator()
                    errorMessage != null -> TextButton(onClick = { refreshKey++ }) {
                        Text(uiText("Reintentar", "Tentar de novo"))
                    }
                    operarios.isEmpty() -> Text(
                        uiText("No hay operarios registrados.", "Non hai operarios rexistrados."),
                    )
                    else -> {
                        OperarioSelector(
                            operarios = operarios,
                            selectedOperario = operarioSeleccionado,
                            enabled = !isSaving,
                            onSelect = { selected ->
                                selectedOperarioId = selected.id
                                nombre = selected.nombre
                                apellidos = selected.apellidos
                                especialista = selected.especialista
                                errorMessage = null
                                statusMessage = null
                            },
                        )
                        if (action == OperarioAdminAction.DELETE) {
                            operarioSeleccionado?.let { selected ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            text = selected.nombreCompleto,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            text = selected.especialista,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Button(
                                    onClick = { operarioParaEliminar = selected },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isSaving,
                                ) {
                                    Text(uiText("Dar de baja al operario", "Dar de baixa o operario"))
                                }
                            }
                        } else if (operarioSeleccionado != null) {
                            OperarioForm(
                                nombre = nombre,
                                onNombreChange = {
                                    nombre = it
                                    errorMessage = null
                                    statusMessage = null
                                },
                                apellidos = apellidos,
                                onApellidosChange = {
                                    apellidos = it
                                    errorMessage = null
                                    statusMessage = null
                                },
                                especialista = especialista,
                                onEspecialistaChange = {
                                    especialista = it
                                    errorMessage = null
                                    statusMessage = null
                                },
                                enabled = !isSaving,
                                submitLabel = uiText("Guardar cambios", "Gardar cambios"),
                                isSaving = isSaving,
                                onSubmit = {
                                    val selectedId = selectedOperarioId ?: return@OperarioForm
                                    save(
                                        successMessage = updatedSuccessMessage,
                                        operation = {
                                            updateOperario(
                                                selectedId,
                                                nombre.trim(),
                                                apellidos.trim(),
                                                especialista,
                                            )
                                        },
                                        onSuccess = { refreshKey++ },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        statusMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.primary)
        }
    }

    operarioParaEliminar?.let { selected ->
        AlertDialog(
            onDismissRequest = { if (!isSaving) operarioParaEliminar = null },
            title = { Text(uiText("Dar de baja al operario", "Dar de baixa o operario")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        uiText(
                            "¿Seguro que quieres dar de baja a ${selected.nombreCompleto}?",
                            "Seguro que queres dar de baixa a ${selected.nombreCompleto}?",
                        ),
                    )
                    errorMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        save(
                            successMessage = deletedSuccessMessage,
                            operation = { deleteOperario(selected.id) },
                            onSuccess = {
                                operarioParaEliminar = null
                                selectedOperarioId = null
                                refreshKey++
                            },
                        )
                    },
                    enabled = !isSaving,
                ) {
                    Text(
                        if (isSaving) uiText("Procesando...", "Procesando...")
                        else uiText("Confirmar", "Confirmar"),
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { operarioParaEliminar = null },
                    enabled = !isSaving,
                ) {
                    Text(uiText("Cancelar", "Cancelar"))
                }
            },
        )
    }
}

@Composable
private fun OperarioActionButton(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(56.dp),
        ) {
            Text(label, maxLines = 2, style = MaterialTheme.typography.labelSmall)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(56.dp),
        ) {
            Text(label, maxLines = 2, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun OperarioForm(
    nombre: String,
    onNombreChange: (String) -> Unit,
    apellidos: String,
    onApellidosChange: (String) -> Unit,
    especialista: String,
    onEspecialistaChange: (String) -> Unit,
    enabled: Boolean,
    submitLabel: String,
    isSaving: Boolean,
    onSubmit: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(uiText("Nombre", "Nome")) },
                singleLine = true,
                enabled = enabled,
            )
            OutlinedTextField(
                value = apellidos,
                onValueChange = onApellidosChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(uiText("Apellidos", "Apelidos")) },
                singleLine = true,
                enabled = enabled,
            )
            EspecialidadSelector(
                value = especialista,
                enabled = enabled,
                onSelect = onEspecialistaChange,
            )
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled &&
                    nombre.isNotBlank() &&
                    apellidos.isNotBlank() &&
                    especialista.isNotBlank(),
            ) {
                if (isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text(submitLabel)
                }
            }
        }
    }
}

@Composable
private fun EspecialidadSelector(
    value: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val menuWidth = maxWidth
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { expanded = true },
                label = { Text(uiText("Especialidad", "Especialidade")) },
                trailingIcon = {
                    IconButton(
                        onClick = { expanded = !expanded },
                        enabled = enabled,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = uiText(
                                "Mostrar especialidades",
                                "Mostrar especialidades",
                            ),
                        )
                    }
                },
                singleLine = true,
                readOnly = true,
                enabled = enabled,
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(menuWidth),
            ) {
                listOf("Soldador", "Tubero", "Encargado").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun OperarioSelector(
    operarios: List<OperarioOption>,
    selectedOperario: OperarioOption?,
    enabled: Boolean,
    onSelect: (OperarioOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val menuWidth = maxWidth
        Box {
            OutlinedTextField(
                value = selectedOperario?.nombreCompleto.orEmpty(),
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { expanded = true },
                label = { Text(uiText("Seleccionar operario", "Seleccionar operario")) },
                trailingIcon = {
                    IconButton(
                        onClick = { expanded = !expanded },
                        enabled = enabled,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = uiText(
                                "Mostrar operarios",
                                "Mostrar operarios",
                            ),
                        )
                    }
                },
                singleLine = true,
                readOnly = true,
                enabled = enabled,
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(menuWidth),
            ) {
                operarios.forEach { operario ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(operario.nombreCompleto)
                                Text(
                                    text = operario.especialista,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        onClick = {
                            onSelect(operario)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
