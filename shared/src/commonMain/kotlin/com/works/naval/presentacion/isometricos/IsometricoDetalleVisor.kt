package com.works.naval.presentacion.isometricos

import com.works.naval.presentacion.trabajos.EstadoColorWork

import com.works.naval.core.localization.*
import com.works.naval.data.preferences.*
import com.works.naval.data.remote.*
import com.works.naval.domain.model.*
import com.works.naval.platform.*


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun IsometricoDetalleVisor(
    item: TrabajoItem,
    onVolver: () -> Unit
) {
    var pdfBytes by remember(item.isometricoId) { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember(item.isometricoId) { mutableStateOf(true) }
    var errorMessage by remember(item.isometricoId) { mutableStateOf<String?>(null) }
    var refreshKey by remember(item.isometricoId) { mutableStateOf(0) }
    var hasOfflineCopy by remember(item.isometricoId) { mutableStateOf(false) }
    var isUsingOfflineCopy by remember(item.isometricoId) { mutableStateOf(false) }
    var isSavingOfflineCopy by remember(item.isometricoId) { mutableStateOf(false) }
    var storageMessage by remember(item.isometricoId) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val language = LocalUiLanguage.current

    LaunchedEffect(item.isometricoId, refreshKey) {
        isLoading = true
        errorMessage = null
        try {
            val offlinePdf = readOfflineIsometricoPdf(item.isometricoId)
            hasOfflineCopy = offlinePdf != null
            try {
                pdfBytes = fetchIsometricoPdf(item.isometricoId)
                isUsingOfflineCopy = false
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (offlinePdf == null) throw exception
                pdfBytes = offlinePdf
                isUsingOfflineCopy = true
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            errorMessage = exception.message ?: language.text(
                "No se pudo cargar el PDF",
                "Non se puido cargar o PDF",
            )
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cabecera con botón de retroceso y datos del isométrico
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = uiText("Volver a la lista", "Volver á lista")
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.codigoIsometrico,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = uiText(
                        "Trabajo #${item.id} • Bloque ${item.bloque}",
                        "Traballo #${item.id} • Bloque ${item.bloque}",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    val bytes = pdfBytes ?: return@Button
                    isSavingOfflineCopy = true
                    storageMessage = null
                    scope.launch {
                        try {
                            saveOfflineIsometricoPdf(item, bytes)
                            hasOfflineCopy = true
                            isUsingOfflineCopy = false
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            storageMessage = exception.message ?: language.text(
                                "No se pudo guardar el PDF en el dispositivo",
                                "Non se puido gardar o PDF no dispositivo",
                            )
                        } finally {
                            isSavingOfflineCopy = false
                        }
                    }
                },
                enabled = pdfBytes != null && !isSavingOfflineCopy,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    when {
                        isSavingOfflineCopy -> uiText("Guardando...", "Gardando...")
                        hasOfflineCopy -> uiText("Actualizar copia sin conexión", "Actualizar copia sen conexión")
                        else -> uiText("Descargar para usar sin conexión", "Descargar para usar sen conexión")
                    },
                )
            }
        }
        if (isUsingOfflineCopy) {
            Text(
                text = uiText("PDF abierto desde el dispositivo (sin conexión)", "PDF aberto desde o dispositivo (sen conexión)"),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        storageMessage?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        ) {
            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                errorMessage != null -> Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = Color.White,
                    )
                    Button(onClick = { refreshKey++ }) {
                        Text(uiText("Reintentar", "Tentar de novo"))
                    }
                }
                else -> PdfViewer(
                    pdfBytes = pdfBytes,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Estado actual del isométrico
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(uiText("Estado actual:", "Estado actual:"), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            EstadoColorWork(estado = item.estado)
        }
    }
}