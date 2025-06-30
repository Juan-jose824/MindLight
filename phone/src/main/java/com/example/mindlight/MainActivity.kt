package com.example.mindlight

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindlight.databinding.ActivityMainBinding
import com.example.mindlight.viewmodel.SensorEventViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sensorEventViewModel: SensorEventViewModel by viewModels()
    private lateinit var adapter: SensorEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SensorEventAdapter()
        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.eventsRecyclerView.adapter = adapter

        lifecycleScope.launch {
            sensorEventViewModel.events.collectLatest { events ->
                adapter.submitList(events)
            }
        }
    }
}
