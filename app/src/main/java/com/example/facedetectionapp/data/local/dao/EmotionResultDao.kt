package com.example.facedetectionapp.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.facedetectionapp.data.local.entities.EmotionResult
@Dao
interface EmotionResultDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmotion(emotionResult: EmotionResult)

    @Query("SELECT*FROM emotionresult")
    fun getEmotionData():LiveData<List<EmotionResult>>

}

