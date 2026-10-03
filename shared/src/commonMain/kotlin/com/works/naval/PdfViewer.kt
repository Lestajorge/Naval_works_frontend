package com.works.naval

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PdfViewer(
    pdfPath: String,
    modifier: Modifier = Modifier,
)
