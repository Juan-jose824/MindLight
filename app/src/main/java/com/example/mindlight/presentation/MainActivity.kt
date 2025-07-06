package com.example.mindlight.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.mindlight.presentation.sensors.HeartRateViewModel
import com.example.mindlight.presentation.sensors.LightSensorViewModel
import com.example.mindlight.presentation.theme.MindLightTheme
import com.example.mindlight.data.MindLightDatabase
import com.example.mindlight.data.SensorEventRepository
import com.example.mindlight.viewmodel.SensorEventViewModel
import com.example.mindlight.viewmodel.SensorEventViewModelFactory
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                SplashScreen { showSplash = false }
            } else {
                WearApp("Android")
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
            modifier = Modifier
                .fillMaxSize()
                .background(color = androidx.compose.ui.graphics.Color(0xFFFFF9C4)), // Amarillo claro
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🌙 MindLight",
                    style = MaterialTheme.typography.title2,
                    color = androidx.compose.ui.graphics.Color.Black
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Equilibra tu mente y tu entorno",
                    style = MaterialTheme.typography.caption1,
                    color = androidx.compose.ui.graphics.Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun WearApp(
    name: String,
    heartRateViewModel: HeartRateViewModel = viewModel(),
    lightSensorViewModel: LightSensorViewModel = viewModel()
) {
    val heartRateState = heartRateViewModel.heartRate.collectAsState()
    val lightLevelState = lightSensorViewModel.lightLevel.collectAsState()
    val context = LocalContext.current

    val database = remember { MindLightDatabase.getDatabase(context) }
    val repository = remember { SensorEventRepository(database.sensorEventDao()) }
    val sensorEventViewModel: SensorEventViewModel = viewModel(
        factory = SensorEventViewModelFactory(repository)
    )

    val mood = analyzeMood(heartRateState.value, lightLevelState.value)

    val sensorStatus = remember { mutableStateOf(0) }
    LaunchedEffect(heartRateState.value, lightLevelState.value) {
        var status = 0
        if (heartRateState.value != null) status += 1
        if (lightLevelState.value != null) status += 2
        sensorStatus.value = status
    }

    LaunchedEffect(heartRateState.value, lightLevelState.value) {
        heartRateState.value?.let { hr ->
            lightLevelState.value?.let { lux ->
                sensorEventViewModel.saveSensorEvent(hr, lux, mood, context)
            }
        }
    }

    MindLightTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = androidx.compose.ui.graphics.Color.White), // fondo blanco para visibilidad
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "MindLight está activo",
                    color = androidx.compose.ui.graphics.Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Estado sensores: ${sensorStatus.value}",
                    color = androidx.compose.ui.graphics.Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = heartRateState.value?.let { "❤️ Ritmo cardíaco: ${it.toInt()} bpm" }
                        ?: "Esperando ritmo cardíaco…",
                    color = androidx.compose.ui.graphics.Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = lightLevelState.value?.let { "💡 Luz ambiental: ${it.toInt()} lux" }
                        ?: "Esperando sensor de luz…",
                    color = androidx.compose.ui.graphics.Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))

                Text(
                    text = mood,
                    color = androidx.compose.ui.graphics.Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



@Composable
fun Greeting(name: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = "Hola $name",
        textAlign = TextAlign.Center,
        color = androidx.compose.ui.graphics.Color.Black
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewWearApp() {
    WearApp("Preview")
}

fun analyzeMood(hr: Float?, lux: Float?): String = when {
    hr == null || lux == null -> "Esperando datos…"
    hr > 100 && lux < 20 -> "Posible estrés: respira profundo"
    hr < 70 && lux > 100 -> "Ambiente relajado"
    hr > 110 -> "Alta actividad o tensión"
    lux < 10 -> "Entorno muy oscuro"
    else -> "Estado estable"
}
