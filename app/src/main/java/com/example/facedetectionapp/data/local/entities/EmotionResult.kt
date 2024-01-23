package com.example.facedetectionapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "emotion_result")
data class EmotionResult (
    @PrimaryKey(autoGenerate = true)
    val id:Long,

    var left:Boolean=false,
    var right:Boolean=false,
    var smile:Boolean=false,
    var neutral: Boolean=false
)