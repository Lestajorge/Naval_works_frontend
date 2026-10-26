package com.works.naval

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import android.provider.OpenableColumns
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
actual fun rememberPdfFilePicker(
    onPdfSelected: (PdfFileSelection) -> Unit,
    onError: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val selectedFile = withContext(Dispatchers.IO) {
                        val fileName = context.contentResolver.query(
                            uri,
                            arrayOf(OpenableColumns.DISPLAY_NAME),
                            null,
                            null,
                            null,
                        )?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                cursor.getString(0)
                            } else {
                                null
                            }
                        } ?: uri.lastPathSegment?.substringAfterLast('/') ?: "isometrico.pdf"
                        val bytes = context.contentResolver.openInputStream(uri)
                            ?.use { it.readBytes() }
                            ?: throw IOException("No se pudo leer el archivo seleccionado")
                        PdfFileSelection(fileName, bytes)
                    }
                    onPdfSelected(selectedFile)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    onError(exception.message ?: "No se pudo leer el PDF seleccionado")
                }
            }
        }
    }

    return { launcher.launch(arrayOf("application/pdf")) }
}
