package com.example.mindlight

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mindlight.data.SensorEventEntity
import com.example.mindlight.databinding.ItemSensorEventBinding
import java.text.SimpleDateFormat
import java.util.*

class SensorEventAdapter : ListAdapter<SensorEventEntity, SensorEventAdapter.SensorEventViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorEventViewHolder {
        val binding = ItemSensorEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SensorEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SensorEventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }

    class SensorEventViewHolder(private val binding: ItemSensorEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: SensorEventEntity) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val dateStr = sdf.format(Date(event.timestamp))
            binding.tvTimestamp.text = dateStr
            binding.tvHeartRate.text = "❤️ ${event.heartRate.toInt()} bpm"
            binding.tvLightLevel.text = "\uD83D\uDCA1 ${event.lightLevel.toInt()} lux"
            binding.tvMood.text = event.mood
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SensorEventEntity>() {
        override fun areItemsTheSame(oldItem: SensorEventEntity, newItem: SensorEventEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SensorEventEntity, newItem: SensorEventEntity) = oldItem == newItem
    }
}
