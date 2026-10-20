package com.works.naval


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException

@Composable
fun IsometricoDetalleVisor(
    item: TrabajoItem,
    onVolver: () -> Unit
) {
    var pdfBytes by remember(item.isometricoId) { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember(item.isometricoId) { mutableStateOf(true) }
    var errorMessage by remember(item.isometricoId) { mutableStateOf<String?>(null) }
    var refreshKey by remember(item.isometricoId) { mutableStateOf(0) }
    val language = LocalUiLanguage.current

    LaunchedEffect(item.isometricoId, refreshKey) {
        isLoading = true
        errorMessage = null
        try {
            pdfBytes = fetchIsometricoPdf(item.isometricoId)
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
            Column {
                Text(
                    text = item.codigoIsometrico,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiText(
                        "Trabajo #${item.id} • Bloque ${item.bloque} • ${item.subsistema}",
                        "Traballo #${item.id} • Bloque ${item.bloque} • ${item.subsistema}",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
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