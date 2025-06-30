// sync.kt
package com.example.mindlight.sync

import android.content.Context
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import kotlinx.coroutines.tasks.await

suspend fun sendSensorDataToPhone(
    context: Context,
    heartRate: Float,
    lightLevel: Float,
    mood: String
) {
    val dataClient = Wearable.getDataClient(context)

    val dataMap = PutDataMapRequest.create("/mindlight/sensor_data").apply {
        dataMap.putFloat("heartRate", heartRate)
        dataMap.putFloat("lightLevel", lightLevel)
        dataMap.putString("mood", mood)
        dataMap.putLong("timestamp", System.currentTimeMillis())
    }

    val request: PutDataRequest = dataMap.asPutDataRequest().setUrgent()
    dataClient.putDataItem(request).await()
}
