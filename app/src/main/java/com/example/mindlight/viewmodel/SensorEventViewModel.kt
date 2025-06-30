package com.example.mindlight.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlight.data.SensorEventEntity
import com.example.mindlight.data.SensorEventRepository
import com.example.mindlight.sync.sendSensorDataToPhone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SensorEventViewModel(private val repository: SensorEventRepository) : ViewModel() {

    private val _events = MutableStateFlow<List<SensorEventEntity>>(emptyList())
    val events: StateFlow<List<SensorEventEntity>> get() = _events

    fun insertEvent(event: SensorEventEntity) {
        viewModelScope.launch {
            repository.insertEvent(event)
            _events.value = repository.getAllEvents()
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            _events.value = repository.getAllEvents()
        }
    }

    // Función actualizada para guardar evento y enviarlo al teléfono
    fun saveSensorEvent(heartRate: Float, lightLevel: Float, mood: String, context: Context) {
        val event = SensorEventEntity(
            timestamp = System.currentTimeMillis(),
            heartRate = heartRate,
            lightLevel = lightLevel,
            mood = mood
        )

        viewModelScope.launch {
            repository.insertEvent(event)
            _events.value = repository.getAllEvents()
            sendSensorDataToPhone(context, heartRate, lightLevel, mood)
        }
    }
}
