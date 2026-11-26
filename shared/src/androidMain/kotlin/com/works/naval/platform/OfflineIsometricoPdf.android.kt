package com.works.naval.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private var offlinePdfContext: Context? = null

fun configureOfflineIsometricoPdfStorage(context: Context) {
    offlinePdfContext = context.applicationContext
}

private fun offlinePdfDirectory(): File =
    File(checkNotNull(offlinePdfContext) { "No se ha inicializado el almacenamiento de PDFs" }.filesDir, "isometricos")
        .apply { mkdirs() }

actual suspend fun writeOfflineIsometricoPdf(isometricoId: Long, pdfBytes: ByteArray, metadata: String) {
    withContext(Dispatchers.IO) {
        val directory = offlinePdfDirectory()
        val pdfFile = File(directory, "$isometricoId.pdf")
        val metadataFile = File(directory, "$isometricoId.json")
        val pdfTemp = File(directory, "$isometricoId.pdf.tmp")
        val metadataTemp = File(directory, "$isometricoId.json.tmp")

        try {
            pdfTemp.writeBytes(pdfBytes)
            metadataTemp.writeText(metadata)
            check(pdfFile.delete() || !pdfFile.exists()) { "No se pudo actualizar el PDF guardado" }
            check(pdfTemp.renameTo(pdfFile)) { "No se pudo guardar el PDF para usar sin conexión" }
            check(metadataFile.delete() || !metadataFile.exists()) { "No se pudieron actualizar los datos del isométrico" }
            check(metadataTemp.renameTo(metadataFile)) { "No se pudieron guardar los datos del isométrico" }
        } finally {
            pdfTemp.delete()
            metadataTemp.delete()
        }
    }
}

actual suspend fun readOfflineIsometricoPdf(isometricoId: Long): ByteArray? = withContext(Dispatchers.IO) {
    File(offlinePdfDirectory(), "$isometricoId.pdf").takeIf(File::isFile)?.readBytes()
}

actual suspend fun listOfflineIsometricoIds(): List<Long> = withContext(Dispatchers.IO) {
    offlinePdfDirectory().listFiles()
        ?.asSequence()
        ?.filter { it.isFile && it.extension == "pdf" }
        ?.mapNotNull { it.nameWithoutExtension.toLongOrNull() }
        ?.filter { id -> File(offlinePdfDirectory(), "$id.json").isFile }
        ?.sorted()
        ?.toList()
        .orEmpty()
}

actual suspend fun readOfflineIsometricoMetadata(isometricoId: Long): String? = withContext(Dispatchers.IO) {
    File(offlinePdfDirectory(), "$isometricoId.json").takeIf(File::isFile)?.readText()
}
