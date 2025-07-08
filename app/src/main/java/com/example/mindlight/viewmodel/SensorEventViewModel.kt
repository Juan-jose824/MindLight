package com.example.mindlight.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlight.data.SensorEventEntity
import com.example.mindlight.data.SensorEventRepository
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SensorEventViewModel(
    private val repository: SensorEventRepository
) : ViewModel() {

    // Estado que indica si la sincronización fue exitosa
    private val _syncStatus = MutableStateFlow<Boolean?>(null)
    val syncStatus: StateFlow<Boolean?> get() = _syncStatus

    // Función para guardar el evento y enviar los datos al teléfono
    fun saveEvent(heartRate: Float, lightLevel: Float, mood: String, context: Context) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()

            val event = SensorEventEntity(
                timestamp = timestamp,
                heartRate = heartRate,
                lightLevel = lightLevel,
                mood = mood
            )

            // Guardar en la base local
            repository.insertEvent(event)

            // Enviar al teléfono mediante Data Layer
            try {
                val message = "$heartRate|$lightLevel|$mood|$timestamp"

                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                for (node in nodes) {
                    Wearable.getMessageClient(context)
                        .sendMessage(node.id, "/sensor_data", message.toByteArray())
                        .await()
                }

                _syncStatus.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _syncStatus.value = false
            }
        }
    }
}
