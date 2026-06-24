package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_scripts")
data class ScriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val platform: String,
    val tone: String,
    val videoTitle: String,
    val scriptJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
