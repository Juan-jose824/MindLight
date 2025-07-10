package com.example.mindlight.presentation

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.mindlight.R
import java.text.SimpleDateFormat
import java.util.*

class HistorialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val textHistorial = findViewById<TextView>(R.id.text_historial)

        val eventos = EventoStorage.eventos
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        val texto = if (eventos.isEmpty()) {
            "No hay eventos registrados en MindLight"
        } else {
            eventos.joinToString("\n\n") {
                val fecha = formatter.format(Date(it.fecha))
                "📅 $fecha\nHRV: %.2f\nEstado: %s".format(it.hrv, it.actividad)
            }
        }

        textHistorial.text = texto
    }
}
