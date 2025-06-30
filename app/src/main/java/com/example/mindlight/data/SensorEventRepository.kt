package com.example.mindlight.data

class SensorEventRepository(private val dao: SensorEventDao) {
    suspend fun insertEvent(event: SensorEventEntity) = dao.insertEvent(event)
    suspend fun getAllEvents() = dao.getAllEvents()
}
