package com.example.mindlight.sync

import android.util.Log
import com.example.mindlight.data.MindLightDatabase
import com.example.mindlight.data.SensorEventEntity
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhoneDataListenerService : WearableListenerService() {

    override fun onDataChanged(buffer: DataEventBuffer) {
        for (event in buffer) {
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/mindlight/sensor_data"
            ) {
                val map = DataMapItem.fromDataItem(event.dataItem).dataMap
                val entity = SensorEventEntity(
                    timestamp = map.getLong("timestamp"),
                    heartRate = map.getFloat("heartRate"),
                    lightLevel = map.getFloat("lightLevel"),
                    mood = map.getString("mood") ?: "Sin estado"
                )

                CoroutineScope(Dispatchers.IO).launch {
                    val dao = MindLightDatabase.getDatabase(applicationContext).sensorEventDao()
                    dao.insertEvent(entity)
                    Log.d("PhoneSync", "Evento recibido y guardado: $entity")
                }
            }
        }
    }
}