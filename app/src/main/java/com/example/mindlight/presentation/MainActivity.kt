package com.example.mindlight.presentation

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text as WearText
import com.example.mindlight.data.MindLightDatabase
import com.example.mindlight.data.SensorEventRepository
import com.example.mindlight.presentation.sensors.HeartRateViewModel
import com.example.mindlight.presentation.sensors.LightSensorViewModel
import com.example.mindlight.presentation.theme.MindLightTheme
import com.example.mindlight.viewmodel.SensorEventViewModel
import com.example.mindlight.viewmodel.SensorEventViewModelFactory

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), 100)
        }

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            if (showSplash) {
                SplashScreen { showSplash = false }
            } else {
                WearApp()
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    MindLightTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFFFF9C4)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WearText(
                    text = "🌙 MindLight",
                    style = MaterialTheme.typography.title2,
                    color = Color.Black
                )
                Spacer(Modifier.height(10.dp))
                WearText(
                    text = "Equilibra tu mente y tu entorno",
                    style = MaterialTheme.typography.caption1,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun WearApp(
    heartRateVM: HeartRateViewModel = viewModel(),
    lightSensorVM: LightSensorViewModel = viewModel()
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val hrState by heartRateVM.heartRate.collectAsState()
    val luxState by lightSensorVM.lightLevel.collectAsState()

    val database = remember { MindLightDatabase.getDatabase(context) }
    val repository = remember { SensorEventRepository(database.sensorEventDao()) }
    val sensorVM: SensorEventViewModel = viewModel(
        factory = SensorEventViewModelFactory(repository)
    )

    var syncMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sensorVM) {
        sensorVM.syncStatus.collectLatest { success ->
            syncMessage = if (success == true) "Datos enviados al teléfono ✅" else "Error al sincronizar ❌"
            delay(2000)
            syncMessage = null
        }
    }

    val mood = analyzeMood(hrState, luxState)

    LaunchedEffect(hrState, luxState) {
        if (hrState != null && luxState != null) {
            sensorVM.saveEvent(hrState!!, luxState!!, mood, context)
        }
    }

    MindLightTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                WearText(
                    text = "MindLight está activo",
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                WearText(
                    text = hrState?.let { "❤️ Ritmo cardíaco: ${it.toInt()} bpm" } ?: "Esperando ritmo cardíaco…",
                    color = Color.Black
                )
                Spacer(Modifier.height(4.dp))
                WearText(
                    text = luxState?.let { "💡 Luz: ${it.toInt()} lux" } ?: "Esperando sensor de luz…",
                    color = Color.Black
                )
                Spacer(Modifier.height(8.dp))
                WearText(
                    text = mood,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            syncMessage?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .background(Color(0xFF323232), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    WearText(
                        text = it,
                        color = Color.White,
                        style = MaterialTheme.typography.caption2
                    )
                }
            }
        }
    }
}

fun analyzeMood(hr: Float?, lux: Float?): String = when {
    hr == null || lux == null -> "Esperando datos…"
    hr > 100 && lux < 20    -> "Posible estrés: respira"
    hr < 70  && lux > 100   -> "Ambiente relajado"
    hr > 110                -> "Alta actividad o tensión"
    lux < 10                -> "Entorno muy oscuro"
    else                    -> "Estado estable"
}
