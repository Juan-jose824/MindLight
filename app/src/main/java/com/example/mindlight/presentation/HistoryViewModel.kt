package com.example.mindlight.presentation.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlight.data.SensorEventEntity
import com.example.mindlight.data.SensorEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: SensorEventRepository) : ViewModel() {

    private val _events = MutableStateFlow<List<SensorEventEntity>>(emptyList())
    val events: StateFlow<List<SensorEventEntity>> get() = _events

    fun loadEvents() {
        viewModelScope.launch {
            _events.value = repository.getAllEvents()
        }
    }
}
