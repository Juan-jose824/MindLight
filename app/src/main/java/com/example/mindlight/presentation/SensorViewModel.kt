package com.example.mindlight.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlight.data.MindLightDatabase
import com.example.mindlight.data.SensorEventEntity
import com.example.mindlight.data.SensorEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SensorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SensorEventRepository

    init {
        val dao = MindLightDatabase.getDatabase(application).sensorEventDao()
        repository = SensorEventRepository(dao)
    }

    fun insertEvent(event: SensorEventEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertEvent(event)
        }
    }

    fun getAllEvents(onResult: (List<SensorEventEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val events = repository.getAllEvents()
            onResult(events)
        }
    }
}
