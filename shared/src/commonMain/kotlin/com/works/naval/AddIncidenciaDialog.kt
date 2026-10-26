package com.works.naval

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun AddIncidenciaDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(uiText("Añadir nueva incidencia", "Engadir unha incidencia nova")) },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(uiText("Nombre", "Nome")) },
                    singleLine = true,
                )
                Text(uiText("Describe la incidencia:", "Describe a incidencia:"))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(uiText("Descripción", "Descrición")) },
                    minLines = 2,
                    maxLines = 4,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(nombre.trim(), descripcion.trim()) },
                enabled = nombre.isNotBlank() && descripcion.isNotBlank(),
            ) {
                Text(uiText("Añadir", "Engadir"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(uiText("Cancelar", "Cancelar"))
            }
        },
    )
}
