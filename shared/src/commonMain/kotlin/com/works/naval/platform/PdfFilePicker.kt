package com.works.naval.platform

import com.works.naval.domain.model.PdfFileSelection

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPdfFilePicker(
    onPdfSelected: (PdfFileSelection) -> Unit,
    onError: (String) -> Unit,
): () -> Unit
