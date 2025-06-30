package com.example.mindlight.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_events")
data class SensorEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val heartRate: Float,
    val lightLevel: Float,
    val mood: String
)
