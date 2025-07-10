package com.example.mindlight

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.wearable.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpleEventAdapter
    private val eventosList = mutableListOf<String>()

    private val client = OkHttpClient()
    private val apiUrl = "https://mindlight-api.onrender.com" // ← REEMPLAZA con tu URL real

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.eventsRecyclerView)
        adapter = SimpleEventAdapter(eventosList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Cargar eventos iniciales desde MongoDB
        obtenerEventosDesdeMongo()
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/DATOS_SENSORES") {
            val data = String(messageEvent.data, Charsets.UTF_8)
            val partes = data.split(",")

            val heartRate = partes.getOrNull(0)?.substringAfter(":")?.toFloatOrNull()
            val lightLevel = partes.getOrNull(1)?.substringAfter(":")?.toFloatOrNull()

            if (heartRate != null && lightLevel != null) {
                val estado = analizarEstadoEmocional(heartRate, lightLevel)

                val evento = JSONObject()
                evento.put("frecuencia_cardiaca", heartRate)
                evento.put("nivel_luz", lightLevel)
                evento.put("estado", estado)
                evento.put("timestamp", System.currentTimeMillis())

                guardarEventoEnMongo(evento)
            }
        }
    }

    private fun guardarEventoEnMongo(json: JSONObject) {
        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error al subir evento", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    obtenerEventosDesdeMongo()
                }
            }
        })
    }

    private fun obtenerEventosDesdeMongo() {
        val request = Request.Builder().url(apiUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error al cargar eventos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val cuerpo = response.body?.string()
                val nuevosEventos = mutableListOf<String>()

                if (response.isSuccessful && cuerpo != null) {
                    val jsonArray = JSONArray(cuerpo)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val texto = "FC: ${obj.getDouble("frecuencia_cardiaca")}, Luz: ${obj.getDouble("nivel_luz")}, Estado: ${obj.getString("estado")}"
                        nuevosEventos.add(texto)
                    }

                    runOnUiThread {
                        eventosList.clear()
                        eventosList.addAll(nuevosEventos)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun analizarEstadoEmocional(hr: Float, lux: Float): String = when {
        hr > 100 && lux < 20 -> "Estrés probable"
        hr < 70 && lux > 100 -> "Ambiente relajado"
        hr > 110             -> "Tensión o ejercicio"
        lux < 10             -> "Ambiente oscuro"
        else                 -> "Estado equilibrado"
    }
}
