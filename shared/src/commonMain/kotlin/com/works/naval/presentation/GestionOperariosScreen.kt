package com.works.naval.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.works.naval.core.localization.LocalUiLanguage
import com.works.naval.core.localization.text
import com.works.naval.core.localization.uiText
import com.works.naval.data.remote.createOperario
import com.works.naval.data.remote.deleteOperario
import com.works.naval.data.remote.fetchOperarios
import com.works.naval.data.remote.updateOperario
import com.works.naval.domain.model.OperarioOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun GestionOperariosScreen() {
    var operarios by remember { mutableStateOf<List<OperarioOption>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var operationError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var showFormDialog by remember { mutableStateOf(false) }
    var editingOperario by remember { mutableStateOf<OperarioOption?>(null) }
    var deletingOperario by remember { mutableStateOf<OperarioOption?>(null) }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var especialista by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val language = LocalUiLanguage.current

    LaunchedEffect(refreshKey) {
        isLoading = true
        loadError = null
        try {
            operarios = fetchOperarios().sortedWith(
                compareBy<OperarioOption> { it.nombre.lowercase() }
                    .thenBy { it.apellidos.lowercase() },
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            loadError = exception.message ?: language.text(
                "No se pudieron cargar los operarios.",
                "Non se puideron cargar os operarios.",
            )
        } finally {
            isLoading = false
        }
    }

    fun openCreateDialog() {
        editingOperario = null
        nombre = ""
        apellidos = ""
        especialista = ""
        usuario = ""
        password = ""
        formError = null
        operationError = null
        showFormDialog = true
    }

    fun openEditDialog(operario: OperarioOption) {
        editingOperario = operario
        nombre = operario.nombre
        apellidos = operario.apellidos
        especialista = operario.especialista
        usuario = ""
        password = ""
        formError = null
        operationError = null
        showFormDialog = true
    }

    fun saveOperario() {
        val normalizedNombre = nombre.trim()
        val normalizedApellidos = apellidos.trim()
        val isCreatingEncargado =
            editingOperario == null && especialista.equals("Encargado", ignoreCase = true)
        if (normalizedNombre.isBlank() || normalizedApellidos.isBlank() || especialista.isBlank()) {
            formError = language.text(
                "Completa el nombre, los apellidos y la especialidad.",
                "Completa o nome, os apelidos e a especialidade.",
            )
            return
        }
        if (isCreatingEncargado && (usuario.isBlank() || password.isBlank())) {
            formError = language.text(
                "Introduce el nombre de usuario y la contraseña del encargado.",
                "Introduce o nome de usuario e o contrasinal do encargado.",
            )
            return
        }
        if (isSaving) return

        isSaving = true
        formError = null
        scope.launch {
            try {
                val operario = editingOperario
                if (operario == null) {
                    createOperario(
                        nombre = normalizedNombre,
                        apellidos = normalizedApellidos,
                        especialista = especialista,
                        usuario = usuario.trim().takeIf { isCreatingEncargado },
                        password = password.takeIf { isCreatingEncargado },
                    )
                    successMessage = language.text(
                        "Operario registrado correctamente.",
                        "Operario rexistrado correctamente.",
                    )
                } else {
                    updateOperario(
                        operario.id,
                        normalizedNombre,
                        normalizedApellidos,
                        especialista,
                    )
                    successMessage = language.text(
                        "Operario modificado correctamente.",
                        "Operario modificado correctamente.",
                    )
                }
                showFormDialog = false
                refreshKey++
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                formError = exception.message ?: language.text(
                    "No se pudo guardar el operario.",
                    "Non se puido gardar o operario.",
                )
            } finally {
                isSaving = false
            }
        }
    }

    fun removeOperario(operario: OperarioOption) {
        if (isSaving) return
        isSaving = true
        operationError = null
        scope.launch {
            try {
                deleteOperario(operario.id)
                deletingOperario = null
                successMessage = language.text(
                    "Operario dado de baja correctamente.",
                    "Operario dado de baixa correctamente.",
                )
                refreshKey++
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                operationError = exception.message ?: language.text(
                    "No se pudo dar de baja al operario.",
                    "Non se puido dar de baixa o operario.",
                )
            } finally {
                isSaving = false
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
                text = uiText("Gestión de operarios", "Xestión de operarios"),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
            )
            IconButton(
                onClick = { refreshKey++ },
                enabled = !isLoading && !isSaving,
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = uiText("Actualizar lista", "Actualizar lista"),
                )
            }
            Button(onClick = ::openCreateDialog, enabled = !isSaving) {
                Text(uiText("Nuevo operario", "Novo operario"))
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
            loadError != null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(loadError.orEmpty(), color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = { refreshKey++ }) {
                    Text(uiText("Reintentar", "Tentar de novo"))
                }
            }
            operarios.isEmpty() -> Text(
                uiText("No hay operarios registrados.", "Non hai operarios rexistrados."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(operarios, key = { it.id }) { operario ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = operario.nombreCompleto,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = operario.especialista,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            OutlinedButton(
                                onClick = { openEditDialog(operario) },
                                enabled = !isSaving,
                            ) {
                                Text(uiText("Modificar", "Modificar"))
                            }
                            TextButton(
                                onClick = {
                                    operationError = null
                                    deletingOperario = operario
                                },
                                enabled = !isSaving,
                            ) {
                                Text(
                                    uiText("Dar de baja", "Dar de baixa"),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) showFormDialog = false },
            title = {
                Text(
                    if (editingOperario == null) {
                        uiText("Nuevo operario", "Novo operario")
                    } else {
                        uiText("Modificar operario", "Modificar operario")
                    },
                )
            },
            text = {
                Column(
                    modifier = Modifier.widthIn(min = 280.dp, max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it; formError = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(uiText("Nombre", "Nome")) },
                        singleLine = true,
                        enabled = !isSaving,
                    )
                    OutlinedTextField(
                        value = apellidos,
                        onValueChange = { apellidos = it; formError = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(uiText("Apellidos", "Apelidos")) },
                        singleLine = true,
                        enabled = !isSaving,
                    )
                    EspecialidadSelector(
                        value = especialista,
                        enabled = !isSaving,
                        onSelect = {
                            especialista = it
                            if (!it.equals("Encargado", ignoreCase = true)) {
                                usuario = ""
                                password = ""
                            }
                            formError = null
                        },
                    )
                    if (editingOperario == null &&
                        especialista.equals("Encargado", ignoreCase = true)
                    ) {
                        OutlinedTextField(
                            value = usuario,
                            onValueChange = { usuario = it; formError = null },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(uiText("Nombre de usuario", "Nome de usuario")) },
                            singleLine = true,
                            enabled = !isSaving,
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; formError = null },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(uiText("Contraseña", "Contrasinal")) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            enabled = !isSaving,
                        )
                    }
                    formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(onClick = ::saveOperario, enabled = !isSaving) {
                    Text(
                        if (isSaving) uiText("Guardando...", "Gardando...")
                        else uiText("Guardar", "Gardar"),
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showFormDialog = false }, enabled = !isSaving) {
                    Text(uiText("Cancelar", "Cancelar"))
                }
            },
        )
    }

    deletingOperario?.let { operario ->
        AlertDialog(
            onDismissRequest = { if (!isSaving) deletingOperario = null },
            title = { Text(uiText("Dar de baja al operario", "Dar de baixa o operario")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        uiText(
                            "¿Seguro que quieres dar de baja a ${operario.nombreCompleto}?",
                            "Seguro que queres dar de baixa a ${operario.nombreCompleto}?",
                        ),
                    )
                    operationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(onClick = { removeOperario(operario) }, enabled = !isSaving) {
                    Text(
                        if (isSaving) uiText("Procesando...", "Procesando...")
                        else uiText("Confirmar", "Confirmar"),
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingOperario = null }, enabled = !isSaving) {
                    Text(uiText("Cancelar", "Cancelar"))
                }
            },
        )
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
                    IconButton(onClick = { expanded = !expanded }, enabled = enabled) {
                        Icon(
                            Icons.Default.ArrowDropDown,
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
