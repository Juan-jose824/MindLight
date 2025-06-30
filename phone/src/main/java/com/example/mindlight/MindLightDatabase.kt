package com.example.mindlight.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SensorEventEntity::class], version = 1, exportSchema = false)
abstract class MindLightDatabase : RoomDatabase() {
    abstract fun sensorEventDao(): SensorEventDao

    companion object {
        @Volatile
        private var INSTANCE: MindLightDatabase? = null

        fun getDatabase(context: Context): MindLightDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MindLightDatabase::class.java,
                    "mindlight_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
