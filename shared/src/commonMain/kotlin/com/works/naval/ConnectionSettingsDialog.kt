package com.works.naval

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionSettingsDialog(
    initialSettings: ServerConnectionSettings,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (ServerConnectionSettings) -> Unit,
) {
    var url by remember(initialSettings) { mutableStateOf(initialSettings.url) }
    var username by remember(initialSettings) { mutableStateOf(initialSettings.username) }
    var password by remember(initialSettings) { mutableStateOf(initialSettings.password) }
    val isUrlValid = isValidServerUrl(url)

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Configuración de conexión") },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL del servidor") },
                    placeholder = { Text(defaultApiBaseUrl) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                    enabled = !isSaving,
                    isError = !isUrlValid,
                    supportingText = if (!isUrlValid) {
                        { Text("Introduce una URL válida que empiece por http:// o https://") }
                    } else {
                        null
                    },
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text("Usuario") },
                    singleLine = true,
                    enabled = !isSaving,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = !isSaving,
                )
                TextButton(
                    onClick = {
                        url = defaultApiBaseUrl
                        username = ""
                        password = ""
                    },
                    enabled = !isSaving,
                ) {
                    Text("Restablecer valores por defecto")
                }
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ServerConnectionSettings(
                            url = url.trim().trimEnd('/'),
                            username = username.trim(),
                            password = password,
                        ),
                    )
                },
                enabled = isUrlValid && !isSaving,
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar")
            }
        },
    )
}

private fun isValidServerUrl(value: String): Boolean {
    val trimmed = value.trim()
    val hasSupportedScheme =
        trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
    if (
        !hasSupportedScheme ||
        trimmed.any(Char::isWhitespace) ||
        trimmed.contains('?') ||
        trimmed.contains('#')
    ) {
        return false
    }

    return trimmed.substringAfter("://").substringBefore('/').isNotBlank()
}
