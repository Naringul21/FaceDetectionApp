package com.example.facedetectionapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class EmotionResult (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var testText: String="",
    var isCorrect: Boolean=false,
    var timestamp: Long = System.currentTimeMillis()
    )