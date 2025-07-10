package com.example.mindlight.presentation

import android.content.Context
import android.hardware.*
import android.os.SystemClock
import android.util.Log

class SensorService(
    private val context: Context,
    private val onHeartRateUpdate: (Float) -> Unit,
    private val onActivityUpdate: (String) -> Unit,
    private val onRRInterval: (List<Long>) -> Unit,
    private val onLightUpdate: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val rrIntervals = mutableListOf<Long>()
    private var lastRRTime: Long? = null

    fun startListening() {
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        rrIntervals.clear()
        lastRRTime = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_HEART_RATE -> {
                    val bpm = it.values[0]
                    Log.d("HR", "Heart Rate: $bpm")
                    onHeartRateUpdate(bpm)

                    val now = SystemClock.elapsedRealtime()
                    lastRRTime?.let { lastTime ->
                        val rr = now - lastTime
                        if (rr in 300..2000) {
                            rrIntervals.add(rr)
                            if (rrIntervals.size > 20) {
                                rrIntervals.removeAt(0)
                            }
                            onRRInterval(rrIntervals.toList())
                        }
                    }
                    lastRRTime = now
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble())
                    val estado = if (magnitude < 1.5) "Reposo" else "Actividad"
                    Log.d("ACC", "Actividad: $estado")
                    onActivityUpdate(estado)
                }

                Sensor.TYPE_LIGHT -> {
                    val lux = it.values[0]
                    Log.d("LUX", "Luminosidad: $lux lux")
                    onLightUpdate(lux)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
