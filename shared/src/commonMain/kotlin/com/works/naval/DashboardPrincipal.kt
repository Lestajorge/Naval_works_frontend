package com.works.naval

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import navalworks_frontend.shared.generated.resources.Res
import navalworks_frontend.shared.generated.resources.LogoNavalWorks

import androidx.compose.ui.unit.dp

private val NavalBlue = Color(0xFF134386)

@Preview
@Composable
private fun DashboardPrincipalPreview() {
    MaterialTheme {
        DashboardPrincipal(
            onGenerateReport = {},
        )
    }
}

@Composable
fun DashboardPrincipal(
    onGenerateReport: (String) -> Unit,
) {
    var selectedBlock by remember { mutableStateOf("B322") } // Bloque seleccionado por defecto
    var selectedSection by remember { mutableStateOf("Trabajos") }
    var selectedLanguage by remember { mutableStateOf(UiLanguage.SPANISH) }
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    var showAddIncidenciaDialog by remember { mutableStateOf(false) }
    var trabajoSeleccionado by remember { mutableStateOf<TrabajoItem?>(null) }
    var incidencias by remember {
        mutableStateOf(
            listOf(
                "Retraso en la soldadura del bloque B322",
                "Falta material para la instalación eléctrica",
                "Revisión pendiente del isométrico 2",
                "Incidencia de seguridad en el taller",
            ),
        )
    }

    CompositionLocalProvider(LocalUiLanguage provides selectedLanguage) {
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
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NavalWorks",
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.headlineLarge,
                        color = NavalBlue
                    )
                    Text(
                        text = uiText(
                            "Supervisión de trabajos en bloques",
                            "Supervisión dos traballos en bloques",
                        ),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = { settingsMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = uiText("Configuración", "Axustes"),
                            tint = NavalBlue,
                        )
                    }
                    DropdownMenu(
                        expanded = settingsMenuExpanded,
                        onDismissRequest = { settingsMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(uiText("Idioma", "Idioma")) },
                            onClick = {},
                            enabled = false,
                        )
                        DropdownMenuItem(
                            text = { Text("Español") },
                            leadingIcon = if (selectedLanguage == UiLanguage.SPANISH) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else {
                                null
                            },
                            onClick = {
                                selectedLanguage = UiLanguage.SPANISH
                                settingsMenuExpanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Galego") },
                            leadingIcon = if (selectedLanguage == UiLanguage.GALICIAN) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else {
                                null
                            },
                            onClick = {
                                selectedLanguage = UiLanguage.GALICIAN
                                settingsMenuExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(uiText("ESTADO DE LA F-110", "ESTADO DA F-110"), style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(uiText("Avance global: 68%", "Progreso global: 68%"), style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(
                        progress = { 0.68f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = NavalBlue,
                    )
                    Text(
                        uiText(
                            "⚠️ ${incidencias.size} incidencias en taller",
                            "⚠️ ${incidencias.size} incidencias no taller",
                        ),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            NavigationButton(
                icon = Icons.Default.ViewInAr,
                label = uiText("Trabajos", "Traballos"),
                isSelected = selectedSection == "Trabajos",
            ) {
                selectedSection = "Trabajos"
            }
            NavigationButton(
                icon = Icons.Default.Group,
                label = uiText("Operarios en buque", "Persoal a bordo"),
                isSelected = selectedSection == "Operarios en buque",
            ) {
                selectedSection = "Operarios en buque"
                trabajoSeleccionado = null
            }
            NavigationButton(
                icon = Icons.Default.Warning,
                label = uiText("Incidencias activas", "Incidencias activas"),
                isSelected = selectedSection == "Incidencias activas",
            ) {
                selectedSection = "Incidencias activas"
                trabajoSeleccionado = null
            }

            if (selectedSection == "Trabajos") {
                trabajoSeleccionado?.let { trabajo ->
                    Spacer(modifier = Modifier.height(8.dp))
                    OperariosAsignadosPanel(trabajo)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        selectedSection = "Asignar trabajo"
                        trabajoSeleccionado = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavalBlue,
                    ),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(uiText("Asignar Nuevo Trabajo", "Asignar novo traballo"))
                }
                Button(
                    onClick = { showAddIncidenciaDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(uiText("Añadir nueva incidencia", "Engadir unha incidencia nova"))
                }
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
                .padding(
                    start = 16.dp,
                    top = if (selectedSection == "Operarios en buque") 0.dp else 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                )
        ) {
            when (selectedSection) {
                "Trabajos" -> TrabajosScreen(
                    onTrabajoSeleccionado = { trabajoSeleccionado = it },
                )
                "Asignar trabajo" -> CrearTrabajoScreen(
                    onTrabajoCreado = {
                        selectedSection = "Trabajos"
                        trabajoSeleccionado = null
                    },
                    onCancelar = {
                        selectedSection = "Trabajos"
                        trabajoSeleccionado = null
                    },
                )
                "Incidencias activas" -> IncidenciasScreen(
                    incidencias = incidencias,
                    onDeleteIncidencia = { incidencia ->
                        incidencias = incidencias - incidencia
                    },
                )
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.375f),
                    ) {
                        LogoNavalWorksImage()
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiText(
                                        "Detalle del bloque: $selectedBlock",
                                        "Detalle do bloque: $selectedBlock",
                                    ),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                OutlinedButton(onClick = { onGenerateReport(selectedBlock) }) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(uiText("Informe PDF", "Informe PDF"))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                uiText(
                                    "Operarios en bloque: 4 (2 soldadores, 2 tuberos)",
                                    "Persoal no bloque: 4 (2 soldadores, 2 tubistas)",
                                ),
                            )
                            Text(
                                uiText(
                                    "Isométricos pendientes de firma: 2",
                                    "Isométricos pendentes de sinatura: 2",
                                ),
                            )
                        }
                    }
                }
            }
        }
        if (showAddIncidenciaDialog) {
            AddIncidenciaDialog(
                onDismiss = { showAddIncidenciaDialog = false },
                onAdd = { descripcion ->
                    incidencias = incidencias + descripcion
                    showAddIncidenciaDialog = false
                },
            )
        }
    }
}
}

@Composable
private fun LogoNavalWorksImage() {
    Image(
        painter = painterResource(Res.drawable.LogoNavalWorks),
        contentDescription = uiText("Logotipo de NavalWorks", "Logotipo de NavalWorks"),
        modifier = Modifier
            .fillMaxSize(),
        contentScale = ContentScale.Fit,
    )
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