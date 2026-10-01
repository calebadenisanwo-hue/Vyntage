package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eq_presets")
data class EqPresetEntity(
    @PrimaryKey val name: String,
    val band0: Int = 0,
    val band1: Int = 0,
    val band2: Int = 0,
    val band3: Int = 0,
    val band4: Int = 0,
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val isCustom: Boolean = false
)
