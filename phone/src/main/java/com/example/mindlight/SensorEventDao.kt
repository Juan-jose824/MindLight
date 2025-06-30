package com.example.mindlight.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorEventDao {
    @Insert
    suspend fun insertEvent(event: SensorEventEntity)

    @Query("SELECT * FROM sensor_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<SensorEventEntity>>
}
