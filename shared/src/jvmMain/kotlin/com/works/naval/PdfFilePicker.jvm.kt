package com.works.naval

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberPdfFilePicker(
    onPdfSelected: (PdfFileSelection) -> Unit,
    onError: (String) -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    return {
        val picker = JFileChooser().apply {
            dialogTitle = "Seleccionar isométrico PDF"
            isAcceptAllFileFilterUsed = false
            fileFilter = FileNameExtensionFilter("Documentos PDF", "pdf")
        }
        if (picker.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val selectedFile: File = picker.selectedFile
            scope.launch {
                try {
                    val bytes = withContext(Dispatchers.IO) { selectedFile.readBytes() }
                    onPdfSelected(PdfFileSelection(selectedFile.name, bytes))
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    onError(exception.message ?: "No se pudo leer el PDF seleccionado")
                }
            }
        }
    }
}
