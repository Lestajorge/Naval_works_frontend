package com.works.naval

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPdfFilePicker(
    onPdfSelected: (PdfFileSelection) -> Unit,
    onError: (String) -> Unit,
): () -> Unit
