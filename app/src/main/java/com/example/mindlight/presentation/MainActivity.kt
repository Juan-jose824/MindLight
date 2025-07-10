package com.example.mindlight.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mindlight.R
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity() {

    private lateinit var sensorService: SensorService
    private lateinit var heartRateText: TextView
    private lateinit var activityText: TextView
    private lateinit var hrvText: TextView
    private lateinit var lightText: TextView

    private var estadoActual = "Reposo"
    private var ultimoHRV: Double = 0.0
    private val eventosEstres = EventoStorage.eventos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "🌙 MindLight\nEquilibra tu mente y tu entorno", Toast.LENGTH_LONG).show()

        heartRateText = findViewById(R.id.text_hr)
        activityText = findViewById(R.id.text_activity)
        hrvText = findViewById(R.id.text_hrv)
        lightText = findViewById(R.id.text_light)

        checkAndRequestPermissions()

        sensorService = SensorService(
            context = this,
            onHeartRateUpdate = { bpm ->
                runOnUiThread { heartRateText.text = "❤️ Ritmo cardíaco: $bpm" }
            },
            onActivityUpdate = { estado ->
                estadoActual = estado
                runOnUiThread { activityText.text = "Estado: $estado" }
            },
            onRRInterval = { rrList ->
                val hrv = calcularRMSSD(rrList)
                runOnUiThread {
                    hrvText.text = "HRV: %.2f ms".format(hrv)
                    evaluarEstres(hrv)
                }
            },
            onLightUpdate = { lux ->
                runOnUiThread {
                    lightText.text = "💡 Luz: %.1f lx".format(lux)
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        sensorService.startListening()
    }

    override fun onPause() {
        super.onPause()
        sensorService.stopListening()
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), 1001)
        }
    }

    private fun calcularRMSSD(rrIntervals: List<Long>): Double {
        if (rrIntervals.size < 2) return 0.0
        val diffs = rrIntervals.zipWithNext { a, b -> (b - a).toDouble() }
        val squares = diffs.map { it * it }
        return Math.sqrt(squares.average())
    }

    private fun evaluarEstres(hrv: Double) {
        ultimoHRV = hrv
        if (hrv < 50 && estadoActual == "Actividad") {
            Log.d("ESTRES", "⚠️ Estrés detectado")
            mostrarAlertaEstres()
            guardarEventoEstres()
        }
    }

    private fun mostrarAlertaEstres() {
        Toast.makeText(this, "⚠️ Estrés detectado", Toast.LENGTH_SHORT).show()
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }

    private fun guardarEventoEstres() {
        val evento = EventoEstres(
            fecha = System.currentTimeMillis(),
            hrv = ultimoHRV,
            actividad = estadoActual
        )
        eventosEstres.add(evento)
        Log.d("ESTRES", "📌 Evento guardado: $evento")
        enviarEventoEstresAlTelefono(evento.hrv, evento.actividad)
    }

    private fun enviarEventoEstresAlTelefono(hrv: Double, actividad: String) {
        val mensaje = "estres|$hrv|$actividad|${System.currentTimeMillis()}"
        val client = Wearable.getMessageClient(this)

        Log.d("SYNC", "Preparando mensaje: $mensaje")

        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                if (nodes.isEmpty()) {
                    Log.w("SYNC", "⚠️ No hay nodos conectados")
                }

                for (node in nodes) {
                    Log.d("SYNC", "Enviando a nodo: ${node.displayName}")
                    client.sendMessage(node.id, "/evento_estres", mensaje.toByteArray())
                        .addOnSuccessListener {
                            Log.d("SYNC", "Mensaje enviado a ${node.displayName}")
                        }
                        .addOnFailureListener {
                            Log.e("SYNC", "Fallo al enviar mensaje", it)
                        }
                }
            }
            .addOnFailureListener {
                Log.e("SYNC", "❌ Error al obtener nodos", it)
            }
    }
}
