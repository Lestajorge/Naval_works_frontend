package com.works.naval

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.View
import com.works.naval.shared.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import java.io.IOException

@Composable
actual fun PdfViewer(pdfPath: String, modifier: Modifier) {
    val context = LocalContext.current
    val view = androidx.compose.runtime.remember { PdfDocumentView(context) }

    DisposableEffect(view) {
        onDispose { view.close() }
    }

    AndroidView(
        factory = { view },
        modifier = modifier,
        update = { it.loadPdf(pdfPath) },
    )
}

private class PdfDocumentView(
    private val context: Context,
) : View(context) {
    private var renderer: PdfRenderer? = null
    private var pageBitmap: Bitmap? = null
    private var loadedPath: String? = null
    private var errorMessage: String? = null
    private val errorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }

    fun loadPdf(path: String) {
        if (path == loadedPath) return
        loadedPath = path
        closeDocument()

        try {
            val file = File(context.cacheDir, path.substringAfterLast('/'))
            if (!file.exists()) {
                context.resources.openRawResource(R.raw.sample_plan).use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }

            renderer = PdfRenderer(
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY),
            )
            if (renderer?.pageCount == 0) {
                errorMessage = "El PDF no contiene páginas"
            } else {
                renderPage()
            }
        } catch (exception: IOException) {
            errorMessage = "No se pudo cargar el PDF: ${exception.message}"
        } catch (exception: IllegalArgumentException) {
            errorMessage = "No se pudo cargar el PDF: ${exception.message}"
        }
        invalidate()
    }

    private fun renderPage() {
        val pdfRenderer = renderer ?: return
        val page = pdfRenderer.openPage(0)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        pageBitmap = bitmap
        errorMessage = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.DKGRAY)

        val bitmap = pageBitmap
        if (bitmap != null) {
            val scale = minOf(
                width.toFloat() / bitmap.width,
                height.toFloat() / bitmap.height,
            )
            val targetWidth = bitmap.width * scale
            val targetHeight = bitmap.height * scale
            val left = (width - targetWidth) / 2f
            val top = (height - targetHeight) / 2f
            canvas.drawBitmap(
                bitmap,
                null,
                android.graphics.RectF(left, top, left + targetWidth, top + targetHeight),
                null,
            )
        } else if (errorMessage != null) {
            canvas.drawText(errorMessage!!, width / 2f, height / 2f, errorPaint)
        }
    }

    fun close() {
        closeDocument()
        pageBitmap?.recycle()
        pageBitmap = null
    }

    private fun closeDocument() {
        renderer?.close()
        renderer = null
        pageBitmap?.recycle()
        pageBitmap = null
    }
}
