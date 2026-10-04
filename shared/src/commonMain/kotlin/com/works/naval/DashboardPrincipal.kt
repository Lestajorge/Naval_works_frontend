package com.works.naval

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val NavalBlue = Color(0xFF134386)

@Preview
@Composable
private fun DashboardPrincipalPreview() {
    MaterialTheme {
        DashboardPrincipal(
            onNavigateToAssignTask = {},
            onGenerateReport = {},
        )
    }
}

@Composable
fun DashboardPrincipal(
    onNavigateToAssignTask: () -> Unit,
    onGenerateReport: (String) -> Unit,
) {
    var selectedBlock by remember { mutableStateOf("B322") } // Bloque seleccionado por defecto
    var selectedSection by remember { mutableStateOf("Trabajos") }
    var showOperarios by remember { mutableStateOf(false) }
    var operarios by remember { mutableStateOf<List<String>>(emptyList()) }
    var loadingOperarios by remember { mutableStateOf(false) }
    var selectedOperario by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Row(modifier = Modifier.fillMaxSize()) {
        // ==========================================
        // PANEL IZQUIERDO: SUPERVISIÓN Y CONTROL
        // ==========================================
        Column(
            modifier = Modifier
                .weight(0.32f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Text(
                text = "Panel de Control",
                style = MaterialTheme.typography.headlineSmall,
                color = NavalBlue
            )
            Text(
                text = "Supervisión de Taller y Bloques",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de KPIs / Estado General
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("ESTADO DE LA F-110", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Avance Global: 68%", style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(
                        progress = { 0.68f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = NavalBlue,
                    )
                    Text("⚠️ 3 Incidencias en taller", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de Navegación del Encargado
            Text("Gestión", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            NavigationButton(
                icon = Icons.Default.ViewInAr,
                label = "Trabajos",
                isSelected = selectedSection == "Trabajos",
            ) {
                selectedSection = "Trabajos"

            }
            NavigationButton(
                icon = Icons.Default.Group,
                label = "Operarios en buque",
                isSelected = selectedSection == "Operarios en buque",
            ) {
                selectedSection = "Operarios en buque"
            }
            NavigationButton(
                icon = Icons.Default.Warning,
                label = "Incidencias activas",
                isSelected = selectedSection == "Incidencias activas",
            ) {
                selectedSection = "Incidencias activas"
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionPreview(selectedSection)

            Spacer(modifier = Modifier.weight(1f))

            // Acciones directas de Jefe de Taller
            if (showOperarios) {
                OperariosPanel(
                    operarios = operarios,
                    isLoading = loadingOperarios,
                    selectedOperario = selectedOperario,
                    onSelectOperario = { selectedOperario = it },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    showOperarios = true
                    loadingOperarios = true
                    onNavigateToAssignTask()
                    scope.launch {
                        try {
                            operarios = fetchOperarios()
                        } finally {
                            loadingOperarios = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavalBlue,
                ),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Asignar Nuevo Trabajo")
            }
        }

        VerticalDivider()

        // ==========================================
        // PANEL DERECHO: VISOR DEL BARCO Y DETALLES
        // ==========================================
        Column(
            modifier = Modifier
                .weight(0.72f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            when (selectedSection) {
                "Trabajos" -> TrabajosScreen()
                "Incidencias activas" -> IncidenciasScreen()
                else -> {
                // Zona Superior: Visor de Bloques
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.35f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("[ ESQUEMA INTERACTIVO DE LA FRAGATA F110 - Detección de $selectedBlock ]")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Zona Inferior: Ficha de gestión del bloque seleccionado
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.65f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Detalle del Bloque: $selectedBlock",
                                style = MaterialTheme.typography.titleMedium
                            )
                            OutlinedButton(onClick = { onGenerateReport(selectedBlock) }) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Informe PDF")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Operarios en bloque: 4 (2 Soldadores, 2 Tuberos)")
                        Text("Isométricos pendientes de firma: 2")
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun OperariosPanel(
    operarios: List<String>,
    isLoading: Boolean,
    selectedOperario: String?,
    onSelectOperario: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Operarios disponibles",
                style = MaterialTheme.typography.titleSmall,
                color = NavalBlue,
            )
            Spacer(modifier = Modifier.height(8.dp))
            when {
                isLoading -> Text("Cargando operarios...")
                operarios.isEmpty() -> Text("No se encontraron operarios.")
                else -> operarios.forEach { operario ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (operario == selectedOperario) {
                                    NavalBlue
                                } else {
                                    Color.Transparent
                                },
                                shape = MaterialTheme.shapes.small,
                            )
                            .clickable { onSelectOperario(operario) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "• $operario",
                            color = if (operario == selectedOperario) {
                                Color.White
                            } else {
                                LocalContentColor.current
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionPreview(section: String) {
    val items = when (section) {
        "Trabajos" -> listOf("101", "102", "103", "104")
        "Operarios en buque" -> listOf(
            "Ana García",
            "Luis Martín",
            "Marta López",
            "Carlos Ruiz",
        )
        else -> listOf(
            "Retraso en la soldadura del bloque B322",
            "Falta material para la instalación eléctrica",
            "Revisión pendiente del isométrico 2",
            "Incidencia de seguridad en el taller",
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = section,
                style = MaterialTheme.typography.titleSmall,
                color = NavalBlue,
            )
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Text(
                    text = if (section == "Incidencias activas") item else "• $item",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun NavigationButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (isSelected) NavalBlue else Color.Transparent,
            contentColor = if (isSelected) Color.White else LocalContentColor.current,
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label)

        }

    }



}