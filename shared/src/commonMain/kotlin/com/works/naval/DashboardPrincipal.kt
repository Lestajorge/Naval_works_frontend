package com.works.naval

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import navalworks_frontend.shared.generated.resources.Res
import navalworks_frontend.shared.generated.resources.LogoNavalWorks

import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val NavalBlue = Color(0xFF134386)

@Preview(name = "Móvil", widthDp = 360, heightDp = 800)
@Preview(name = "Tablet vertical", widthDp = 800, heightDp = 1280)
@Preview(name = "Tablet horizontal", widthDp = 1280, heightDp = 800)
@Composable
private fun DashboardPrincipalPreview() {
    MaterialTheme {
        DashboardPrincipal(
            preferences = PreviewAppPreferences(),
            onGenerateReport = {},
        )
    }
}

@Composable
fun DashboardPrincipal(
    preferences: AppPreferences,
    onGenerateReport: (String) -> Unit,
) {
    var selectedBlock by remember { mutableStateOf("B322") } // Bloque seleccionado por defecto
    var selectedSection by remember { mutableStateOf("Trabajos") }
    var selectedLanguage by remember(preferences) { mutableStateOf(UiLanguage.SPANISH) }
    var isLanguageLoaded by remember(preferences) { mutableStateOf(false) }
    var preferenceError by remember(preferences) { mutableStateOf<PreferenceError?>(null) }
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var showAssignedOperariosDialog by remember { mutableStateOf(false) }
    var administrator by remember { mutableStateOf<String?>(null) }
    var showAddIncidenciaDialog by remember { mutableStateOf(false) }
    var trabajoSeleccionado by remember { mutableStateOf<TrabajoItem?>(null) }
    var incidencias by remember {
        mutableStateOf(
            listOf(
                IncidenciaItem("", "Retraso en la soldadura del bloque B322"),
                IncidenciaItem("", "Falta material para la instalación eléctrica"),
                IncidenciaItem("", "Revisión pendiente del isométrico 2"),
                IncidenciaItem("", "Incidencia de seguridad en el taller"),
            ),
        )
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(preferences) {
        try {
            selectedLanguage = preferences.readUiLanguage() ?: UiLanguage.SPANISH
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            preferenceError = PreferenceError.LOAD
        } finally {
            isLanguageLoaded = true
        }
    }

    fun selectLanguage(language: UiLanguage) {
        selectedLanguage = language
        settingsMenuExpanded = false
        scope.launch {
            try {
                preferences.writeUiLanguage(language)
                preferenceError = null
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                preferenceError = PreferenceError.SAVE
            }
        }
    }

    CompositionLocalProvider(LocalUiLanguage provides selectedLanguage) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val compactLayout = maxWidth < 840.dp
            if (compactLayout) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF00141F)),
                        ) {
                            LogoNavalWorksImage()
                        }
                        administrator?.let {
                            Text(
                                text = it,
                                modifier = Modifier.widthIn(max = 96.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                            )
                        }
                        IconButton(
                            onClick = {
                                if (administrator == null) {
                                    showAdminLoginDialog = true
                                } else {
                                    logoutAdministrador()
                                    administrator = null
                                    if (selectedSection == "Asignar trabajo" ||
                                        selectedSection == "Administrar" ||
                                        selectedSection == "Isométricos"
                                    ) {
                                        selectedSection = "Trabajos"
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (administrator == null) {
                                    Icons.Default.AdminPanelSettings
                                } else {
                                    Icons.AutoMirrored.Filled.Logout
                                },
                                contentDescription = if (administrator == null) {
                                    uiText("Entrar en modo administrador", "Entrar no modo administrador")
                                } else {
                                    uiText("Salir del modo administrador", "Saír do modo administrador")
                                },
                                tint = if (administrator == null) NavalBlue else Color(0xFF2E7D32),
                            )
                        }
                        Box {
                            IconButton(
                                onClick = { settingsMenuExpanded = true },
                                enabled = isLanguageLoaded,
                            ) {
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
                                    onClick = { selectLanguage(UiLanguage.SPANISH) },
                                    enabled = isLanguageLoaded,
                                )
                                DropdownMenuItem(
                                    text = { Text("Galego") },
                                    leadingIcon = if (selectedLanguage == UiLanguage.GALICIAN) {
                                        { Icon(Icons.Default.Check, contentDescription = null) }
                                    } else {
                                        null
                                    },
                                    onClick = { selectLanguage(UiLanguage.GALICIAN) },
                                    enabled = isLanguageLoaded,
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    uiText("ESTADO DE LA F-110", "ESTADO DA F-110"),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Text(
                                    uiText("Avance global: 68%", "Progreso global: 68%"),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            LinearProgressIndicator(
                                progress = { 0.68f },
                                modifier = Modifier.weight(1f),
                                color = NavalBlue,
                            )
                            Text(
                                "${incidencias.size}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NavigationButton(
                            icon = Icons.Default.ViewInAr,
                            label = uiText("Trabajos", "Traballos"),
                            isSelected = selectedSection == "Trabajos",
                        ) { selectedSection = "Trabajos" }
                        NavigationButton(
                            icon = Icons.Default.Group,
                            label = uiText("Resumen de bloques", "Resumo de bloques"),
                            isSelected = selectedSection == "Resumen de bloques",
                        ) {
                            selectedSection = "Resumen de bloques"
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
                        if (administrator != null) {
                            NavigationButton(
                                icon = Icons.Default.AdminPanelSettings,
                                label = uiText("Administrar", "Administrar"),
                                isSelected = selectedSection == "Administrar",
                            ) {
                                selectedSection = "Administrar"
                                trabajoSeleccionado = null
                            }
                        }
                        if (administrator != null) {
                            NavigationButton(
                                icon = Icons.Default.PictureAsPdf,
                                label = uiText("Isométricos", "Isométricos"),
                                isSelected = selectedSection == "Isométricos",
                            ) {
                                selectedSection = "Isométricos"
                                trabajoSeleccionado = null
                            }
                        }
                    }
                    if (selectedSection == "Trabajos" && trabajoSeleccionado != null) {
                        TextButton(
                            onClick = { showAssignedOperariosDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(uiText("Ver operarios asignados", "Ver operarios asignados"))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                selectedSection = "Asignar trabajo"
                                trabajoSeleccionado = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavalBlue),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                uiText("Trabajo", "Traballo"),
                                maxLines = 1,
                            )
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
                            Text(
                                uiText("Incidencia", "Incidencia"),
                                maxLines = 1,
                            )
                        }
                    }
                    HorizontalDivider()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp),
                    ) {
                        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                            DashboardSectionContent(
                                selectedSection = selectedSection,
                                selectedBlock = selectedBlock,
                                administrator = administrator,
                                incidencias = incidencias,
                                onTrabajoSeleccionado = { trabajoSeleccionado = it },
                                onSectionSelected = { selectedSection = it },
                                onClearTrabajo = { trabajoSeleccionado = null },
                                onIncidenciasChanged = { incidencias = it },
                                onBlockSelected = { selectedBlock = it },
                                onGenerateReport = onGenerateReport,
                            )
                        }
                    }
                }
            } else {
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF00141F),
                ),
            ) {
                LogoNavalWorksImage()
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                administrator?.let {
                    Text(
                        text = uiText("Admin: $it", "Admin: $it"),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                    )
                }
                IconButton(
                    onClick = {
                        if (administrator == null) {
                            showAdminLoginDialog = true
                        } else {
                            logoutAdministrador()
                            administrator = null
                            if (selectedSection == "Asignar trabajo") {
                                selectedSection = "Trabajos"
                            }
                            if (selectedSection == "Administrar") {
                                selectedSection = "Trabajos"
                            }
                            if (selectedSection == "Isométricos") {
                                selectedSection = "Trabajos"
                            }
                        }
                    },
                ) {
                    Icon(
                        imageVector = if (administrator == null) {
                            Icons.Default.AdminPanelSettings
                        } else {
                            Icons.AutoMirrored.Filled.Logout
                        },
                        contentDescription = if (administrator == null) {
                            uiText("Entrar en modo administrador", "Entrar no modo administrador")
                        } else {
                            uiText("Salir del modo administrador", "Saír do modo administrador")
                        },
                        tint = if (administrator == null) NavalBlue else Color(0xFF2E7D32),
                    )
                }
                Box {
                    IconButton(
                        onClick = { settingsMenuExpanded = true },
                        enabled = isLanguageLoaded,
                    ) {
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
                            onClick = { selectLanguage(UiLanguage.SPANISH) },
                            enabled = isLanguageLoaded,
                        )
                        DropdownMenuItem(
                            text = { Text("Galego") },
                            leadingIcon = if (selectedLanguage == UiLanguage.GALICIAN) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else {
                                null
                            },
                            onClick = { selectLanguage(UiLanguage.GALICIAN) },
                            enabled = isLanguageLoaded,
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
                label = uiText("Resumen de bloques", "Resumo de bloques"),
                isSelected = selectedSection == "Resumen de bloques",
            ) {
                selectedSection = "Resumen de bloques"
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
            if (administrator != null) {
                Card(
                    onClick = {
                        selectedSection = "Administrar"
                        trabajoSeleccionado = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedSection == "Administrar") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = if (selectedSection == "Administrar") {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiText("Administrar", "Administrar"),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedSection == "Administrar") {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                        )
                    }
                }
                NavigationButton(
                    icon = Icons.Default.PictureAsPdf,
                    label = uiText("Isométricos", "Isométricos"),
                    isSelected = selectedSection == "Isométricos",
                ) {
                    selectedSection = "Isométricos"
                    trabajoSeleccionado = null
                }
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
            DashboardSectionContent(
                selectedSection = selectedSection,
                selectedBlock = selectedBlock,
                administrator = administrator,
                incidencias = incidencias,
                onTrabajoSeleccionado = { trabajoSeleccionado = it },
                onSectionSelected = { selectedSection = it },
                onClearTrabajo = { trabajoSeleccionado = null },
                onIncidenciasChanged = { incidencias = it },
                onBlockSelected = { selectedBlock = it },
                onGenerateReport = onGenerateReport,
            )
        }
                }
            }
        if (showAddIncidenciaDialog) {
            AddIncidenciaDialog(
                onDismiss = { showAddIncidenciaDialog = false },
                onAdd = { nombre, descripcion ->
                    incidencias = incidencias + IncidenciaItem(nombre, descripcion)
                    showAddIncidenciaDialog = false
                },
            )
        }
        if (showAdminLoginDialog) {
            AdminLoginDialog(
                onDismiss = { showAdminLoginDialog = false },
                onAuthenticated = {
                    administrator = it
                    showAdminLoginDialog = false
                },
            )
        }
        if (showAssignedOperariosDialog) {
            trabajoSeleccionado?.let { trabajo ->
                AlertDialog(
                    onDismissRequest = { showAssignedOperariosDialog = false },
                    title = {
                        Text(
                            uiText(
                                "Operarios del trabajo #${trabajo.id}",
                                "Operarios do traballo #${trabajo.id}",
                            ),
                        )
                    },
                    text = { OperariosAsignadosPanel(trabajo) },
                    confirmButton = {
                        TextButton(onClick = { showAssignedOperariosDialog = false }) {
                            Text(uiText("Cerrar", "Pechar"))
                        }
                    },
                )
            }
        }
        preferenceError?.let { error ->
            AlertDialog(
                onDismissRequest = { preferenceError = null },
                title = { Text(uiText("Preferencias", "Preferencias")) },
                text = {
                    Text(
                        when (error) {
                            PreferenceError.LOAD -> uiText(
                                "No se pudieron cargar las preferencias guardadas.",
                                "Non se puideron cargar as preferencias gardadas.",
                            )
                            PreferenceError.SAVE -> uiText(
                                "No se pudo guardar el idioma seleccionado.",
                                "Non se puido gardar o idioma seleccionado.",
                            )
                        },
                    )
                },
                confirmButton = {
                    TextButton(onClick = { preferenceError = null }) {
                        Text(uiText("Aceptar", "Aceptar"))
                    }
                },
            )
        }
    }
}
}

@Composable
private fun DashboardSectionContent(
    selectedSection: String,
    selectedBlock: String,
    administrator: String?,
    incidencias: List<IncidenciaItem>,
    onTrabajoSeleccionado: (TrabajoItem?) -> Unit,
    onSectionSelected: (String) -> Unit,
    onClearTrabajo: () -> Unit,
    onIncidenciasChanged: (List<IncidenciaItem>) -> Unit,
    onBlockSelected: (String) -> Unit,
    onGenerateReport: (String) -> Unit,
) {
    when (selectedSection) {
        "Trabajos" -> TrabajosScreen(
            onTrabajoSeleccionado = onTrabajoSeleccionado,
        )
        "Asignar trabajo" -> CrearTrabajoScreen(
            onTrabajoCreado = {
                onSectionSelected("Trabajos")
                onClearTrabajo()
            },
            onCancelar = {
                onSectionSelected("Trabajos")
                onClearTrabajo()
            },
        )
        "Incidencias activas" -> IncidenciasScreen(
            incidencias = incidencias,
            onDeleteIncidencia = { incidencia ->
                onIncidenciasChanged(incidencias - incidencia)
            },
        )
        "Administrar" -> if (administrator != null) {
            GestionOperariosScreen()
        } else {
            Text(
                text = uiText(
                    "Se requiere acceso de administrador.",
                    "Requírese acceso de administrador.",
                ),
                color = MaterialTheme.colorScheme.error,
            )
        }
        "Isométricos" -> if (administrator != null) {
            GestionIsometricosScreen()
        } else {
            Text(
                text = uiText(
                    "Se requiere acceso de administrador.",
                    "Requírese acceso de administrador.",
                ),
                color = MaterialTheme.colorScheme.error,
            )
        }
        "Resumen de bloques" -> ResumenBloquesScreen(
            selectedBlock = selectedBlock,
            onBlockSelected = onBlockSelected,
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
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF00141F),
                ),
            ) {
                LogoNavalWorksImage()
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = uiText(
                                "Detalle del bloque: $selectedBlock",
                                "Detalle do bloque: $selectedBlock",
                            ),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        OutlinedButton(onClick = { onGenerateReport(selectedBlock) }) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
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

private enum class PreferenceError {
    LOAD,
    SAVE,
}

private class PreviewAppPreferences : AppPreferences {
    override suspend fun readUiLanguage(): UiLanguage = UiLanguage.SPANISH

    override suspend fun writeUiLanguage(language: UiLanguage) = Unit

    override suspend fun readServerConnectionSettings(): ServerConnectionSettings? = null

    override suspend fun writeServerConnectionSettings(settings: ServerConnectionSettings) = Unit
}

@Composable
private fun ResumenBloquesScreen(
    selectedBlock: String,
    onBlockSelected: (String) -> Unit,
) {
    var bloques by remember { mutableStateOf<List<BloqueOption>>(emptyList()) }
    var trabajos by remember { mutableStateOf<List<TrabajoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var expandedZones by remember { mutableStateOf<Set<String>>(emptySet()) }
    val language = LocalUiLanguage.current

    LaunchedEffect(refreshKey) {
        isLoading = true
        errorMessage = null
        try {
            bloques = fetchBloques().sortedWith(
                compareBy<BloqueOption> { it.zona }.thenBy { it.codigo },
            )
            trabajos = fetchTrabajos()
        } catch (exception: kotlinx.coroutines.CancellationException) {
            throw exception
        } catch (exception: Exception) {
            errorMessage = exception.message ?: language.text(
                "No se pudieron cargar los bloques",
                "Non se puideron cargar os bloques",
            )
        } finally {
            isLoading = false
        }
    }

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
        bloques.isEmpty() -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(uiText("No hay bloques disponibles", "Non hai bloques dispoñibles"))
        }
        else -> Column(modifier = Modifier.fillMaxSize()) {
            val trabajosPorBloque = trabajos.groupBy { it.bloque }
            val bloquesPorZona = bloques.groupBy { it.zona }.toSortedMap()
            Text(
                text = uiText("Resumen de bloques", "Resumo de bloques"),
                style = MaterialTheme.typography.headlineSmall,
                color = NavalBlue,
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                bloquesPorZona.forEach { (zona, bloquesZona) ->
                    val isExpanded = zona in expandedZones
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            onClick = {
                                expandedZones = if (isExpanded) {
                                    expandedZones - zona
                                } else {
                                    expandedZones + zona
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = uiText(
                                        "Zona $zona · ${bloquesZona.size} bloques",
                                        "Zona $zona · ${bloquesZona.size} bloques",
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NavalBlue,
                                )
                                Icon(
                                    imageVector = if (isExpanded) {
                                        Icons.Default.ExpandLess
                                    } else {
                                        Icons.Default.ExpandMore
                                    },
                                    contentDescription = if (isExpanded) {
                                        uiText("Contraer zona", "Contraer zona")
                                    } else {
                                        uiText("Mostrar bloques", "Mostrar bloques")
                                    },
                                    tint = NavalBlue,
                                )
                            }
                        }
                    }
                    if (isExpanded) items(bloquesZona) { bloque ->
                        val isSelected = bloque.codigo == selectedBlock
                        val trabajosDelBloque = trabajosPorBloque[bloque.codigo].orEmpty()
                        val trabajosPorEstado = trabajosDelBloque.groupingBy { it.estado }.eachCount()
                        val trabajosAceptados = trabajosPorEstado[EstadoTrabajo.ACEPTADO] ?: 0
                        val porcentajeCompletado = if (trabajosDelBloque.isEmpty()) {
                            0
                        } else {
                            trabajosAceptados * 100 / trabajosDelBloque.size
                        }
                        Card(
                            onClick = { onBlockSelected(bloque.codigo) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF2E7D32) else Color(0xFFE8F5E9),
                            ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = bloque.codigo,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) Color.White else Color(0xFF2E7D32),
                                )
                                EstadoTrabajo.values().forEach { estado ->
                                    val estadoColor = estadoColors(estado)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = if (language == UiLanguage.GALICIAN) {
                                                estado.descripcionGallega
                                            } else {
                                                estado.descripcion
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White else estadoColor.content,
                                        )
                                        Text(
                                            text = (trabajosPorEstado[estado] ?: 0).toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White else estadoColor.content,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$porcentajeCompletado%",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) Color.White else Color(0xFF2E7D32),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiText("Estado de trabajos", "Estado dos traballos"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else Color(0xFF2E7D32),
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                ) {
                                    EstadoTrabajo.values().forEach { estado ->
                                        val cantidad = trabajosPorEstado[estado] ?: 0
                                        if (cantidad > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(cantidad.toFloat())
                                                    .fillMaxHeight()
                                                    .background(estadoColors(estado).background),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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