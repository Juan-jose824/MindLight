package com.example.mindlight.presentation.sensors

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mindlight.data.MindLightDatabase
import com.example.mindlight.data.SensorEventRepository

class HistoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dao = MindLightDatabase.getDatabase(context).sensorEventDao()
        val repo = SensorEventRepository(dao)
        return HistoryViewModel(repo) as T
    }
}
