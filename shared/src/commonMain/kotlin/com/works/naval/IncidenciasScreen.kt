package com.works.naval

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IncidenciasScreen(
    incidencias: List<String>,
    onDeleteIncidencia: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = uiText("Incidencias activas", "Incidencias activas"),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (incidencias.isEmpty()) {
            Text(uiText("No hay incidencias activas.", "Non hai incidencias activas."))
        }
        incidencias.forEachIndexed { index, incidencia ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiText("Incidencia ${index + 1}", "Incidencia ${index + 1}"),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (incidencia) {
                                "Retraso en la soldadura del bloque B322" ->
                                    uiText(
                                        incidencia,
                                        "Atraso na soldadura do bloque B322",
                                    )
                                "Falta material para la instalación eléctrica" ->
                                    uiText(
                                        incidencia,
                                        "Falta material para a instalación eléctrica",
                                    )
                                "Revisión pendiente del isométrico 2" ->
                                    uiText(
                                        incidencia,
                                        "Revisión pendente do isométrico 2",
                                    )
                                "Incidencia de seguridad en el taller" ->
                                    uiText(
                                        incidencia,
                                        "Incidencia de seguridade no taller",
                                    )
                                else -> incidencia
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                    IconButton(
                        onClick = { onDeleteIncidencia(incidencia) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = uiText("Eliminar incidencia", "Eliminar incidencia"),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
