package com.works.naval



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun IsometricoCard(
    item: TrabajoItem,
    onClick: () -> Unit,
    isUpdating: Boolean,
    isSaving: Boolean,
    isAddingOperarios: Boolean,
    onEstadoChange: (EstadoTrabajo) -> Unit,
    onAddOperarios: () -> Unit,
) {
    val colors = estadoColors(item.estado)
    var estadoMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colors.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = item.codigoIsometrico,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.content,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiText(
                        "Trabajo #${item.id} • Bloque: ${item.bloque} • Sistema: ${item.subsistema}",
                        "Traballo #${item.id} • Bloque: ${item.bloque} • Sistema: ${item.subsistema}",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.content.copy(alpha = 0.8f),
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EstadoColorWork(estado = item.estado)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box {
                        Button(
                            onClick = { estadoMenuExpanded = true },
                            enabled = !isUpdating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF134386),
                                contentColor = Color.White,
                            ),
                        ) {
                            Text(
                                if (isSaving) uiText("Guardando...", "Gardando...")
                                else uiText("Cambiar estado", "Cambiar estado"),
                            )
                        }
                        DropdownMenu(
                            expanded = estadoMenuExpanded,
                            onDismissRequest = { estadoMenuExpanded = false }
                        ) {
                            EstadoTrabajo.values().forEach { estado ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (LocalUiLanguage.current == UiLanguage.GALICIAN) {
                                                estado.descripcionGallega
                                            } else {
                                                estado.descripcion
                                            },
                                        )
                                    },
                                    onClick = {
                                        onEstadoChange(estado)
                                        estadoMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = onAddOperarios,
                        enabled = !isUpdating && !isAddingOperarios,
                    ) {
                        Text(
                            if (isAddingOperarios) uiText("Guardando...", "Gardando...")
                            else uiText("Añadir operarios", "Engadir operarios"),
                        )
                    }
                }
            }
        }
    }
}