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
import androidx.compose.ui.text.style.TextOverflow
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

    BoxWithConstraints {
        val compactLayout = maxWidth < 480.dp
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = colors.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            if (compactLayout) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TrabajoCardDescription(item = item, contentColor = colors.content)
                    EstadoColorWork(estado = item.estado)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstadoTrabajoMenuButton(
                            expanded = estadoMenuExpanded,
                            onExpandedChange = { estadoMenuExpanded = it },
                            isUpdating = isUpdating,
                            isSaving = isSaving,
                            compactLayout = true,
                            onEstadoChange = onEstadoChange,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(
                            onClick = onAddOperarios,
                            enabled = !isUpdating && !isAddingOperarios,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (isAddingOperarios) "..." else uiText("Operarios", "Operarios"), maxLines = 1)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TrabajoCardDescription(item = item, contentColor = colors.content)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EstadoColorWork(estado = item.estado)
                        EstadoTrabajoMenuButton(
                            expanded = estadoMenuExpanded,
                            onExpandedChange = { estadoMenuExpanded = it },
                            isUpdating = isUpdating,
                            isSaving = isSaving,
                            compactLayout = false,
                            fillButton = false,
                            onEstadoChange = onEstadoChange,
                        )
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
}

@Composable
private fun TrabajoCardDescription(
    item: TrabajoItem,
    contentColor: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = uiText(
                "Isométrico: ${item.codigoIsometrico}",
                "Isométrico: ${item.codigoIsometrico}",
            ),
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = uiText(
                "Bloque: ${item.bloque} · Sistema: ${item.subsistema}",
                "Bloque: ${item.bloque} · Sistema: ${item.subsistema}",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EstadoTrabajoMenuButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isUpdating: Boolean,
    isSaving: Boolean,
    compactLayout: Boolean,
    fillButton: Boolean = true,
    onEstadoChange: (EstadoTrabajo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Button(
            onClick = { onExpandedChange(true) },
            enabled = !isUpdating,
            modifier = if (fillButton) Modifier.fillMaxWidth() else Modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF134386),
                contentColor = Color.White,
            ),
        ) {
            Text(
                when {
                    isSaving -> uiText("Guardando...", "Gardando...")
                    compactLayout -> uiText("Estado", "Estado")
                    else -> uiText("Cambiar estado", "Cambiar estado")
                },
                maxLines = 1,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
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
                        onExpandedChange(false)
                    },
                )
            }
        }
    }
}