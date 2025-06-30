package com.example.mindlight.presentation.sensors

import android.content.Context
import android.graphics.pdf.models.ListItem
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import com.example.mindlight.data.SensorEventEntity
import java.text.SimpleDateFormat
import java.util.*



@Composable
fun HistoryScreen(context: Context) {
    val factory = HistoryViewModelFactory(context)
    val viewModel: HistoryViewModel = viewModel(factory = factory)
    val events by viewModel.events.collectAsState()

    // Cargar eventos al abrir pantalla
    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp)
    ) {
        items(events.take(10)) { event: SensorEventEntity ->
            val date = remember(event.timestamp) {
                SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault()).format(Date(event.timestamp))
            }

            ListItem(
                headlineContent = { Text("🕒 $date") },
                supportingContent = {
                    Text("❤️ ${event.heartRate.toInt()} | 💡 ${event.lightLevel.toInt()} | 😐 ${event.mood}")
                }
            )
        }
    }
}
