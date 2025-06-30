package com.example.mindlight.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlight.data.MindLightDatabase
import com.example.mindlight.data.SensorEventEntity
import com.example.mindlight.data.SensorEventRepository
import kotlinx.coroutines.launch

class SensorEventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SensorEventRepository

    init {
        val dao = MindLightDatabase.getDatabase(application).sensorEventDao()
        repository = SensorEventRepository(dao)
    }

    fun saveEvent(heartRate: Float, lightLevel: Float, mood: String) {
        val timestamp = System.currentTimeMillis()
        val event = SensorEventEntity(
            timestamp = timestamp,
            heartRate = heartRate,
            lightLevel = lightLevel,
            mood = mood
        )
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }
}
