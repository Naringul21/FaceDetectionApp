package com.example.facedetectionapp

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.facedetectionapp.data.EmotionResult

@Database(entities = [EmotionResult::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun headTurnResultDao(): EmotionResultDao

}