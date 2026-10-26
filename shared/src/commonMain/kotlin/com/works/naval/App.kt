package com.works.naval

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun App(preferences: AppPreferences) {
    MaterialTheme {
        LoginScreen(preferences)
    }
}

@Composable
fun LoginScreen(preferences: AppPreferences) {
    var user by remember(preferences) { mutableStateOf("") }
    var password by remember(preferences) { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var loggedIn by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var connectionUrl by remember(preferences) { mutableStateOf(defaultApiBaseUrl) }
    var connectionSettingsLoaded by remember(preferences) { mutableStateOf(false) }
    var showConnectionSettings by remember { mutableStateOf(false) }
    var savingConnectionSettings by remember { mutableStateOf(false) }
    var connectionSettingsError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(preferences) {
        try {
            val settings =
                preferences.readServerConnectionSettings() ?: ServerConnectionSettings.defaults()
            connectionUrl = settings.url
            user = settings.username
            password = settings.password
            configureApiBaseUrl(settings.url)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            connectionSettingsError = "No se pudieron cargar las preferencias de conexión."
        } finally {
            connectionSettingsLoaded = true
        }
    }

    if (loggedIn) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
           DashboardPrincipal(
               preferences = preferences,
               onGenerateReport = {},
           )
        }
    } else {
        Box(
           modifier = Modifier.fillMaxSize(),
           contentAlignment = Alignment.Center,
        ) {
           Column(
               modifier = Modifier
                   .widthIn(max = 480.dp)
                   .fillMaxWidth()
                   .padding(horizontal = 24.dp),
               horizontalAlignment = Alignment.CenterHorizontally,
               verticalArrangement = Arrangement.Center,
           ) {
               OutlinedTextField(
                   value = user,
                   onValueChange = {
                       user = it
                       message = null
                   },
                   modifier = Modifier.fillMaxWidth(),
                   placeholder = { Text("Usuario") },
               )

               Spacer(modifier = Modifier.height(12.dp))

               OutlinedTextField(
                   value = password,
                   onValueChange = {
                       password = it
                       message = null
                   },
                   modifier = Modifier.fillMaxWidth(),
                   placeholder = { Text("Contraseña") },
                   visualTransformation = PasswordVisualTransformation(),
                   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
               )

               OutlinedButton(
                   onClick = { showConnectionSettings = true },
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(top = 12.dp),
                   enabled = connectionSettingsLoaded && !loading,
               ) {
                   Text("Configuración de conexión")
               }

               connectionSettingsError?.let {
                   Text(
                       text = it,
                       color = MaterialTheme.colorScheme.error,
                   )
               }

               message?.let {
                   Spacer(modifier = Modifier.height(12.dp))
                   Text(it, color = MaterialTheme.colorScheme.error)
               }

               Spacer(modifier = Modifier.height(20.dp))

               Button(
                   onClick = {
                       loading = true
                       message = null
                       scope.launch {
                           try {
                               loggedIn = login(user, password)
                               if (!loggedIn) message = "Usuario incorrecto"
                           } catch (exception: CancellationException) {
                               throw exception
                           } catch (exception: Exception) {
                               message = "No se pudo conectar con el servidor"
                           } finally {
                               loading = false
                           }
                       }
                   },
                   modifier = Modifier.fillMaxWidth(),
                   enabled = !loading &&
                       connectionSettingsLoaded &&
                       user.isNotBlank() &&
                       password.isNotBlank(),
                   colors = ButtonDefaults.buttonColors(
                       containerColor = Color(0xFF134386),
                   ),
               ) {
                   Text(if (loading) "Comprobando..." else "Iniciar sesión")
               }
           }
        }

        if (showConnectionSettings) {
           ConnectionSettingsDialog(
               initialSettings = ServerConnectionSettings(
                   url = connectionUrl,
                   username = user,
                   password = password,
               ),
               isSaving = savingConnectionSettings,
               errorMessage = connectionSettingsError,
               onDismiss = { showConnectionSettings = false },
               onSave = { settings ->
                   savingConnectionSettings = true
                   connectionSettingsError = null
                   scope.launch {
                       try {
                           preferences.writeServerConnectionSettings(settings)
                           connectionUrl = settings.url
                           user = settings.username
                           password = settings.password
                           configureApiBaseUrl(settings.url)
                           showConnectionSettings = false
                       } catch (exception: CancellationException) {
                           throw exception
                       } catch (exception: Exception) {
                           connectionSettingsError =
                               "No se pudieron guardar las preferencias de conexión."
                       } finally {
                           savingConnectionSettings = false
                       }
                   }
               },
           )
        }
    }
}