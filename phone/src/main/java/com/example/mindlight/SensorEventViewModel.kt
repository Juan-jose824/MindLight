package com.example.mindlight.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlight.data.MindLightDatabase
import com.example.mindlight.data.SensorEventEntity
import com.example.mindlight.data.SensorEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SensorEventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SensorEventRepository

    private val _events = MutableStateFlow<List<SensorEventEntity>>(emptyList())
    val events: StateFlow<List<SensorEventEntity>> get() = _events

    init {
        val dao = MindLightDatabase.getDatabase(application).sensorEventDao()
        repository = SensorEventRepository(dao)
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            repository.getAllEvents().collect { eventsList ->
                _events.value = eventsList
            }
        }
    }

    fun saveEvent(heartRate: Float, lightLevel: Float, mood: String) {
        val event = SensorEventEntity(
            timestamp = System.currentTimeMillis(),
            heartRate = heartRate,
            lightLevel = lightLevel,
            mood = mood
        )
        viewModelScope.launch {
            repository.insertEvent(event)
            loadEvents()
        }
    }
}
