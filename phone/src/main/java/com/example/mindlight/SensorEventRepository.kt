package com.example.mindlight.data

import kotlinx.coroutines.flow.Flow

class SensorEventRepository(private val dao: SensorEventDao) {

    suspend fun insertEvent(event: SensorEventEntity) {
        dao.insertEvent(event)
    }

    fun getAllEvents(): Flow<List<SensorEventEntity>> {
        return dao.getAllEvents()
    }
}
