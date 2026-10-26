package com.works.naval

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private enum class AdminLoginError {
    INVALID_CREDENTIALS,
    CONNECTION,
}

@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onAuthenticated: (String) -> Unit,
) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<AdminLoginError?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(uiText("Acceso de administrador", "Acceso de administrador")) },
        text = {
            Column {
                OutlinedTextField(
                    value = usuario,
                    onValueChange = {
                        usuario = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(uiText("Usuario", "Usuario")) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text(uiText("Contraseña", "Contrasinal")) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                )
                errorMessage?.let { error ->
                    Text(
                        text = when (error) {
                            AdminLoginError.INVALID_CREDENTIALS -> uiText(
                                "Credenciales incorrectas o usuario sin permisos de administrador",
                                "Credenciais incorrectas ou usuario sen permisos de administrador",
                            )
                            AdminLoginError.CONNECTION -> uiText(
                                "No se pudo validar el acceso de administrador",
                                "Non se puido validar o acceso de administrador",
                            )
                        },
                        modifier = Modifier.padding(top = 8.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val administrator = loginAdministrador(usuario.trim(), password)
                            if (administrator == null) {
                                errorMessage = AdminLoginError.INVALID_CREDENTIALS
                            } else {
                                onAuthenticated(administrator)
                            }
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            errorMessage = AdminLoginError.CONNECTION
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = usuario.isNotBlank() && password.isNotBlank() && !isLoading,
            ) {
                Text(
                    if (isLoading) {
                        uiText("Validando...", "Validando...")
                    } else {
                        uiText("Entrar", "Entrar")
                    },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(uiText("Cancelar", "Cancelar"))
            }
        },
    )
}
