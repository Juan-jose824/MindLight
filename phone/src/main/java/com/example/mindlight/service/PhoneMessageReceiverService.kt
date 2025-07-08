package com.example.mindlight.service

import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class PhoneMessageReceiverService : WearableListenerService(), MessageClient.OnMessageReceivedListener {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/sensor_data") {
            val payload = String(messageEvent.data)
            val parts = payload.split("|")

            val heartRate = parts.getOrNull(0)?.toFloatOrNull()
            val lightLevel = parts.getOrNull(1)?.toFloatOrNull()
            val mood = parts.getOrNull(2) ?: "Unknown"

            Log.d("PhoneReceiver", "Datos recibidos: HR=$heartRate, Luz=$lightLevel, Mood=$mood")

            // Aquí puedes guardar en la base de datos o notificar al usuario, etc.
        }
    }

    override fun onCreate() {
        super.onCreate()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getMessageClient(this).removeListener(this)
    }
}
