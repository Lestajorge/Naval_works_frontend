package com.works.naval

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        LoginScreenPreview()
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var loggedIn by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (loggedIn) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
           DashboardPrincipal(
               onNavigateToAssignTask = {},
               onGenerateReport = {},
               username = user,
               password = password,
           )
        }
    } else {
        Box(
           modifier = Modifier.fillMaxSize(),
           contentAlignment = Alignment.Center,
        ) {
           Column(
               modifier = Modifier
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
                           } catch (exception: Exception) {
                               message = "No se pudo conectar con el servidor"
                           } finally {
                               loading = false
                           }
                       }
                   },
                   modifier = Modifier.fillMaxWidth(),
                   enabled = !loading,
                   colors = ButtonDefaults.buttonColors(
                       containerColor = Color(0xFF134386),
                   ),
               ) {
                   Text(if (loading) "Comprobando..." else "Iniciar sesión")
               }
           }
        }
    }
}