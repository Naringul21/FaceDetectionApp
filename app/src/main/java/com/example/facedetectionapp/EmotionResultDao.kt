package com.example.facedetectionapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.facedetectionapp.data.EmotionResult
@Dao
interface EmotionResultDao {
    @Insert
    suspend fun insert(result: EmotionResult)


    @Query("SELECT * FROM emotionresult ORDER BY timestamp DESC")
    fun getAllResults(): LiveData<List<EmotionResult>>
}

