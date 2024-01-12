package com.example.facedetectionapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class EmotionResult (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testText: String,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
    )