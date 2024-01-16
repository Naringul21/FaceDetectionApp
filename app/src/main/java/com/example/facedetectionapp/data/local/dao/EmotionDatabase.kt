package com.example.facedetectionapp.data.local.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.facedetectionapp.data.local.entities.EmotionResult

@Database(entities = [EmotionResult::class], version = 1)
abstract class EmotionDatabase : RoomDatabase() {
    abstract fun emotionResultDao(): EmotionResultDao

}