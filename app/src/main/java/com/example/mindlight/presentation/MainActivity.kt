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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.mindlight.R
import com.example.mindlight.presentation.sensors.HeartRateViewModel
import com.example.mindlight.presentation.sensors.LightSensorViewModel
import com.example.mindlight.presentation.theme.MindLightTheme
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindlight.viewmodel.SensorEventViewModel
import com.example.mindlight.viewmodel.SensorEventViewModelFactory
import com.example.mindlight.data.MindLightDatabase
import com.example.mindlight.data.SensorEventRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.mindlight.sync.sendSensorDataToPhone




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                SplashScreen {
                    showSplash = false
                }
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
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🌙 MindLight",
                    style = MaterialTheme.typography.title2,
                    color = MaterialTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Equilibra tu mente y tu entorno",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.secondary,
                    textAlign = TextAlign.Center
                )
               /* Text(
                    text = "✅ Monitoreando...",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.primary,
                    textAlign = TextAlign.Center
                )*/

            }
        }
    }
}

@Composable
fun WearApp(
    greetingName: String,
    heartRateViewModel: HeartRateViewModel = viewModel(),
    lightSensorViewModel: LightSensorViewModel = viewModel()
) {
    val heartRateState = heartRateViewModel.heartRate.collectAsState()
    val lightLevelState = lightSensorViewModel.lightLevel.collectAsState()
    val context = LocalContext.current

    // Crear instancia del ViewModel con Factory
    val database = remember { MindLightDatabase.getDatabase(context) }
    val repository = remember { SensorEventRepository(database.sensorEventDao()) }
    val viewModelFactory = remember { SensorEventViewModelFactory(repository) }
    val sensorEventViewModel: SensorEventViewModel = viewModel(factory = viewModelFactory)

    val mood = analyzeMood(heartRateState.value, lightLevelState.value)

    // Guardar evento cuando haya datos disponibles
    LaunchedEffect(heartRateState.value, lightLevelState.value) {
        val heartRate = heartRateState.value
        val light = lightLevelState.value

        if (heartRate != null && light != null) {
            // ✅ Ahora también se envía al teléfono
            sensorEventViewModel.saveSensorEvent(heartRate, light, mood, context)
        }
    }

    // UI
    MindLightTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimeText()
                Greeting(greetingName = greetingName)
                Spacer(modifier = Modifier.height(8.dp))

                heartRateState.value?.let { bpm ->
                    Text(
                        text = "❤️ ${bpm.toInt()} bpm",
                        color = MaterialTheme.colors.secondary,
                        textAlign = TextAlign.Center
                    )
                } ?: Text("Esperando ritmo cardíaco...", color = MaterialTheme.colors.secondary)

                Spacer(modifier = Modifier.height(8.dp))

                lightLevelState.value?.let { lux ->
                    Text(
                        text = "\uD83D\uDCA1 Luz: ${lux.toInt()} lux",
                        color = MaterialTheme.colors.secondary,
                        textAlign = TextAlign.Center
                    )
                } ?: Text("Esperando sensor de luz...", color = MaterialTheme.colors.secondary)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = mood,
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.caption1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}

// Función para analizar el estado emocional basada en sensores
fun analyzeMood(heartRate: Float?, lightLevel: Float?): String {
    return when {
        heartRate == null || lightLevel == null -> "Esperando datos..."
        heartRate > 100 && lightLevel < 20 -> "Posible estrés: respira profundo"
        heartRate < 70 && lightLevel > 100 -> "Ambiente relajado"
        heartRate > 110 -> "Alta actividad o tensión"
        lightLevel < 10 -> "Entorno muy oscuro"
        else -> "Estado estable"
    }
}
