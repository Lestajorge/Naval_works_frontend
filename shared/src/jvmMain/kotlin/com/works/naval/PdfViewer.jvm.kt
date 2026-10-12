package com.works.naval

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.io.IOException
import javax.swing.JPanel

@Composable
actual fun PdfViewer(pdfBytes: ByteArray?, modifier: Modifier) {
    val panel = androidx.compose.runtime.remember { PdfPanel() }
    SwingPanel(
        factory = { panel },
        modifier = modifier,
        update = { it.loadPdf(pdfBytes) },
    )
}

private class PdfPanel : JPanel() {
    private var pageImage: java.awt.image.BufferedImage? = null
    private var errorMessage: String? = null
    private var loadedBytes: ByteArray? = null

    init {
        background = Color.DARK_GRAY
    }

    fun loadPdf(bytes: ByteArray?) {
        if (bytes == null || bytes === loadedBytes) return
        loadedBytes = bytes
        pageImage = null
        errorMessage = null
        try {
            Loader.loadPDF(bytes).use { document ->
                pageImage = PDFRenderer(document).renderImageWithDPI(0, 120f)
            }
        } catch (exception: IOException) {
            errorMessage = exception.message ?: "No se pudo cargar el PDF"
        }
        repaint()
    }

    override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)
        val image = pageImage
        if (image == null) {
            graphics.color = Color.WHITE
            graphics.drawString(errorMessage ?: "Cargando PDF...", 24, 32)
            return
        }

        val graphics2D = graphics as Graphics2D
        graphics2D.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,
        )
        val scale = minOf(
            width.toDouble() / image.width,
            height.toDouble() / image.height,
        )
        val targetWidth = (image.width * scale).toInt()
        val targetHeight = (image.height * scale).toInt()
        val left = (width - targetWidth) / 2
        val top = (height - targetHeight) / 2
        graphics2D.drawImage(image, left, top, targetWidth, targetHeight, null)
    }
}
