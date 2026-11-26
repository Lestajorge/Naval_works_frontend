package com.works.naval.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Path
import java.nio.file.Paths

private fun offlinePdfDirectory(): Path = Paths.get(
    System.getProperty("user.home"),
    ".navalworks",
    "isometricos",
).also { Files.createDirectories(it) }

actual suspend fun writeOfflineIsometricoPdf(isometricoId: Long, pdfBytes: ByteArray, metadata: String) {
    withContext(Dispatchers.IO) {
        val directory = offlinePdfDirectory()
        writeAtomically(directory.resolve("$isometricoId.pdf"), pdfBytes)
        writeAtomically(directory.resolve("$isometricoId.json"), metadata.toByteArray(Charsets.UTF_8))
    }
}

private fun writeAtomically(destination: Path, bytes: ByteArray) {
    val temporary = Files.createTempFile(destination.parent, destination.fileName.toString(), ".tmp")
    try {
        Files.write(temporary, bytes)
        try {
            Files.move(temporary, destination, ATOMIC_MOVE, REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary, destination, REPLACE_EXISTING)
        }
    } finally {
        Files.deleteIfExists(temporary)
    }
}

actual suspend fun readOfflineIsometricoPdf(isometricoId: Long): ByteArray? = withContext(Dispatchers.IO) {
    offlinePdfDirectory().resolve("$isometricoId.pdf").toFile().takeIf(File::isFile)?.readBytes()
}

actual suspend fun listOfflineIsometricoIds(): List<Long> = withContext(Dispatchers.IO) {
    offlinePdfDirectory().toFile().listFiles()
        ?.asSequence()
        ?.filter { it.isFile && it.extension == "pdf" }
        ?.mapNotNull { it.nameWithoutExtension.toLongOrNull() }
        ?.filter { Files.isRegularFile(offlinePdfDirectory().resolve("$it.json")) }
        ?.sorted()
        ?.toList()
        .orEmpty()
}

actual suspend fun readOfflineIsometricoMetadata(isometricoId: Long): String? = withContext(Dispatchers.IO) {
    offlinePdfDirectory().resolve("$isometricoId.json").toFile().takeIf(File::isFile)?.readText()
}
