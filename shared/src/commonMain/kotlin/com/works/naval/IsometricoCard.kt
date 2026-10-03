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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun IsometricoCard(
    item: IsometricoItem,
    onClick: () -> Unit,
    onEstadoChange: (EstadoTrabajo) -> Unit
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
                    text = item.id,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.content,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bloque: ${item.bloque} • Sistema: ${item.subsistema}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.content.copy(alpha = 0.8f),
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EstadoColorWork(estado = item.estado)
                Box {
                    Button(onClick = { estadoMenuExpanded = true }) {
                        Text("Cambiar Estado")
                    }
                    DropdownMenu(
                        expanded = estadoMenuExpanded,
                        onDismissRequest = { estadoMenuExpanded = false }
                    ) {
                        EstadoTrabajo.values().forEach { estado ->
                            DropdownMenuItem(
                                text = { Text(estado.descripcion) },
                                onClick = {
                                    onEstadoChange(estado)
                                    estadoMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}