package com.works.naval

import com.works.naval.data.preferences.AndroidAppPreferences
import com.works.naval.platform.configureOfflineIsometricoPdfStorage
import com.works.naval.presentacion.app.App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        configureOfflineIsometricoPdfStorage(applicationContext)

        setContent {
            App(AndroidAppPreferences(applicationContext))
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(AndroidAppPreferences(LocalContext.current))
}