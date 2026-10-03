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
actual fun PdfViewer(pdfPath: String, modifier: Modifier) {
    SwingPanel(
        factory = { PdfPanel(pdfPath) },
        modifier = modifier,
    )
}

private class PdfPanel(
    private val pdfPath: String,
) : JPanel() {
    private var pageImage: java.awt.image.BufferedImage? = null
    private var errorMessage: String? = null

    init {
        background = Color.DARK_GRAY
        loadFirstPage()
    }

    private fun loadFirstPage() {
        try {
            val resource = javaClass.classLoader.getResourceAsStream(pdfPath)
                ?: throw IOException("No se encontró el recurso PDF: $pdfPath")

            resource.use { input ->
                Loader.loadPDF(input.readBytes()).use { document ->
                    pageImage = PDFRenderer(document).renderImageWithDPI(0, 120f)
                }
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
