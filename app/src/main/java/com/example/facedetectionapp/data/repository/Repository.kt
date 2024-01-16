package com.example.facedetectionapp.data.repository

import com.example.facedetectionapp.data.local.dao.EmotionResultDao
import com.example.facedetectionapp.data.local.entities.EmotionResult
import javax.inject.Inject

class Repository @Inject constructor(private val db:EmotionResultDao){


        suspend fun insertEmotion(emotionResult: EmotionResult){
            db.insertEmotion(emotionResult)
        }
        fun getResultData()=db.getEmotionData()
    }
