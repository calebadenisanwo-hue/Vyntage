package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TrackEntity::class, EqPresetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PhonoDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun eqPresetDao(): EqPresetDao

    companion object {
        @Volatile
        private var INSTANCE: PhonoDatabase? = null

        fun getInstance(context: Context): PhonoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhonoDatabase::class.java,
                    "phono_music.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
