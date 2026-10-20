package com.works.naval

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun OperariosAsignadosPanel(trabajo: TrabajoItem) {
    var operarios by remember(trabajo.id) { mutableStateOf<List<OperarioOption>>(emptyList()) }
    var isLoading by remember(trabajo.id) { mutableStateOf(true) }
    var errorMessage by remember(trabajo.id) { mutableStateOf<String?>(null) }
    var refreshKey by remember(trabajo.id) { mutableStateOf(0) }
    val language = LocalUiLanguage.current

    LaunchedEffect(trabajo.id, trabajo.operarioIds, refreshKey) {
        isLoading = true
        errorMessage = null
        try {
            operarios = if (trabajo.operarioIds.isEmpty()) {
                emptyList()
            } else {
                val idsAsignados = trabajo.operarioIds.toSet()
                fetchOperarios()
                    .filter { it.id in idsAsignados }
                    .sortedWith(
                        compareBy<OperarioOption> { it.nombre.lowercase() }
                            .thenBy { it.apellidos.lowercase() },
                    )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            errorMessage = exception.message ?: language.text(
                "No se pudieron cargar los operarios",
                "Non se puideron cargar os operarios",
            )
        } finally {
            isLoading = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        val tuberos = operarios.filter { it.especialista.contains("TUBER", ignoreCase = true) }
        val soldadores = operarios.filter { it.especialista.contains("SOLD", ignoreCase = true) }
        val otrosEspecialistas = operarios.filterNot { operario ->
            operario.especialista.contains("TUBER", ignoreCase = true) ||
                operario.especialista.contains("SOLD", ignoreCase = true)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Text(
                text = uiText(
                    "Operarios · Trabajo #${trabajo.id}",
                    "Operarios · Traballo #${trabajo.id}",
                ),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            when {
                isLoading -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text(uiText("Cargando operarios...", "Cargando operarios..."), style = MaterialTheme.typography.bodySmall)
                }
                errorMessage != null -> Column {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    TextButton(onClick = { refreshKey++ }) {
                        Text(uiText("Reintentar", "Tentar de novo"))
                    }
                }
                operarios.isEmpty() -> Text(
                    text = if (trabajo.operarioIds.isEmpty()) {
                        uiText("No hay operarios asignados.", "Non hai operarios asignados.")
                    } else {
                        uiText("No se encontraron los operarios asignados.", "Non se atoparon os operarios asignados.")
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item {
                        Text(
                            text = uiText(
                                "Montaje · Tuberos (${tuberos.size})",
                                "Montaxe · Tubistas (${tuberos.size})",
                            ),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    if (tuberos.isEmpty()) {
                        item { Text(uiText("No hay tuberos asignados.", "Non hai tubistas asignados.")) }
                    } else {
                        items(tuberos, key = { it.id }) { operario ->
                            Text(
                                text = operario.nombreCompleto,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiText(
                                "Soldadura · Soldadores (${soldadores.size})",
                                "Soldadura · Soldadores (${soldadores.size})",
                            ),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    if (soldadores.isEmpty()) {
                        item { Text(uiText("No hay soldadores asignados.", "Non hai soldadores asignados.")) }
                    } else {
                        items(soldadores, key = { it.id }) { operario ->
                            Text(
                                text = operario.nombreCompleto,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    if (otrosEspecialistas.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiText(
                                    "Otros especialistas (${otrosEspecialistas.size})",
                                    "Outros especialistas (${otrosEspecialistas.size})",
                                ),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        items(otrosEspecialistas, key = { it.id }) { operario ->
                            Text(
                                text = operario.nombreCompleto,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}
