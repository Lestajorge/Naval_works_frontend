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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@Composable
fun CrearTrabajoScreen(
    onTrabajoCreado: () -> Unit,
    onCancelar: () -> Unit,
) {
    var operarios by remember { mutableStateOf<List<OperarioOption>>(emptyList()) }
    var isometricos by remember { mutableStateOf<List<IsometricoOption>>(emptyList()) }
    var bloques by remember { mutableStateOf<List<BloqueOption>>(emptyList()) }
    var selectedOperarioIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var selectedIsometricoId by remember { mutableStateOf<Long?>(null) }
    var isometricoSearchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var saveErrorMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val language = LocalUiLanguage.current

    LaunchedEffect(refreshKey) {
        isLoading = true
        errorMessage = null
        try {
            val fetchedOperarios = fetchOperarios().sortedWith(
                compareBy<OperarioOption> { it.nombre.lowercase() }
                    .thenBy { it.apellidos.lowercase() },
            )
            val fetchedIsometricos = fetchIsometricos()
            val fetchedBloques = fetchBloques()
            operarios = fetchedOperarios
            isometricos = fetchedIsometricos
            bloques = fetchedBloques
            selectedOperarioIds = selectedOperarioIds.intersect(fetchedOperarios.map { it.id }.toSet())
            if (selectedIsometricoId !in fetchedIsometricos.map { it.id }) {
                selectedIsometricoId = fetchedIsometricos.firstOrNull()?.id
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            errorMessage = exception.message ?: language.text(
                "No se pudieron cargar los datos",
                "Non se puideron cargar os datos",
            )
        } finally {
            isLoading = false
        }
    }

    val selectedIsometrico = isometricos.firstOrNull { it.id == selectedIsometricoId }
    val filteredIsometricos = isometricos.filter { isometrico ->
        val query = isometricoSearchQuery.trim()
        query.isEmpty() ||
            isometrico.id.toString().contains(query, ignoreCase = true) ||
            isometrico.codigo.contains(query, ignoreCase = true) ||
            isometrico.bloqueId.toString().contains(query, ignoreCase = true) ||
            bloques.firstOrNull { it.id == isometrico.bloqueId }
                ?.codigo
                ?.contains(query, ignoreCase = true) == true
    }

    fun isometricoLabel(isometrico: IsometricoOption): String {
        val bloque = bloques.firstOrNull { it.id == isometrico.bloqueId }?.codigo
            ?: isometrico.bloqueId.toString()
        return "${isometrico.codigo} · ID ${isometrico.id} · Bloque $bloque"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                uiText("Asignar trabajo", "Asignar traballo"),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
            )
            OutlinedButton(onClick = onCancelar, enabled = !isSaving) {
                Text(uiText("Cancelar", "Cancelar"))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            errorMessage != null -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                Button(onClick = { refreshKey++ }) {
                    Text(uiText("Reintentar", "Tentar de novo"))
                }
            }
            else -> {
                Text(uiText("Isométrico", "Isométrico"), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = isometricoSearchQuery,
                    onValueChange = { isometricoSearchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(uiText("Buscar isométrico o bloque", "Buscar isométrico ou bloque"))
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = uiText("Buscar", "Buscar"),
                        )
                    },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                selectedIsometrico?.let { selected ->
                    Text(
                        text = uiText(
                            "Seleccionado: ${isometricoLabel(selected)}",
                            "Seleccionado: ${isometricoLabel(selected)}",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (isometricoSearchQuery.isNotBlank()) {
                    if (filteredIsometricos.isEmpty()) {
                        Text(
                            uiText(
                                "No hay isométricos que coincidan.",
                                "Non hai isométricos que coincidan.",
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(filteredIsometricos, key = { it.id }) { isometrico ->
                                val isSelected = isometrico.id == selectedIsometricoId
                                Card(
                                    onClick = { selectedIsometricoId = isometrico.id },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                    ),
                                ) {
                                    Text(
                                        text = isometricoLabel(isometrico),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiText(
                        "Operarios (${selectedOperarioIds.size} seleccionados)",
                        "Operarios (${selectedOperarioIds.size} seleccionados)",
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (operarios.isEmpty()) {
                    Text(uiText("No se encontraron operarios.", "Non se atoparon operarios."))
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(operarios, key = { it.id }) { operario ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = operario.id in selectedOperarioIds,
                                        onCheckedChange = { checked ->
                                            selectedOperarioIds = if (checked) {
                                                selectedOperarioIds + operario.id
                                            } else {
                                                selectedOperarioIds - operario.id
                                            }
                                        },
                                    )
                                    Text(
                                        text = operario.nombreCompleto,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }

                saveErrorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                Button(
                    onClick = {
                        val isometricoId = selectedIsometrico?.id ?: return@Button
                        scope.launch {
                            isSaving = true
                            saveErrorMessage = null
                            try {
                                createTrabajo(isometricoId, selectedOperarioIds)
                                onTrabajoCreado()
                            } catch (exception: CancellationException) {
                                throw exception
                            } catch (exception: Exception) {
                                saveErrorMessage = exception.message
                                    ?: language.text("No se pudo crear el trabajo", "Non se puido crear o traballo")
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving && selectedIsometrico != null && selectedOperarioIds.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                ) {
                    Text(
                        if (isSaving) uiText("Creando trabajo...", "Creando traballo...")
                        else uiText("Crear trabajo", "Crear traballo"),
                    )
                }
            }
        }
    }
}
